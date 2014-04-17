package es.uvigo.esei.tfg.mapofspecies.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Se encarga de gestionar la BD.
 * @author Alberto Pardellas Soto
 */
public class MapOpenHelper extends SQLiteOpenHelper {
    private static final String MAP_TABLE_CREATE = "CREATE TABLE Names " +
            "(_id INTEGER PRIMARY KEY  AUTOINCREMENT, " +
            "name TEXT);";

    private static final String NAME_TABLE_CREATE = "CREATE TABLE Maps " +
            "(_id INTEGER PRIMARY KEY  AUTOINCREMENT, " +
            "latitude REAL, " +
            "longitude REAL, " +
            "color INTEGER, " +
            "name TEXT," +
            "names_id INTEGER REFERENCES Names (id));";

    private static final String CONVEX_HULL_TABLE_CREATE = "CREATE TABLE Convex_Hull " +
            "(_id INTEGER PRIMARY KEY  AUTOINCREMENT, " +
            "string_array TEXT," +
            "convex_hull_id INTEGER REFERENCES Names (id));";

    private static final String MAP_TABLE_DROP = "DROP TABLE IF EXISTS Maps";
    private static final String NAME_TABLE_DROP = "DROP TABLE IF EXISTS Names";
    private static final String CONVEX_HULL_TABLE_DROP = "DROP TABLE IF EXISTS Convex_Hull";

    public MapOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MAP_TABLE_CREATE);
        sqLiteDatabase.execSQL(NAME_TABLE_CREATE);
        sqLiteDatabase.execSQL(CONVEX_HULL_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(MAP_TABLE_DROP);
        sqLiteDatabase.execSQL(NAME_TABLE_DROP);
        sqLiteDatabase.execSQL(CONVEX_HULL_TABLE_DROP);
        sqLiteDatabase.execSQL(MAP_TABLE_CREATE);
        sqLiteDatabase.execSQL(NAME_TABLE_CREATE);
        sqLiteDatabase.execSQL(CONVEX_HULL_TABLE_CREATE);
    }
}
