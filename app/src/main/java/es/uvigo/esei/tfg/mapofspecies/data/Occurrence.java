package es.uvigo.esei.tfg.mapofspecies.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Representa un registro del portal de datos de GBIF.
 * @author Alberto Pardellas Soto
 */
public class Occurrence implements Parcelable{
    private Double latitude;
    private Double longitude;
    private Integer color;
    private String name;

    public Occurrence() {
    }

    private Occurrence(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
        color = in.readInt();
        name = in.readString();
    }

    public static final Creator<Occurrence> CREATOR
            = new Creator<Occurrence>() {

        public Occurrence createFromParcel(Parcel in) {
            return new Occurrence(in);
        }
        public Occurrence[] newArray(int size) {
            return new Occurrence[size];
        }
    };

    /**
     * Devuelve la latitud.
     * @return latitud actual.
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Devuelve la logitud.
     * @return longitud actual.
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Devuelve el color.
     * @return color actual.
     */
    public Integer getColor() {
        return color;
    }

    /**
     * Devuelve el nombre.
     * @return nombre actual.
     */
    public String getName() {
        return name;
    }

    /**
     * Permite asignar la latitud.
     * @param latitude representa la latitud.
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Permite asignar la longitud.
     * @param longitude representa la longitud.
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Permite asignar un color.
     * @param color representa el color.
     */
    public void setColor(Integer color) {
        this.color = color;
    }

    /**
     * Permite asignar un nombre.
     * @param name representa el nombre.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeInt(color);
        parcel.writeString(name);
    }
}
