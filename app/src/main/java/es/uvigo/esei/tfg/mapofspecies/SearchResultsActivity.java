package es.uvigo.esei.tfg.mapofspecies;

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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Muestra la lista de sinónimos y permite seleccionar el color de los marcadores entre otras
 * opciones.
 * @author Alberto Pardellas Soto
 */
public class SearchResultsActivity extends Activity {
    private ArrayList<Occurrence> pointList;
    private ArrayList<String> data = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private GBIFDataParser gbifDataParser;
    private boolean[] listChecked;
    private String query;
    private int currentColor;

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

            if (bundle != null) {
                query = bundle.getString("query");
            }

            pointList = loadOccurencesFromJson();

            GBIFSynonymsDownloader gbifSynonymsDownloader = new GBIFSynonymsDownloader();

            if (checkInternetConnection()) {
                gbifSynonymsDownloader.execute(query);

            }  else {
                if (getApplicationContext() != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.connection_failed),
                            Toast.LENGTH_LONG).show();
                }

                Intent intent2 = new Intent(SearchResultsActivity.this, MainActivity.class);
                startActivity(intent2);
            }
        }
    }

    /**
     * Llamado cuando se muestra la actividad al usuario.
     */
    @Override
    protected void onStart() {
        super.onStart();

        pointList = loadOccurencesFromJson();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(getString(R.string.title_search_synonyms) + " \"" + query + "\"");

        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, data);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        // Selecciona los elementos del ListView cuando se cambia la orientación de la pantalla
        for (int i = 0; i < data.size(); i++) {
            if (listChecked[i]) {
                listView.setItemChecked(i, true);
            }
        }

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(SearchResultsActivity.this);

        currentColor = preferences.getInt("default_color", 0xffff5e56);

        final AmbilWarnaDialog dialog = new AmbilWarnaDialog(
                this,
                currentColor,
                new AmbilWarnaDialog.OnAmbilWarnaListener() {

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
     * @return false para permitir el procesamiento de menú normal, true para procesar aquí.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_accept) {
            ListView listView = (ListView) findViewById(R.id.listView);
            SparseBooleanArray checked = listView.getCheckedItemPositions();
            ArrayList<String> selectedItems = new ArrayList<String>();

            if (checked != null) {
                for (int i = 0; i < checked.size(); i++) {
                    int position = checked.keyAt(i);

                    if (checked.valueAt(i)) {
                        selectedItems.add(adapter.getItem(position));
                    }
                }
            }

            GBIFOccurrencesDownloader gbifDownloader = new GBIFOccurrencesDownloader();
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

        if (checked != null) {
            for (int i = 0; i < checked.size(); i++) {
                int position = checked.keyAt(i);

                if (checked.valueAt(i)) {
                    listChecked[position] = true;
                }
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SearchResultsActivity.this);
        Set<String> selections = preferences.getStringSet("kind_records", null);

        String url = "";

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
     * Guarda una lista de registros en las preferencias con representación Json.
     * @param occurrences ArrayList con las coordenadas.
     */
    private void saveOccurrencesInJson(ArrayList<Occurrence> occurrences) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Occurrence>>(){}.getType();
        String string = gson.toJson(occurrences, type);

        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("point_list", string);
        editor.commit();
    }

    /**
     * Carga las ocurrencias guardadas en las preferencias en una representación Json.
     * @return array con las ocurrencias.
     */
    private ArrayList<Occurrence> loadOccurencesFromJson() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Occurrence>>(){}.getType();

        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String string = sharedPreferences.getString("point_list", null);

        return gson.fromJson(string, type);
    }

    /**
     * Elimina elementos duplicados.
     * @param occurrences ArrayList con las coordenadas.
     * @return ArrayList modificado.
     */
    private ArrayList<Occurrence> deleteDuplicates(ArrayList<Occurrence> occurrences) {
        ArrayList<Occurrence> result = new ArrayList<Occurrence>();
        Set<String> titles = new HashSet<String>();

        for (Occurrence occurrence : occurrences) {
            if (titles.add(String.valueOf(
                    occurrence.getLatitude() +
                            occurrence.getLongitude()))) {

                result.add(occurrence);
            }
        }

        return result;
    }

    /**
     * Elmimina elementos con coordinadas 0.0
     * @param occurrences ArrayList con las coordenadas.
     * @return ArrayList modificado.
     */
    private ArrayList<Occurrence> deleteZeroZeroCoordinates(ArrayList<Occurrence> occurrences) {
        ArrayList<Occurrence> result = new ArrayList<Occurrence>();

        for (Occurrence occurrence : occurrences) {
            if (!occurrence.getLatitude().equals(Float.valueOf(0))
                    && !occurrence.getLongitude().equals(Float.valueOf(0))) {

                result.add(occurrence);
            }
        }

        return result;
    }

    /**
     * Elimina elementos con la misma longitud y latitud.
     * @param occurrences ArrayList con las coordenadas.
     * @return ArrayList modificado.
     */
    private ArrayList<Occurrence> deleteRecordsWithSameLoAndLa(ArrayList<Occurrence> occurrences) {
        ArrayList<Occurrence> result = new ArrayList<Occurrence>();

        for (Occurrence occurrence : occurrences) {
            if (!occurrence.getLatitude().equals(occurrence.getLongitude())) {
                result.add(occurrence);
            }
        }

        return result;
    }

    /**
     * Fragmento que contiene una lista de sinónimos y opciones de búsqueda.
     */
    public static class SearchResultsFragment extends Fragment {
        public SearchResultsFragment() {
        }

        /**
         * Llamado para que la instancia de un fragmento se pase a la vista de la interfaz de
         * usuario.
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
     * Se encarga de descargar los registros de GBIF.
     */
    private class GBIFOccurrencesDownloader
            extends AsyncTask<ArrayList<String>, Integer, ArrayList<Occurrence>> {

        String urlDataOccurrence = "http://data.gbif.org/ws/rest/occurrence/list?scientificname=";
        String urlCountOccurrences =
                "http://data.gbif.org/ws/rest/occurrence/count?scientificname=";

        ProgressDialog progressDialog;
        int totalOccurrences;
        int processedOccurrences;

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
            newPointList = gbifDataParser
                    .getOccurrences(urlDataOccurrence, pointList, currentColor);

            publishProgress(totalOccurrences);

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

                    if(getApplicationContext() != null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.action_download_canceled),
                                Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

            lockScreenOrientation();
            progressDialog.show();
            processedOccurrences = pointList.size();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param result ArrayList con las coordenadas de la especie.
         */
        @Override
        protected void onPostExecute(ArrayList<Occurrence> result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            unlockScreenOrientation();

            result = deleteDuplicates(result);

            CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

            if (checkBox.isChecked()) {
                result = deleteZeroZeroCoordinates(result);
            }

            CheckBox checkBox2 = (CheckBox) findViewById(R.id.checkBox2);

            if (checkBox2.isChecked()) {
                result = deleteRecordsWithSameLoAndLa(result);
            }

            if (getApplicationContext() != null) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.total_records) + " " +
                                totalOccurrences + ". " +
                                (result.size() - processedOccurrences) + " " +
                                getString(R.string.valid_records),
                        Toast.LENGTH_LONG
                ).show();
            }

            saveOccurrencesInJson(result);

            Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }

    /**
     * Se encarga de descargar los sinónimos de una especie.
     */
    private class GBIFSynonymsDownloader extends AsyncTask<String, Integer, ArrayList<String>> {
        String urlDataTaxon = "http://data.gbif.org/ws/rest/taxon/list?scientificname=";
        String urlCountOccurrences =
                "http://data.gbif.org/ws/rest/occurrence/count?scientificname=";

        ProgressDialog progressDialog;
        int totalOccurrences;

        /**
         * Tareas para hacer en segundo plano.
         * @param strings nombre científico de la especie.
         * @return ArrayList con la lista de sinónimos.
         */
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            ArrayList<String> listOfSynonyms = new ArrayList<String>();
            String scientificName = strings[0];
            totalOccurrences = -1;

            gbifDataParser = new GBIFDataParser(progressDialog);

            urlCountOccurrences += scientificName + "*";
            urlCountOccurrences += getUrlPreferences();
            urlCountOccurrences += "&coordinatestatus=true";

            totalOccurrences = gbifDataParser.getNumberOfOccurrences(urlCountOccurrences);

            if (totalOccurrences != 0) {
                publishProgress();
                urlDataTaxon += scientificName.replace(" ", "+") + "&rank=species";

                listOfSynonyms = gbifDataParser.getListOfSynonyms(urlDataTaxon);

                String synonym = "";

                for (String s : listOfSynonyms) {
                    if (s.equalsIgnoreCase(scientificName)) {
                        synonym = s;
                    }
                }

                listOfSynonyms.remove(synonym);
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

                    if (getApplicationContext() != null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.action_search_canceled),
                                Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
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
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            unlockScreenOrientation();

            if (totalOccurrences == 0) {
                if (getApplicationContext() != null){
                    Toast.makeText(getApplicationContext(), getString(R.string.no_records_found),
                            Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(SearchResultsActivity.this, MainActivity.class);
                startActivity(intent);

            } else {
                data.clear();
                data.addAll(result);

                listChecked = new boolean[data.size()];

                adapter.notifyDataSetChanged();

                if (data.isEmpty()) {
                    if (getApplicationContext() != null)
                        Toast.makeText(getApplicationContext(), getString(R.string.no_synonyms_found),
                                Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
