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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

    }
}
