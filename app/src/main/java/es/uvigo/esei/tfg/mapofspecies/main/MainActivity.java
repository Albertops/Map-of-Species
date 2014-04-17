package es.uvigo.esei.tfg.mapofspecies.main;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.SearchManager;
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
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import es.uvigo.esei.tfg.mapofspecies.utils.ConvexHull;
import es.uvigo.esei.tfg.mapofspecies.ui.DeleteMapDialog;
import es.uvigo.esei.tfg.mapofspecies.data.ItemObject;
import es.uvigo.esei.tfg.mapofspecies.data.ListViewAdapter;
import es.uvigo.esei.tfg.mapofspecies.ui.LoadMapDialog;
import es.uvigo.esei.tfg.mapofspecies.data.Occurrence;
import es.uvigo.esei.tfg.mapofspecies.R;
import es.uvigo.esei.tfg.mapofspecies.ui.SaveMapDialog;
import es.uvigo.esei.tfg.mapofspecies.ui.SelectSpeciesDialog;
import es.uvigo.esei.tfg.mapofspecies.ui.WelcomeDialog;
import es.uvigo.esei.tfg.mapofspecies.database.MapOpenHelper;

/**
 * Muestra la pantalla principal.
 * @author Alberto Pardellas Soto
 */
public class MainActivity extends Activity
        implements SaveMapDialog.SaveMapDialogListener,
        LoadMapDialog.LoadMapDialogListener,
        DeleteMapDialog.DeleteMapDialogListener,
        SelectSpeciesDialog.SelectSpeciesDialogListener {

    private ArrayList<Occurrence> pointList;
    private ArrayList<PolygonOptions> polygonOptionsList;
    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private LatLng centroid;

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
            drawerItems.add(new ItemObject(drawerOptions[5], drawerIcons.getResourceId(5, -1)));
        }

        ListViewAdapter navigationAdapter = new ListViewAdapter(this, drawerItems, R.layout.item);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            savedInstanceState.putParcelableArrayList("points", pointList);
            savedInstanceState.putParcelableArrayList("polygon_options", polygonOptionsList);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        pointList = savedInstanceState.getParcelableArrayList("points");
        polygonOptionsList = savedInstanceState.getParcelableArrayList("polygon_options");
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
     * Llamado cuando se hace clic en el botón aceptar en el diálogo para calcular la envolvente
     * convexa.
     * @param nameSelected nombre de la especie.
     * @param colorMarker color del marcador.
     * @param alphaChannel valor para el canal alfa.
     */
    @Override
    public void onDialogPositiveClickSelectSpecies(
            String nameSelected, Boolean colorMarker, Integer alphaChannel) {

        if (nameSelected != null) {
            GoogleMap googleMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();

            ArrayList<Occurrence> specie = new ArrayList<Occurrence>();

            for (Occurrence occurrence : pointList) {
                if (occurrence.getName().equals(nameSelected)) {
                    specie.add(occurrence);
                }
            }

            ArrayList<Occurrence> convexHull = ConvexHull.quickHull(specie);

            float[] hsv = new float[3];

            if (colorMarker) {
                SharedPreferences sharedPreferences =
                        getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                hsv[0] = sharedPreferences.getInt(
                        "convex_hull_color", (int) BitmapDescriptorFactory.HUE_YELLOW);

                hsv[1] = 1;
                hsv[2] = 1;

            } else {
                hsv[0] = 0;
                hsv[1] = 0;
                hsv[2] = 0;
            }

            int fillColor = Color.HSVToColor(alphaChannel, hsv);

            PolygonOptions polygonOptions = new PolygonOptions();
            ArrayList<LatLng> points = new ArrayList<LatLng>();

            for (Occurrence occurrence : convexHull) {
                points.add(new LatLng(occurrence.getLatitude(), occurrence.getLongitude()));
            }

            centroid = getCenter(points);

            Collections.sort(points, new PolygonComparator());

            polygonOptions.addAll(points);
            polygonOptions.strokeWidth(0);
            polygonOptions.fillColor(fillColor);

            if (polygonOptionsList == null) {
                polygonOptionsList = new ArrayList<PolygonOptions>();
            }

            polygonOptionsList.add(polygonOptions);

            savePolygonsToJson(polygonOptionsList);

            googleMap.addPolygon(polygonOptions);
        }
    }

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
     * Guarda una lista de polígonos en una representación Json.
     * @param list lista con los polígonos.
     */
    private void savePolygonsToJson(ArrayList<PolygonOptions> list) {
        Gson gson = new Gson();
        Type type = type = new TypeToken<ArrayList<PolygonOptions>>(){}.getType();
        String string = gson.toJson(list, type);

        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("polygon_options", string);
        editor.commit();
    }

    /**
     * Carga una lista de polígonos en una representación Json.
     * @return lista con los polígonos.
     */
    private ArrayList<PolygonOptions> loadPolygonsFromJson() {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<PolygonOptions>>(){}.getType();

        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);

        String string = sharedPreferences.getString("polygon_options", null);

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

                if (polygonOptionsList != null) {
                    polygonOptionsList.clear();
                }

                saveDataToJson(pointList);
                savePolygonsToJson(polygonOptionsList);

                break;

            case 4:
                if (pointList != null && !pointList.isEmpty()) {
                    HashSet<String> hashSet = new HashSet<String>();

                    for (Occurrence occurrence : pointList) {
                        hashSet.add(occurrence.getName());
                    }

                    ArrayList<String> names = new ArrayList<String>();
                    names.addAll(hashSet);

                    SelectSpeciesDialog selectSpeciesDialog = new SelectSpeciesDialog(names);
                    selectSpeciesDialog.show(getFragmentManager(), "");

                } else {
                    if (getApplicationContext() != null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_markers),
                                Toast.LENGTH_LONG).show();
                    }
                }

                break;

            case 5:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                break;
        }

        drawerLayout.closeDrawer(listView);
    }

    private LatLng getCenter(ArrayList<LatLng> points) {
        Double latitude = (double) 0;
        Double longutude = (double) 0;

        for (LatLng point : points) {
            latitude += point.latitude;
            longutude += point.longitude;
        }

        int totalPoints = points.size();

        return new LatLng(latitude/totalPoints, longutude/totalPoints);
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
                        mapValues.put("name", occurrence.getName());
                        mapValues.put("names_id", nameId);

                        sqLiteDatabase.insert("Maps", null, mapValues);
                    }

                    Gson gson = new Gson();
                    Type type = type = new TypeToken<ArrayList<PolygonOptions>>(){}.getType();
                    String string = gson.toJson(polygonOptionsList, type);

                    ContentValues polygonOptionsValues = new ContentValues();
                    polygonOptionsValues.put("string_array", string);
                    polygonOptionsValues.put("convex_hull_id", nameId);

                    sqLiteDatabase.insert("Convex_Hull", null, polygonOptionsValues);
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

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_save_data));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

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

        @Override
        protected ArrayList<Occurrence> doInBackground(String... strings) {
            MapOpenHelper mapOpenHelper = new MapOpenHelper(getBaseContext(), "DBMaps", null, 1);
            SQLiteDatabase sqLiteDatabase = mapOpenHelper.getWritableDatabase();
            ArrayList<Occurrence> results = new ArrayList<Occurrence>();

            String[] fields = new String[] {"latitude", "longitude", "color", "name"};
            String[] args = new String[] {strings[0]};
            Cursor cursor;

            if (sqLiteDatabase != null) {
                cursor = sqLiteDatabase.query("Maps", fields, "names_id=?", args, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            Occurrence occurrence = new Occurrence();
                            occurrence.setLatitude(cursor.getDouble(0));
                            occurrence.setLongitude(cursor.getDouble(1));
                            occurrence.setColor(cursor.getInt(2));
                            occurrence.setName(cursor.getString(3));

                            results.add(occurrence);

                        } while (cursor.moveToNext());
                    }
                }

                fields = new String[] {"string_array"};
                cursor = sqLiteDatabase.query("Convex_Hull", fields, "convex_hull_id=?", args, null, null, null);

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<PolygonOptions>>(){}.getType();

                        String string = cursor.getString(0);

                        polygonOptionsList = gson.fromJson(string, type);
                    }
                }

                sqLiteDatabase.close();
            }

            return results;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_load_data));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

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

        @Override
        protected Boolean doInBackground(String... strings) {
            MapOpenHelper mapOpenHelper = new MapOpenHelper(getBaseContext(), "DBMaps", null, 1);
            SQLiteDatabase sqLiteDatabase = mapOpenHelper.getWritableDatabase();

            String[] args = new String[] {strings[0]};

            if (sqLiteDatabase != null) {
                sqLiteDatabase.delete("Names", "_id=?", args);
                sqLiteDatabase.delete("Maps", "names_id=?", args);
                sqLiteDatabase.delete("Convex_Hull", "convex_hull_id=?", args);

                sqLiteDatabase.close();
            }
            else {
                return false;
            }

            return true;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_delete_data));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

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
                result.add(new MarkerOptions()
                        .position(new LatLng(
                                occurrence.getLatitude(),
                                occurrence.getLongitude()))
                        .icon(BitmapDescriptorFactory.defaultMarker(occurrence.getColor())));
            }

            saveDataToJson(occurrences);
            pointList = occurrences;

            if (polygonOptionsList == null) {
                polygonOptionsList = loadPolygonsFromJson();

                if (polygonOptionsList == null) {
                    polygonOptionsList = new ArrayList<PolygonOptions>();
                }
            }

            return result;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.action_rendering_map));
            progressDialog.setCancelable(false);

            lockScreenOrientation();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(ArrayList<MarkerOptions> result) {
            GoogleMap googleMap = ((MapFragment) getFragmentManager()
                    .findFragmentById(R.id.map)).getMap();

            if (googleMap != null) {
                googleMap.clear();

                for (MarkerOptions markerOptions : result) {
                    googleMap.addMarker(markerOptions);
                }

                for (PolygonOptions polygonOptions : polygonOptionsList) {
                    googleMap.addPolygon(polygonOptions);
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

    public class PolygonComparator implements Comparator<LatLng> {
        @Override
        public int compare(LatLng o1, LatLng o2) {
            double angle1 = Math.atan2(
                    o1.latitude - centroid.latitude, o1.longitude - centroid.longitude);

            double angle2 = Math.atan2(
                    o2.latitude - centroid.latitude, o2.longitude - centroid.longitude);

            if(angle1 < angle2) return 1;
            else if (angle2 > angle1) return -1;
            return 0;
        }
    }
}
