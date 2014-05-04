package es.uvigo.esei.tfg.mapofspecies.data;

/**
 * Representa un elemento del ListView asociado al DrawerLayout.
 * @author Alberto Pardellas Soto
 */
public class ItemObject {
    private String title;
    private int icon;

    /**
     * Constructor para los elementos asociados al DrawerLayout.
     * @param title representa un título.
     * @param icon representa un ícono.
     */
    public ItemObject(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    /**
     * Devuelve el título.
     * @return título actual.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Permite asignar un título.
     * @param title representa el título.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Devuelve el ícono.
     * @return ícono actual.
     */
    public int getIcon() {
        return icon;
    }

    /**
     * Permite asignar un ícono.
     * @param icon representa un ícono.
     */
    public void setIcon(int icon) {
        this.icon = icon;
    }
}
