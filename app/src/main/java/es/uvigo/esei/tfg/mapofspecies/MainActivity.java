package es.uvigo.esei.tfg.mapofspecies;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Muestra la pantalla principal.
 * @author Alberto Pardellas Soto
 */
public class MainActivity extends Activity
        implements SaveMapDialog.SaveMapDialogListener,
                   LoadMapDialog.LoadMapDialogListener,
                   DeleteMapDialog.DeleteMapDialogListener {

    private ArrayList<Occurrence> pointList;
    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    /**
     * Llamado cuando se inicia la actividad.
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
        }
    }

    /**
     * Llamado cuando se muestra la actividad al usuario.
     */
    @Override
    protected void onStart() {
        super.onStart();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        TypedArray drawerIcons = getResources().obtainTypedArray(R.array.drawer_icons);
        String[] drawerOptions = getResources().getStringArray(R.array.drawer_options);
        ArrayList<ItemObject> drawerItems = new ArrayList<ItemObject>();

        if (drawerIcons != null) {
            drawerItems.add(new ItemObject(drawerOptions[0], drawerIcons.getResourceId(0, -1)));
            drawerItems.add(new ItemObject(drawerOptions[1], drawerIcons.getResourceId(1, -1)));
            drawerItems.add(new ItemObject(drawerOptions[2], drawerIcons.getResourceId(2, -1)));
            drawerItems.add(new ItemObject(drawerOptions[3], drawerIcons.getResourceId(3, -1)));
            drawerItems.add(new ItemObject(drawerOptions[4], drawerIcons.getResourceId(4, -1)));
        }

        NavigationAdapter navigationAdapter = new NavigationAdapter(this, drawerItems);

        listView = (ListView) findViewById(R.id.left_drawer);
        listView.setAdapter(navigationAdapter);
        listView.setOnItemClickListener(new DrawerItemClickListener());

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer_dark,
                R.string.drawer_open,
                R.string.drawer_close);

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow_dark, GravityCompat.START);

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        actionBarDrawerToggle.syncState();

        if (getActionBar() != null) {
            getActionBar().setIcon(R.drawable.ic_action_map_dark);
        }

        GoogleMap googleMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map)).getMap();

        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        Boolean showWelcomeDialog = sharedPreferences.getBoolean("show_welcome_dialog", true);

        if (showWelcomeDialog) {
            WelcomeDialog welcomeDialog = new WelcomeDialog();
            welcomeDialog.setCancelable(false);
            welcomeDialog.show(getFragmentManager(), "");
        }

        SharedPreferences defaultSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(MainActivity.this);

        String mapType = defaultSharedPreferences.getString("map_type", "terrain");

        if (googleMap != null) {
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

            RenderingMap prepareMarkerOptions = new RenderingMap();
            prepareMarkerOptions.execute(pointList);
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

        final SearchView searchView = (SearchView)
                menu.findItem(R.id.action_search).getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    Bundle bundle = new Bundle();
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
        }

        return true;
    }

    /**
     * Llamado cada vez que se selecciona un elemento en el menú de opciones.
     * @param item elemendo del menú que se ha seleccionado.
     * @return false para permitir el procesamiento de menú normal, true para procesar aquí.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Llamado para guardar el estado de una actividad.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            savedInstanceState.putParcelableArrayList("points", pointList);
        }
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
     * Llamado cuando se hace clic en el botón aceptar en el diálogo guardar mapa.
     * @param mapName nombre del mapa que el usuario ha introducido.
     */
    @Override
    public void onDialogPositiveClickSaveMap(String mapName) {
        SaveDataToBD saveDataToBD = new SaveDataToBD();
        saveDataToBD.execute(mapName);
    }

    /**
     * Llamado cuando se hace clic en el botón aceptar en el diálogo cargar mapa.
     * @param mapSelected nombre del mapa seleccionado.
     */
    @Override
    public void onDialogPositiveClickLoadMap(String mapSelected) {
        MapOpenHelper mapOpenHelper = new MapOpenHelper(this, "DBMaps", null, 1);
        SQLiteDatabase sqLiteDatabase = mapOpenHelper.getReadableDatabase();

        String[] fields = new String[] {"_id"};
        String[] args = new String[] {mapSelected};
        Cursor cursor;

        if (sqLiteDatabase != null) {
            cursor = sqLiteDatabase.query("Names", fields, "name=?", args, null, null, null);

            if (cursor.moveToFirst()) {
                long nameId = cursor.getLong(0);

                LoadDataFromBD loadDataFromBD = new LoadDataFromBD();
                loadDataFromBD.execute(String.valueOf(nameId));

                GoogleMap googleMap = ((MapFragment) getFragmentManager()
                        .findFragmentById(R.id.map)).getMap();

                if (googleMap != null) {
                    googleMap.clear();
                }
            }

            sqLiteDatabase.close();
        }
    }

    /**
     * Llamado cuando se hace clic en el botón aceptar en el diálogo borrar mapa.
     * @param mapSelected nombre del mapa seleccionado.
     */
    @Override
    public void onDialogPositiveClickDeleteMap(String mapSelected) {
        MapOpenHelper mapOpenHelper = new MapOpenHelper(this, "DBMaps", null, 1);
        SQLiteDatabase sqLiteDatabase = mapOpenHelper.getReadableDatabase();

        String[] fields = new String[] {"_id"};
        String[] args = new String[] {mapSelected};
        Cursor cursor;

        if (sqLiteDatabase != null) {
            cursor = sqLiteDatabase.query("Names", fields, "name=?", args, null, null, null);

            if (cursor.moveToFirst()) {
                long nameId = cursor.getLong(0);

                DeleteDataFromBD deleteDataFromBD = new DeleteDataFromBD();
                deleteDataFromBD.execute(String.valueOf(nameId));

                sqLiteDatabase.close();
            }
        }
    }

    /**
     * Llamado por el sistema cuando la configuración del dispositivo cambia, mientras que la
     * actividad sigue en marcha.
     * @param newConfig la nueva configuración del dispositivo.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        actionBarDrawerToggle.onConfigurationChanged(newConfig);
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
     * Guarda una lista de registros en las preferencias con representación Json.
     * @param occurrences ArrayList con las coordenadas.
     */
    private void saveDataToJson(ArrayList<Occurrence> occurrences) {
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
    private ArrayList<Occurrence> loadDataFromJson() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Occurrence>>(){}.getType();

        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String string = sharedPreferences.getString("point_list", null);

        return gson.fromJson(string, type);
    }

    /**
     * Ejecuta diferentes acciones según el elemento seleccionado en el DrawerLayout.
     * @param position representa la posición del elemento.
     */
    private void selectItem(int position) {
        switch (position) {
            case 0:
                SaveMapDialog saveMapDialog = new SaveMapDialog();
                saveMapDialog.show(getFragmentManager(), "");
                break;

            case 1:
                MapOpenHelper mapOpenHelper = new MapOpenHelper(this, "DBMaps", null, 1);
                SQLiteDatabase sqLiteDatabase = mapOpenHelper.getWritableDatabase();

                String[] fields = new String[] {"name"};
                Cursor cursor;

                if (sqLiteDatabase != null) {
                    cursor = sqLiteDatabase.query("Names", fields, null, null, null, null, null);

                    ArrayList<String> results = new ArrayList<String>();

                    if (cursor.moveToFirst()) {
                        do {
                            results.add(cursor.getString(0));

                        } while (cursor.moveToNext());
                    }


                    if (!results.isEmpty()) {
                        LoadMapDialog loadMapDialog = new LoadMapDialog(results);
                        loadMapDialog.show(getFragmentManager(), "");

                    } else {
                        Toast.makeText(this, getString(R.string.no_data), Toast.LENGTH_LONG).show();
                    }

                    sqLiteDatabase.close();
                }
                break;

            case 2:
                mapOpenHelper = new MapOpenHelper(this, "DBMaps", null, 1);
                sqLiteDatabase = mapOpenHelper.getWritableDatabase();

                fields = new String[] {"name"};

                if (sqLiteDatabase != null) {
                    cursor = sqLiteDatabase.query("Names", fields, null, null, null, null, null);

                    ArrayList<String> results = new ArrayList<String>();

                    if (cursor.moveToFirst()) {
                        do {
                            results.add(cursor.getString(0));

                        } while (cursor.moveToNext());
                    }

                    if (!results.isEmpty()) {
                        DeleteMapDialog deleteMapDialog = new DeleteMapDialog(results);
                        deleteMapDialog.show(getFragmentManager(), "");

                    } else {
                        Toast.makeText(this, getString(R.string.no_data), Toast.LENGTH_LONG).show();
                    }

                    sqLiteDatabase.close();
                }
                break;

            case 3:
                GoogleMap googleMap = ((MapFragment) getFragmentManager()
                        .findFragmentById(R.id.map)).getMap();

                if (googleMap != null) {
                    googleMap.clear();
                }

                if (pointList != null) {
                    pointList.clear();
                }

                saveDataToJson(pointList);
                break;

            case 4:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
        }

        drawerLayout.closeDrawer(listView);
    }

    /**
     * Fragmento que contiene una vista del mapa.
     */
    public static class GoogleMapFragment extends Fragment {

        public GoogleMapFragment() {
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

            return inflater.inflate(R.layout.fragment_main, container, false);
        }
    }

    /**
     * Se encarga de guardar los datos en una BD.
     */
    private class SaveDataToBD extends AsyncTask<String, Integer, Boolean> {
        private static final String DATABASE_ERROR = "DATABASE_ERROR";
        private static final String NO_MARKERS = "NO_MARKERS";

        ProgressDialog progressDialog;
        String error = "";

        /**
         * Tareas para hacer en segundo plano.
         * @param strings nombre de la especie.
         * @return true si se ha podido insertar. False en caso contrario.
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            MapOpenHelper mapOpenHelper = new MapOpenHelper(getBaseContext(), "DBMaps", null, 1);
            SQLiteDatabase sqLiteDatabase = mapOpenHelper.getWritableDatabase();

            if (sqLiteDatabase != null) {
                if (pointList != null && !pointList.isEmpty()) {
                    ContentValues nameValue = new ContentValues();
                    nameValue.put("name", strings[0]);
                    long nameId = sqLiteDatabase.insert("Names", null, nameValue);

                    for (Occurrence occurrence : pointList) {
                        ContentValues mapValues = new ContentValues();

                        mapValues.put("latitude", occurrence.getLatitude());
                        mapValues.put("longitude", occurrence.getLongitude());
                        mapValues.put("color", occurrence.getColor());
                        mapValues.put("names_id", nameId);

                        sqLiteDatabase.insert("Maps", null, mapValues);
                    }

                    sqLiteDatabase.close();

                }
                else {
                    error = NO_MARKERS;
                    return false;
                }
            } else {
                error = DATABASE_ERROR;
                return false;
            }

            return true;
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_save_data));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param result Boolean con el resultado de la tarea.
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (getApplicationContext() != null) {
                if (error.equals(DATABASE_ERROR)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_saving_data),
                            Toast.LENGTH_LONG).show();

                } else if (error.equals(NO_MARKERS)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.no_markers),
                            Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.action_data_saved),
                            Toast.LENGTH_LONG).show();
                }
            }

            unlockScreenOrientation();
        }
    }

    /**
     * Se encarga de cargar los datos en la BD.
     */
    private class LoadDataFromBD extends AsyncTask<String, Integer, ArrayList<Occurrence>> {
        ProgressDialog progressDialog;

        /**
         * Tareas para hacer en segundo plano.
         * @param strings nombre de la especie.
         * @return ArrayList con las coordenadas de la especie.
         */
        @Override
        protected ArrayList<Occurrence> doInBackground(String... strings) {
            MapOpenHelper mapOpenHelper = new MapOpenHelper(getBaseContext(), "DBMaps", null, 1);
            SQLiteDatabase sqLiteDatabase = mapOpenHelper.getWritableDatabase();
            ArrayList<Occurrence> results = new ArrayList<Occurrence>();

            String[] fields = new String[] {"latitude", "longitude", "color"};
            String[] args = new String[] {strings[0]};
            Cursor cursor;

            if (sqLiteDatabase != null) {
                cursor = sqLiteDatabase.query("Maps", fields, "names_id=?", args, null, null, null);


                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            Occurrence occurrence = new Occurrence();
                            occurrence.setLatitude(cursor.getFloat(0));
                            occurrence.setLongitude(cursor.getFloat(1));
                            occurrence.setColor(cursor.getInt(2));

                            results.add(occurrence);

                        } while (cursor.moveToNext());
                    }
                }

                sqLiteDatabase.close();
            }

            return results;
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_load_data));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param results ArrayList con las coordenadas de la especie.
         */
        @Override
        protected void onPostExecute(ArrayList<Occurrence> results) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            unlockScreenOrientation();
            RenderingMap prepareMarkerOptions = new RenderingMap();
            prepareMarkerOptions.execute(results);
        }
    }

    /**
     * Se encarga de borrar datos de la BD.
     */
    private class DeleteDataFromBD extends AsyncTask<String, Integer, Boolean> {
        ProgressDialog progressDialog;

        /**
         * Tareas para hacer en segundo plano.
         * @param strings nombre de la especie.
         * @return true si se ha podido borrar. False en caso contrario.
         */
        @Override
        protected Boolean doInBackground(String... strings) {
            MapOpenHelper mapOpenHelper = new MapOpenHelper(getBaseContext(), "DBMaps", null, 1);
            SQLiteDatabase sqLiteDatabase = mapOpenHelper.getWritableDatabase();

            String[] args = new String[] {strings[0]};

            if (sqLiteDatabase != null) {
                sqLiteDatabase.delete("Names", "_id=?", args);
                sqLiteDatabase.delete("Maps", "names_id=?", args);

                sqLiteDatabase.close();
            }
            else {
                return false;
            }

            return true;
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_delete_data));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param result resultado de la tarea.
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            if (getApplicationContext() != null) {
                Toast.makeText(getApplicationContext(), getString(R.string.action_data_deleted),
                        Toast.LENGTH_LONG).show();
            }

            unlockScreenOrientation();
        }
    }

    /**
     * Se encarga de preparar y dibujar los marcadores en el mapa.
     */
    private class RenderingMap extends AsyncTask<ArrayList<Occurrence>, Integer, ArrayList<MarkerOptions>> {
        ProgressDialog progressDialog;

        /**
         * Tareas para hacer en segundo plano.
         * @param arrayLists ArrayList con las coordenadas de la especie.
         * @return ArrayList con las opciones de los marcadores.
         */
        @Override
        protected ArrayList<MarkerOptions> doInBackground(ArrayList<Occurrence>... arrayLists) {
            ArrayList<MarkerOptions> result = new ArrayList<MarkerOptions>();
            ArrayList<Occurrence> occurrences = arrayLists[0];

            if (occurrences == null) {
                occurrences = loadDataFromJson();
            }

            if (occurrences == null) {
                occurrences = new ArrayList<Occurrence>();
            }

            for(Occurrence occurrence : occurrences) {
                int currentColor = occurrence.getColor();

                float[] hsv = new float[3];
                Color.RGBToHSV(
                        Color.red(currentColor),
                        Color.green(currentColor),
                        Color.blue(currentColor), hsv);

                result.add(new MarkerOptions()
                        .position(new LatLng(
                                occurrence.getLatitude(),
                                occurrence.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(hsv[0])));

            }

            saveDataToJson(occurrences);

            pointList = occurrences;

            return result;
        }

        /**
         * Llamado antes de ejecutar la tarea.
         */
        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_rendering_map));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

        /**
         * Llamado después de ejecutar la tarea.
         * @param result ArrayList con las opciones de los marcadores.
         */
        @Override
        protected void onPostExecute(ArrayList<MarkerOptions> result) {
            GoogleMap googleMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();

            if (googleMap != null) {
                for (MarkerOptions markerOptions : result) {
                    googleMap.addMarker(markerOptions);
                }
            }

            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            unlockScreenOrientation();
        }
    }

    /**
     * Listener para el ListView asociado al DrawerLayout.
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }
}
