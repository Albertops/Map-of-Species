package es.uvigo.esei.tfg.mapofspecies;

import android.app.ProgressDialog;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Permite parsear un fichero xml a través del portal de datos de GBIF a través de diferentes
 * funciones.
 * @author Alberto Pardellas Soto
 */
public class GBIFDataParser {
    private final String gbifNS = "http://portal.gbif.org/ws/response/gbif";
    private final String tcNS = "http://rs.tdwg.org/ontology/voc/TaxonConcept#";
    private final String rdfNS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    private ArrayList<String> urlsTaxonConcept;
    private ArrayList<String> urlsSynonymsFound;
    private ArrayList<String> listSynonyms;
    private ArrayList<Occurrence> occurrences;
    private URL url;
    private String urlTaxon;
    private String nextUrl;
    private String currentNameComplete;
    private int totalMatched;
    private int cont;
    private int currentColor;
    private boolean add;
    private Occurrence currentOccurrence;
    private ProgressDialog progressDialog;

    /**
     * Constructor por defecto.
     * @param progressDialog barra de progreso.
     */
    public GBIFDataParser (ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    /**
     * Obtiene las urls de los taxon concepts.
     * @param url cadena de texto con la url.
     */
    public void getUrlsTaxonConcepts(String url) {
        RootElement root = new RootElement(gbifNS, "gbifResponse");

        Element dataProviders = root.getChild(gbifNS, "dataProviders");
        Element dataProvider = dataProviders.getChild(gbifNS, "dataProvider");
        Element dataResources = dataProvider.getChild(gbifNS, "dataResources");
        Element dataResource = dataResources.getChild(gbifNS, "dataResource");
        Element taxonConcepts = dataResource.getChild(gbifNS, "taxonConcepts");
        Element taxonConcept = taxonConcepts.getChild(tcNS, "TaxonConcept");

        urlsTaxonConcept = new ArrayList<String>();
        totalMatched = 0;

        taxonConcept.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                String urlTaxonConcept = attributes.getValue(rdfNS, "about");
                urlsTaxonConcept.add(urlTaxonConcept);
            }
        });

        try {
            this.url = new URL(url);
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());

        } catch (IOException e) {
            e.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene los sinónimos de un taxon concept.
     * @param nextUrl cadena de texto con la url.
     */
    public void getSynonyms(String nextUrl) {
        String tnNS = "http://rs.tdwg.org/ontology/voc/TaxonName#";

        RootElement root = new RootElement(gbifNS, "gbifResponse");

        Element dataProviders = root.getChild(gbifNS, "dataProviders");
        Element dataProvider = dataProviders.getChild(gbifNS, "dataProvider");
        Element dataResources = dataProvider.getChild(gbifNS, "dataResources");
        Element dataResource = dataResources.getChild(gbifNS, "dataResource");
        Element taxonConcepts = dataResource.getChild(gbifNS, "taxonConcepts");
        Element taxonConcept = taxonConcepts.getChild(tcNS, "TaxonConcept");
        Element hasRelationship = taxonConcept.getChild(tcNS, "hasRelationship");
        Element relationship = hasRelationship.getChild(tcNS, "Relationship");
        Element toTaxon = relationship.getChild(tcNS, "toTaxon");
        Element relationshipCategory = relationship.getChild(tcNS, "relationshipCategory");
        Element hasName = taxonConcept.getChild(tcNS, "hasName");
        Element TaxonName = hasName.getChild(tnNS, "TaxonName");
        Element nameComplete = TaxonName.getChild(tnNS, "nameComplete");
        Element primary = taxonConcept.getChild("primary");

        add = false;

        taxonConcept.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                if (attributes.getValue("status").equals("accepted")) {
                    add = true;
                }
            }
        });

        taxonConcept.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                add = false;
            }
        });

        nameComplete.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                currentNameComplete = s;
            }
        });

        primary.setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                if (s.equals("false")) {
                    add = false;
                }
            }
        });

        toTaxon.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                urlTaxon = attributes.getValue(rdfNS, "resource");
            }
        });

        relationshipCategory.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                String resource = attributes.getValue("resource");

                if (resource.endsWith("#HasSynonym")
                        || resource.endsWith("#Includes")
                        || resource.endsWith("#IsIncludedIn")
                        || resource.endsWith("#HasVernacular")) {

                    if (add) {
                        urlsSynonymsFound.add(urlTaxon);
                        listSynonyms.add(currentNameComplete);
                    }
                }
            }
        });

        try {
            this.url = new URL(nextUrl);
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());

        } catch (IOException e) {
            e.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene las url de los taxon concepts y luego busca los sinónimos.
     * @param url cadena de texto con la url.
     * @return lista de sinónimos.
     */
    public ArrayList<String> getListOfSynonyms(String url) {
        ArrayList<String> aux = new ArrayList<String>();
        listSynonyms = new ArrayList<String>();
        urlsSynonymsFound = new ArrayList<String>();

        getUrlsTaxonConcepts(url);
        progressDialog.setMax(urlsTaxonConcept.size());

        int cont = 0;

        for (String urlTaxonConcept : urlsTaxonConcept) {
            getSynonyms(urlTaxonConcept);
            progressDialog.setProgress(++cont);
        }

        do {
            for (String urlSynonymsFound : urlsSynonymsFound) {
                if (!urlsTaxonConcept.contains(urlSynonymsFound)) {
                    urlsTaxonConcept.add(urlSynonymsFound);
                    aux.add(urlSynonymsFound);
                }
            }

            progressDialog.setMax(urlsTaxonConcept.size());

            urlsSynonymsFound.clear();

            for (String s : aux) {
                getSynonyms(s);
                progressDialog.setProgress(++cont);
            }
            aux.clear();

        } while (!aux.isEmpty() || !urlsSynonymsFound.isEmpty());

        // Quitamos duplicados
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(listSynonyms);
        listSynonyms.clear();
        listSynonyms.addAll(hashSet);

        return listSynonyms;
    }

    /**
     * Cuenta el número de especies.
     * @param url que se va a parsear.
     * @return entero con el total de especies.
     */
    public int getNumberOfOccurrences(String url) {
        RootElement root = new RootElement(gbifNS, "gbifResponse");

        Element header = root.getChild(gbifNS, "header");
        Element summary = header.getChild(gbifNS, "summary");

        summary.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                totalMatched = Integer.parseInt(attributes.getValue("totalMatched"));
            }
        });

        try {
            this.url = new URL(url);
            Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());

        } catch (IOException e) {
            e.printStackTrace();

        } catch (SAXException e) {
            e.printStackTrace();
        }

        return totalMatched;
    }

    /**
     * Descarga todos los datos a partir de una url.
     * @param firstUrl cadena de texto con la url.
     * @return ArrayList con las coordenadas.
     */
    public ArrayList<Occurrence> getOccurrences(String firstUrl, ArrayList<Occurrence> o, int c) {
        String toNS = "http://rs.tdwg.org/ontology/voc/TaxonOccurrence#";

        RootElement root = new RootElement(gbifNS, "gbifResponse");

        Element header = root.getChild(gbifNS, "header");
        Element dataProviders = root.getChild(gbifNS, "dataProviders");
        Element dataProvider = dataProviders.getChild(gbifNS, "dataProvider");
        Element dataResources = dataProvider.getChild(gbifNS, "dataResources");
        Element dataResource = dataResources.getChild(gbifNS, "dataResource");
        Element occurrenceRecords = dataResource.getChild(gbifNS, "occurrenceRecords");
        Element taxonOccurrence = occurrenceRecords.getChild(toNS, "TaxonOccurrence");

        cont = 0;
        occurrences = new ArrayList<Occurrence>();
        occurrences = o;
        currentColor = c;

        header.getChild(gbifNS, "nextRequestUrl").setEndTextElementListener(
                new EndTextElementListener() {
                    @Override
                    public void end(String s) {
                        nextUrl = s;
                        nextUrl += "&coordinatestatus=true";
                    }
                });

        taxonOccurrence.setStartElementListener(new StartElementListener() {
            @Override
            public void start(Attributes attributes) {
                currentOccurrence = new Occurrence();
            }
        });

        taxonOccurrence.setEndElementListener(new EndElementListener() {
            @Override
            public void end() {
                if (currentOccurrence.getLatitude() != null && currentOccurrence.getLongitude() != null) {
                    currentOccurrence.setColor(currentColor);
                    occurrences.add(currentOccurrence);
                }
                cont++;
            }
        });


        taxonOccurrence.getChild(toNS, "decimalLatitude").setEndTextElementListener(
                new EndTextElementListener() {
                    @Override
                    public void end(String s) {
                        s = s.replace(",",".");

                        try {
                            currentOccurrence.setLatitude(Float.parseFloat(s));
                        } catch (NumberFormatException ignored) {}

                    }
                });

        taxonOccurrence.getChild(toNS, "decimalLongitude").setEndTextElementListener(
                new EndTextElementListener() {
                    @Override
                    public void end(String s) {
                        s = s.replace(",",".");

                        try {
                            currentOccurrence.setLongitude(Float.parseFloat(s));
                        } catch (NumberFormatException ignored) {}
                    }
                });

        this.nextUrl = firstUrl;

        while (!nextUrl.equals("")) {
            try {
                url = new URL(nextUrl);
                nextUrl = "";
                Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
                progressDialog.setProgress(cont);

            } catch (MalformedURLException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            } catch (SAXException e) {
                e.printStackTrace();
            }
        }

        return occurrences;
    }

    /**
     * Permite obtener un flujo de entrada.
     * @return flujo de entrada de la URL.
     */
    private InputStream getInputStream() throws IOException{
        return url.openConnection().getInputStream();
    }
}
