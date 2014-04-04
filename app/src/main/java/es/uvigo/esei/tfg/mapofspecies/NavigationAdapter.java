package es.uvigo.esei.tfg.mapofspecies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavigationAdapter extends BaseAdapter{
    private Activity activity;
    private ArrayList<ItemObject> items;

    public NavigationAdapter(Activity activity,ArrayList<ItemObject> items) {
        super();

        this.activity = activity;
        this.items =items;
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return items.size();
    }

    public static class Row {
        TextView title;
        ImageView icon;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Row view;
        LayoutInflater inflater = activity.getLayoutInflater();

        if(convertView == null) {
            view = new Row();
            ItemObject itemObject = items.get(position);
            convertView = inflater.inflate(R.layout.item, null);

            view.title = (TextView) convertView.findViewById(R.id.title_item);
            view.title.setText(itemObject.getTitle());
            view.icon = (ImageView) convertView.findViewById(R.id.icon);
            view.icon.setImageResource(itemObject.getIcon());

            convertView.setTag(view);

        } else {
            view = (Row) convertView.getTag();
        }

        return convertView;
    }
}
