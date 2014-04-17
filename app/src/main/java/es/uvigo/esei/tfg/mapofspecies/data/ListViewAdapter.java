package es.uvigo.esei.tfg.mapofspecies.data;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import es.uvigo.esei.tfg.mapofspecies.R;

/**
 * Adaptador personalizado para el DrawerLayout.
 * @author Alberto Pardellas Soto
 */
public class ListViewAdapter extends BaseAdapter{
    private Context context;
    private ArrayList<ItemObject> items;
    private int idLayout;

    public ListViewAdapter(Context context, ArrayList<ItemObject> items, int idLayout) {
        super();

        this.context = context;
        this.items =items;
        this.idLayout = idLayout;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View row = inflater.inflate(idLayout, null);

        ItemObject itemObject = items.get(position);

        TextView label;

        if (row != null) {
            label = (TextView) row.findViewById(R.id.title_item);

            if (label != null) {
                label.setText(itemObject.getTitle());
            }

            ImageView icon = (ImageView) row.findViewById(R.id.icon);
            icon.setImageResource(itemObject.getIcon());
        }

        return row;
    }
}
