package es.uvigo.esei.tfg.modestrforandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Muestra la pantalla principal.
 * @author Alberto Pardellas Soto
 */
public class MainActivity extends Activity {
    private GoogleMap googleMap;
    private ProgressDialog progressDialog;
    private GBIFDownloader gbifDownloader;
    private ArrayList<Occurrence> pointList;

    /**
     * Llamado cuando se crea la actividad por primera vez.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new GoogleMapFragment())
                    .commit();

        } else if (savedInstanceState.containsKey("points")) {
            pointList = savedInstanceState.getParcelableArrayList("points");
        }
    }

    /**
     * Llamado cuando la actividad es visible para el usuario.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if(pointList != null){
            drawMarkers(pointList);
        }
    }

    /**
     * Inicializa el contenido del menú de opciones.
     * @param menu gestiona los elementos del menú.
     * @return true si el menú se va a mostrar. False si no se va a mostrar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        /*
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        */

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /**
             * Llamado cuando el usuario envía la consulta.
             * @param s cadena de texto con la consulta.
             * @return true si la consulta ha sido manejada por el oyente, false para que el
             * SearchView realice la acción predeterminada.
             */
            @Override
            public boolean onQueryTextSubmit(String s) {
                progressDialog = new ProgressDialog(MainActivity.this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage(getString(R.string.action_download_data_gbif));
                progressDialog.setCancelable(true);

                gbifDownloader = new GBIFDownloader();
                gbifDownloader.execute(s);

                return false;
            }

            /**
             * Llamado cuando cambia el texto de la consulta.
             * @param s cadena de texto con la consulta.
             * @return false si el SearchView debe realizar la acción predeterminada de mostrar
             * alguna sugerencia, si está disponible, true si la acción fue manejada por el
             * oyente.
             */
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

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

        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (id == R.id.action_map_type_normal) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        if (id == R.id.action_map_type_hybrid) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        if (id == R.id.action_map_type_satellite) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        if (id == R.id.action_map_type_terrain) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

        if (id == R.id.action_map_clear) {
            googleMap.clear();

            if (pointList != null) {
                pointList.clear();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Llamado para recuperar el estado de una actividad.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("points", pointList);

        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Dibuja marcadores en el mapa.
     * @param list lista con las coordenadas.
     */
    public void drawMarkers(ArrayList<Occurrence> list) {
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (googleMap != null) {
            LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;

            for(Occurrence occurrence : list) {
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                occurrence.getLatitude(),
                                occurrence.getLongitude())));
            }
            pointList = list;
        }
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
     * Fragmento que contiene una vista del mapa.
     */
    public static class GoogleMapFragment extends Fragment {

        public GoogleMapFragment() {
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            GoogleMap googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

            return rootView;
        }
    }

    /**
     * Se encarga de descargar los datos de GBIF.
     */
    private class GBIFDownloader extends AsyncTask<String,Integer,ArrayList<Occurrence>> {
        private static final String URLDataOcurrence = "http://data.gbif.org/ws/rest/occurrence/list?scientificname=";
        private GBIFDataParser gbifDataParser;

        /**
         * Tareas para hacer en segundo plano.
         * @param strings cadena de texto con el nombre científico de la especie.
         * @return ArrayList con las coordenadas de la especie.
         */
        @Override
        protected ArrayList<Occurrence> doInBackground(String... strings) {
            String scientificName = strings[0];
            scientificName = scientificName.replace(" ", "+");

            gbifDataParser = new GBIFDataParser(URLDataOcurrence + scientificName + "&coordinatestatus=true");

            return gbifDataParser.parse();
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    GBIFDownloader.this.cancel(true);
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
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            unlockScreenOrientation();
            Toast.makeText(getApplicationContext(), getString(R.string.total_records) + " " + result.size(),
                    Toast.LENGTH_LONG).show();

            // Quitamos duplicados
            HashSet<Occurrence> hashSet = new HashSet<Occurrence>();
            hashSet.addAll(result);
            result.clear();
            result.addAll(hashSet);

            drawMarkers(result);
        }

        /**
         * Llamado cuando se cancela la tarea.
         */
        @Override
        protected void onCancelled() {
            Toast.makeText(getApplicationContext(), getString(R.string.action_download_canceled),
                    Toast.LENGTH_LONG).show();
        }
    }
}
