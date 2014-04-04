package es.uvigo.esei.tfg.mapofspecies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Se encarga de gestionar la BD.
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
            "names_id INTEGER REFERENCES Names (id));";

    private static final String MAP_TABLE_DROP = "DROP TABLE IF EXISTS Maps";
    private static final String NAME_TABLE_DROP = "DROP TABLE IF EXISTS Names";

    /**
     * Constructor por defecto que se encarga de crear el helper de la BD.
     * @param context contexto para crear la BD.
     * @param name nombre de la BD.
     * @param factory para crear objetos cursor, o null para usar los valores por defecto.
     * @param version versión de la BD.
     */
    public MapOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * Llamado cuando se crea la BD.
     * @param sqLiteDatabase helper de la BD.
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MAP_TABLE_CREATE);
        sqLiteDatabase.execSQL(NAME_TABLE_CREATE);
    }

    /**
     * Llamado cuando se actualiza la BD.
     * @param sqLiteDatabase helper de la BD.
     * @param i versión antigua de la BD.
     * @param i2 nueva versión de la BD.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL(MAP_TABLE_DROP);
        sqLiteDatabase.execSQL(NAME_TABLE_DROP);
        sqLiteDatabase.execSQL(MAP_TABLE_CREATE);
        sqLiteDatabase.execSQL(NAME_TABLE_CREATE);
    }
}
