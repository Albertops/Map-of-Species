package es.uvigo.esei.tfg.modestrforandroid;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Representa un registro del portal de datos de GBIF.
 * @author Alberto Pardellas Soto
 */
public class Occurrence implements Parcelable{
    private Float latitude;
    private Float longitude;
    private int color;

    /**
     * Crea un nuevo objeto de registro.
     */
    public Occurrence() {
    }

    /**
     * Lee los objetos de un Parcel.
     * @param in Parcel a leer.
     */
    private Occurrence(Parcel in) {
        latitude = in.readFloat();
        longitude = in.readFloat();
        color = in.readInt();
    }

    /**
     * Interfaz que proporciona un campo creador público que genera instancias de la clase
     * Parcelable desde un Parcel.
     */
    public static final Parcelable.Creator<Occurrence> CREATOR
            = new Parcelable.Creator<Occurrence>() {

        /**
         * Crea una nueva instancia de la clase Parcelable.
         * @param in paquete para leer los datos del objeto.
         * @return nueva instancia de la clase Parcelable.
         */
        public Occurrence createFromParcel(Parcel in) {
            return new Occurrence(in);
        }

        /**
         * Crea un nuevo array de la clase Parcelable.
         * @param size tamaño del array.
         * @return array de la clase Parcelable.
         */
        public Occurrence[] newArray(int size) {
            return new Occurrence[size];
        }
    };

    /**
     * Devuelve la latitud.
     * @return latitud actual.
     */
    public Float getLatitude() {
        return latitude;
    }

    /**
     * Devuelve la logitud.
     * @return longitud actual.
     */
    public Float getLongitude() {
        return longitude;
    }

    /**
     * Devuelve el color.
     * @return color actual.
     */
    public int getColor() {
        return color;
    }

    /**
     * Permite asignar la latitud.
     * @param latitude representa la latitud.
     */
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    /**
     * Permite asignar la longitud.
     * @param longitude representa la longitud.
     */
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    /**
     * Permite asignar un color.
     * @param color representa el color.
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Describe los tipos de objetos especiales en representación marshalled.
     * @return una máscara de bits que indica el conjunto de tipos de objetos especiales.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Aplana el objeto actual en un paquete.
     * @param parcel objeto Parcel donde debe ser escrito
     * @param i flags adicionales sobre cómo se debe escribir el objeto
     */
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeFloat(latitude);
        parcel.writeFloat(longitude);
        parcel.writeInt(color);
    }
}
