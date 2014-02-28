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

    }
}
