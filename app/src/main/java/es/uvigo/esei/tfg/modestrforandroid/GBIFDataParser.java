package es.uvigo.esei.tfg.modestrforandroid;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Permite parsear un fichero xml a través del portal de datos de GBIF.
 * @author Alberto Pardellas Soto
 */
public class GBIFDataParser {
    private URL url;
    private Occurrence currentOccurrence;
    private String nextUrl;
    private ArrayList<Occurrence> occurrences;

    /**
     * Crea un nuevo parser SAX
     * @param url La URL de la petición que se va a realizar.
     */
    public GBIFDataParser (String url) {
        this.nextUrl = url;
        this.occurrences = new ArrayList<Occurrence>();
    }

    /**
     * Define las acciones a realizar para cada evento asociadas a etiquetas xml.
     * @return ArrayList con las coordenadas.
     */
    public ArrayList<Occurrence> parse() {
        final String gbifNS = "http://portal.gbif.org/ws/response/gbif";
        final String toNS = "http://rs.tdwg.org/ontology/voc/TaxonOccurrence#";

        RootElement root = new RootElement(gbifNS, "gbifResponse");

        Element header = root.getChild(gbifNS, "header");
        Element dataProviders = root.getChild(gbifNS, "dataProviders");
        Element dataProvider = dataProviders.getChild(gbifNS, "dataProvider");
        Element dataResources = dataProvider.getChild(gbifNS, "dataResources");
        Element dataResource = dataResources.getChild(gbifNS, "dataResource");
        Element occurrenceRecords = dataResource.getChild(gbifNS, "occurrenceRecords");
        Element taxonOccurrence = occurrenceRecords.getChild(toNS, "TaxonOccurrence");

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
                occurrences.add(currentOccurrence);
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

        while (!nextUrl.equals("")) {
            try {
                url = new URL(nextUrl);
                nextUrl = "";
                Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());

            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return occurrences;
    }

    /**
     * Permite obtener un flujo de entrada.
     * @return flujo de entrada de la URL.
     */
    private InputStream getInputStream() {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
