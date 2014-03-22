package es.uvigo.esei.tfg.modestrforandroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Muestra la pantalla principal.
 * @author Alberto Pardellas Soto
 */
public class MainActivity extends Activity {
    private GoogleMap googleMap;
    private ArrayList<Occurrence> pointList;
    private int currentColor;

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

            Bundle bundle = this.getIntent().getExtras();

            if (bundle != null) {
                pointList = bundle.getParcelableArrayList("points");
            }
        }
    }

    /**
     * Llamado cuando se inicia la interacción con el usuario.
     */
    @Override
    protected void onResume() {
        super.onResume();

        GoogleMap googleMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);

        String mapType = preferences.getString("map_type", "terrain");


        if (mapType.equals("normal")) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        }

        if (mapType.equals("hybrid")) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }

        if (mapType.equals("satellite")) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        if (mapType.equals("terrain")) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        }

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
        getMenuInflater().inflate(R.menu.main, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (pointList == null) {
                    pointList = new ArrayList<Occurrence>();
                }

                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("points", pointList);
                bundle.putString("query", s);

                Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
                intent.putExtras(bundle);

                searchView.clearFocus();

                startActivity(intent);

                return false;
            }

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

        if (id == R.id.action_map_clear) {
            if (googleMap != null) {
                googleMap.clear();
            }

            if (pointList != null) {
                pointList.clear();
            }
        }

        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
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

        savedInstanceState.putParcelableArrayList("points", pointList);

    }

    /**
     * Llamado para recuperar el estado de una actividad previamente guardado.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pointList = savedInstanceState.getParcelableArrayList("points");
    }

    /**
     * Dibuja marcadores en el mapa.
     * @param list lista con las coordenadas.
     */
    protected void drawMarkers(ArrayList<Occurrence> list) {
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        if (googleMap != null) {
            for(Occurrence occurrence : list) {
                currentColor = occurrence.getColor();

                float[] hsv = new float[3];
                Color.RGBToHSV(Color.red(currentColor), Color.green(currentColor), Color.blue(currentColor), hsv);

                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(
                                occurrence.getLatitude(),
                                occurrence.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(hsv[0])));


            }
            pointList = list;
        }
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

            return rootView;
        }
    }
}
