package es.uvigo.esei.tfg.mapofspecies.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.util.ArrayList;

import es.uvigo.esei.tfg.mapofspecies.R;
import es.uvigo.esei.tfg.mapofspecies.data.ItemObject;
import es.uvigo.esei.tfg.mapofspecies.data.ListViewAdapter;

/**
 * Crea un diálogo que permite seleccionar un color.
 * @author Alberto Pardellas Soto
 */
public class SelectColorDialog extends DialogFragment {
    private int colorSelected = 0;
    private int icon;

    public SelectColorDialog() {
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = null;

        if (getActivity() != null) {
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_select_color, null);
            builder.setView(view)
                    .setTitle(R.string.dialog_select_color)
                    .setIcon(icon)
                    .setNegativeButton(getString(R.string.action_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }
                    );

            final ListView listView;

            TypedArray colorIcons = getResources().obtainTypedArray(R.array.color_icons);
            String[] colorNames = getResources().getStringArray(R.array.colors);
            ArrayList<ItemObject> drawerItems = new ArrayList<ItemObject>();

            if (colorIcons != null) {
                drawerItems.add(new ItemObject(colorNames[0], colorIcons.getResourceId(0, -1)));
                drawerItems.add(new ItemObject(colorNames[1], colorIcons.getResourceId(1, -1)));
                drawerItems.add(new ItemObject(colorNames[2], colorIcons.getResourceId(2, -1)));
                drawerItems.add(new ItemObject(colorNames[3], colorIcons.getResourceId(3, -1)));
                drawerItems.add(new ItemObject(colorNames[4], colorIcons.getResourceId(4, -1)));
                drawerItems.add(new ItemObject(colorNames[5], colorIcons.getResourceId(5, -1)));
                drawerItems.add(new ItemObject(colorNames[6], colorIcons.getResourceId(6, -1)));
                drawerItems.add(new ItemObject(colorNames[7], colorIcons.getResourceId(7, -1)));
                drawerItems.add(new ItemObject(colorNames[8], colorIcons.getResourceId(8, -1)));
                drawerItems.add(new ItemObject(colorNames[9], colorIcons.getResourceId(9, -1)));
            }

            if (view != null) {
                listView = (ListView) view.findViewById(R.id.listView);

                if (view.getContext() != null) {
                    ListViewAdapter listViewAdapter = new ListViewAdapter(view.getContext(), drawerItems, R.layout.color_item);
                    listView.setAdapter(listViewAdapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            colorSelected = i;
                            listView.setItemChecked(i, true);
                            getColor(colorSelected);

                            if (getDialog() != null) {
                                getDialog().dismiss();
                            }
                        }
                    });
                }
            }
        }

        if (builder != null) {
            return builder.create();
        }

        return null;
    }


    /**
     * Interfaz para el botón aceptar del diálogo.
     */
    public interface SelectColorDialogListener {
        public void onDialogPositiveClickSelectColor(int color, int idIcon, String tag);
    }

    SelectColorDialogListener selectColorDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            selectColorDialogListener = (SelectColorDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SelectColorDialogListener");
        }
    }

    /**
     * Selecciona el color y el id del ícono.
     * @param colorSelected color seleccionado.
     */
    public void getColor(int colorSelected) {
        int color = 0;
        int idIcon = 0;

        if (colorSelected == 0) {
            color = (int) BitmapDescriptorFactory.HUE_AZURE;
            idIcon = R.drawable.ic_color_azure;

        } else if (colorSelected == 1) {
            color = (int) BitmapDescriptorFactory.HUE_BLUE;
            idIcon = R.drawable.ic_color_blue;

        } else if (colorSelected == 2) {
            color = (int) BitmapDescriptorFactory.HUE_CYAN;
            idIcon = R.drawable.ic_color_cyan;

        } else if (colorSelected == 3) {
            color = (int) BitmapDescriptorFactory.HUE_GREEN;
            idIcon = R.drawable.ic_color_green;

        } else if (colorSelected == 4) {
            color = (int) BitmapDescriptorFactory.HUE_MAGENTA;
            idIcon = R.drawable.ic_color_magenta;

        } else if (colorSelected == 5) {
            color = (int) BitmapDescriptorFactory.HUE_ORANGE;
            idIcon = R.drawable.ic_color_orange;

        } else if (colorSelected == 6) {
            color = (int) BitmapDescriptorFactory.HUE_RED;
            idIcon = R.drawable.ic_color_red;

        } else if (colorSelected == 7) {
            color = (int) BitmapDescriptorFactory.HUE_ROSE;
            idIcon = R.drawable.ic_color_rose;

        } else if (colorSelected == 8) {
            color = (int) BitmapDescriptorFactory.HUE_VIOLET;
            idIcon = R.drawable.ic_color_violet;

        } else if (colorSelected == 9) {
            color = (int) BitmapDescriptorFactory.HUE_YELLOW;
            idIcon = R.drawable.ic_color_yellow;
        }

        selectColorDialogListener.onDialogPositiveClickSelectColor(color, idIcon, getTag());
    }
}
