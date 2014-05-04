package es.uvigo.esei.tfg.mapofspecies.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * Adaptador para el diálogo de selección de color.
 * @author Alberto Pardellas Soto
 */
public class ImageAdapter extends BaseAdapter{
    private Context context;
    private Integer[] data;

    /**
     * Constructor para el adaptador personalizado ImageAdapter.
     * @param c contexto en el cual se crea el adaptador.
     * @param d array de enteros con los id de las imágenes.
     */
    public ImageAdapter(Context c, Integer[] d) {
        context = c;
        data = d;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ImageView imageView;

        if (view == null) {
            imageView = new ImageView(context);
            imageView.setPadding(8, 8, 8, 8);

        } else {
            imageView = (ImageView) view;
        }

        imageView.setImageResource(data[i]);

        return imageView;
    }
}
