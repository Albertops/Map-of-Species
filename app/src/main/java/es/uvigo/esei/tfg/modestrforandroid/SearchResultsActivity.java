package es.uvigo.esei.tfg.modestrforandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Muestra la lista de sinónimos y permite seleccionar el color de los marcadores.
 * @author Alberto Pardellas Soto
 */
public class SearchResultsActivity extends Activity {
    private ArrayList<Occurrence> pointList;
    private ArrayList<String> data = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private GBIFOccurrencesDownloader gbifDownloader;
    private GBIFDataParser gbifDataParser;
    private boolean[] listChecked;
    private String query;
    private SharedPreferences preferences;
    private int currentColor;
    private int totalOccurrences;

    /**
     * Llamado cuando se crea la actividad por primera vez.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SearchResultsFragment())
                    .commit();

            Bundle bundle = getIntent().getExtras();

            query = bundle.getString("query");
            pointList = bundle.getParcelableArrayList("points");

            GBIFSynonymsDownloader gbifSynonymsDownloader = new GBIFSynonymsDownloader();

            if (checkInternetConnection()) {
                gbifSynonymsDownloader.execute(query);

            }  else {
                Toast.makeText(getApplicationContext(), getString(R.string.connection_failed),
                        Toast.LENGTH_LONG).show();

                Intent intent2 = new Intent(SearchResultsActivity.this, MainActivity.class);
                startActivity(intent2);
            }
        }
    }

    /**
     * Llamado cuando se inicia la interacción con el usuario.
     */
    @Override
    protected void onResume() {
        super.onResume();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getString(R.string.title_search_synonyms) + " \"" + query + "\"");

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, data);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        // Selecciona los elementos del ListView cuando se cambia la orientación de la pantalla
        for (int i = 0; i < data.size(); i++) {
            if (listChecked[i] == true) {
                listView.setItemChecked(i, true);
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(SearchResultsActivity.this);
        currentColor = preferences.getInt("default_color", 0xffff5e56);

        final AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                currentColor = color;
            }

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
            }
        });

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
    }

    /**
     * Inicializa el contenido del menú de opciones.
     * @param menu gestiona los elementos del menú.
     * @return true si el menú se va a mostrar. False si no se va a mostrar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_results, menu);

        return true;
    }

    /**
     * Llamado cada vez que se selecciona un elemento en el menú de opciones.
     * @param item elemendo del menú que se ha seleccionado.
     * @return false para permitir el procesamiento de menú normal, true para consumir aquí.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_accept) {
            ListView listView = (ListView) findViewById(R.id.listView);
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            ArrayList<String> selectedItems = new ArrayList<String>();

            for (int i = 0; i < checked.size(); i++) {
                int position = checked.keyAt(i);

                if (checked.valueAt(i)) {
                    selectedItems.add(adapter.getItem(position));
                }
            }

            gbifDownloader = new GBIFOccurrencesDownloader();
            gbifDownloader.execute(selectedItems);
        }

        if (id == R.id.action_cancel) {
            Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Llamado para guardar el estado de una actividad.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString("query", query);
        savedInstanceState.putStringArrayList("data", data);

        ListView listView = (ListView) findViewById(R.id.listView);

        SparseBooleanArray checked = listView.getCheckedItemPositions();

        listChecked = new boolean[data.size()];

        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);

            if (checked.valueAt(i)) {
                listChecked[position] = true;
            }
        }

        savedInstanceState.putBooleanArray("listChecked", listChecked);
        savedInstanceState.putParcelableArrayList("points", pointList);
    }

    /**
     * Llamado para recuperar el estado de una actividad previamente guardado.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        query = savedInstanceState.getString("query");
        data = savedInstanceState.getStringArrayList("data");
        listChecked = savedInstanceState.getBooleanArray("listChecked");
        pointList = savedInstanceState.getParcelableArrayList("points");
    }

    /**
     * Bloquea la orientación de la pantalla.
     */
    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;

        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * Desbloquea la orientación de la pantalla.
     */
    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * Comprueba si existe conexión a internet.
     * @return True si existe conexión. False en caso contrario.
     */
    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Obtiene las opciones de búsqueda.
     * @return Url con las preferencias seleccionadas.
     */
    private String getUrlPreferences() {
        preferences = PreferenceManager.getDefaultSharedPreferences(SearchResultsActivity.this);
        Set<String> selections = preferences.getStringSet("kind_records", null);

        String url = new String();

        if (selections != null) {
            if (selections.contains("specimen")) {
                url += "&basisofrecordcode=specimen";
            }

            if (selections.contains("observation")) {
                url += "&basisofrecordcode=observation";
            }

            if (selections.contains("living")) {
                url += "&basisofrecordcode=living";
            }

            if (selections.contains("germplasm")) {
                url += "&basisofrecordcode=germplasm";
            }

            if (selections.contains("fossil")) {
                url += "&basisofrecordcode=fossil";
            }

            if (selections.contains("unknown")) {
                url += "&basisofrecordcode=unknown";
            }

        } else {
            url = "&basisofrecordcode=specimen&basisofrecordcode=observation";
        }

        return url;
    }

    /**
     * Fragmento que contiene una lista de sinónimos y opciones de búsqueda.
     */
    public static class SearchResultsFragment extends Fragment {
        public SearchResultsFragment() {
        }

        /**
         * Llamado para que la instancia de un fragmento se pase a la interfaz de usuario.
         * @param inflater usado para inflar la vista en el fragmento.
         * @param container si no es nulo, contiene la vista superior que debe ser conectada.
         * @param savedInstanceState contiene el estado de la actividad.
         * @return devuelve la vista del fragmento, o un valor nulo.
         */
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            return inflater.inflate(R.layout.fragment_search_results, container, false);
        }

        /**
         * Llamado para recuperar el estado de un fragmento.
         * @param savedInstanceState contiene el estado del fragmento.
         */
        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
        }
    }

    /**
     * Se encarga de descargar los datos de GBIF.
     */
    private class GBIFOccurrencesDownloader
            extends AsyncTask<ArrayList<String>, Integer, ArrayList<Occurrence>> {

        String urlDataOccurrence = "http://data.gbif.org/ws/rest/occurrence/list?scientificname=";
        String urlCountOccurrences = "http://data.gbif.org/ws/rest/occurrence/count?scientificname=";
        ProgressDialog progressDialog;

        /**
         * Tareas para hacer en segundo plano.
         * @param arrayLists contiene los sinónimos de la especie.
         * @return ArrayList con las coordenadas de la especie.
         */
        @Override
        protected ArrayList<Occurrence> doInBackground(ArrayList<String>... arrayLists) {
            ArrayList<String> listSynonyms = arrayLists[0];
            ArrayList<Occurrence> newPointList;

            // Construimos la url final
            String scientificName = query.replace(" ", "+");
            urlDataOccurrence += scientificName + "*";
            urlCountOccurrences += scientificName + "*";

            for (String s : listSynonyms) {
                s = s.toLowerCase();
                s = s.replace(" ", "+");
                urlDataOccurrence += "&scientificname=" + s + "*";
                urlCountOccurrences += "&scientificname=" + s + "*";
            }

            urlDataOccurrence += getUrlPreferences();
            urlDataOccurrence += "&mode=processed" +
                    "&format=darwin" +
                    "&startindex=0" +
                    "&maxresults=1000" +
                    "&coordinatestatus=true";

            urlCountOccurrences += getUrlPreferences();
            urlCountOccurrences += "&coordinatestatus=true";

            gbifDataParser = new GBIFDataParser(progressDialog);

            totalOccurrences = gbifDataParser.getNumberOfOccurrences(urlCountOccurrences);
            progressDialog.setMax(totalOccurrences);
            newPointList = gbifDataParser.getOccurrences(urlDataOccurrence, pointList, currentColor);
            publishProgress(totalOccurrences);

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            return newPointList;
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SearchResultsActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getString(R.string.action_download_data_gbif));
            progressDialog.setCancelable(true);
            progressDialog.setProgress(0);

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    GBIFOccurrencesDownloader.this.cancel(true);

                    Toast.makeText(getApplicationContext(), getString(R.string.action_download_canceled),
                            Toast.LENGTH_LONG).show();

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("points", pointList);

                    Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            lockScreenOrientation();
            progressDialog.show();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param result ArrayList con las coordenadas de la especie.
         */
        @Override
        protected void onPostExecute(ArrayList<Occurrence> result) {
            unlockScreenOrientation();

            // Quitamos duplicados
            HashSet<Occurrence> hashSet = new HashSet<Occurrence>();
            hashSet.addAll(result);
            result.clear();
            result.addAll(hashSet);

            Toast.makeText(getApplicationContext(),
                    getString(R.string.total_records) + " " + totalOccurrences,
                    Toast.LENGTH_LONG).show();

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("points", result);

            Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    /**
     * Se encarga de descargar los sinónimos de una especie.
     */
    private class GBIFSynonymsDownloader extends AsyncTask<String, Integer, ArrayList<String>> {
        String urlDataTaxon = "http://data.gbif.org/ws/rest/taxon/list?scientificname=";
        String urlCountOccurrences = "http://data.gbif.org/ws/rest/occurrence/count?scientificname=";

        ProgressDialog progressDialog;

        /**
         * Tareas para hacer en segundo plano.
         * @param strings nombre científico de la especie.
         * @return ArrayList con la lista de sinónimos.
         */
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            String scientificName = strings[0];

            gbifDataParser = new GBIFDataParser(progressDialog);

            urlCountOccurrences += scientificName + "*";
            urlCountOccurrences += getUrlPreferences();
            urlCountOccurrences += "&coordinatestatus=true";

            int totalOccurrences = gbifDataParser.getNumberOfOccurrences(urlCountOccurrences);

            if (totalOccurrences == 0) {
                GBIFSynonymsDownloader.this.cancel(true);
            }

            publishProgress();
            urlDataTaxon += scientificName.replace(" ", "+") + "&rank=species";

            ArrayList<String> listOfSynonyms = gbifDataParser.getListOfSynonyms(urlDataTaxon);

            String synonym = "";

            for (String s : listOfSynonyms) {
                if (s.equalsIgnoreCase(scientificName)) {
                    synonym = s;
                }
            }

            listOfSynonyms.remove(synonym);

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            return listOfSynonyms;
        }

        /**
         * Se ejecuta cuando es llamado el método publishProgress().
         * @param values indican el progreso.
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            progressDialog.setMessage(getString(R.string.action_search_synonyms));
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(SearchResultsActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage(getString(R.string.acion_search_occurrences));
            progressDialog.setCancelable(true);
            progressDialog.setProgress(0);

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    GBIFSynonymsDownloader.this.cancel(true);

                    Toast.makeText(getApplicationContext(), getString(R.string.action_search_canceled),
                            Toast.LENGTH_LONG).show();

                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("points", pointList);

                    Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });

            lockScreenOrientation();
            progressDialog.show();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param result ArrayList con las coordenadas de la especie.
         */
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            unlockScreenOrientation();

            data.clear();
            data.addAll(result);

            listChecked = new boolean[data.size()];

            adapter.notifyDataSetChanged();

            if (data.isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_synonyms_found),
                        Toast.LENGTH_LONG).show();
            }
        }

        /**
         * Llamado si se cancela la tarea.
         */
        @Override
        protected void onCancelled() {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            Toast.makeText(getApplicationContext(), getString(R.string.no_records_found),
                    Toast.LENGTH_LONG).show();

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("points", pointList);

            Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
}
