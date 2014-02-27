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

    @Override
    protected void onStart() {
        super.onStart();

        if(pointList != null){
            drawMarkers(pointList);
        }
    }

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

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

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
     * Tarea asíncrona que se encarga de descargar los datos de GBIF.
     */
    private class GBIFDownloader extends AsyncTask<String,Integer,ArrayList<Occurrence>> {
        private static final String URLDataOcurrence = "http://data.gbif.org/ws/rest/occurrence/list?scientificname=";
        private GBIFDataParser gbifDataParser;

        @Override
        protected ArrayList<Occurrence> doInBackground(String... strings) {
            String scientificName = strings[0];
            scientificName = scientificName.replace(" ", "+");

            gbifDataParser = new GBIFDataParser(URLDataOcurrence + scientificName + "&coordinatestatus=true");

            return gbifDataParser.parse();
        }

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

        @Override
        protected void onCancelled() {
            Toast.makeText(getApplicationContext(), getString(R.string.action_download_canceled),
                    Toast.LENGTH_LONG).show();
        }
    }
}
