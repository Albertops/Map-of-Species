package es.uvigo.esei.tfg.mapofspecies.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import es.uvigo.esei.tfg.mapofspecies.R;
import es.uvigo.esei.tfg.mapofspecies.data.ImageAdapter;

/**
 * Crea un diálogo que permite seleccionar un color.
 * @author Alberto Pardellas Soto
 */
public class SelectColorDialog extends DialogFragment {
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

            final GridView gridView;

            Integer[] data = new Integer[] {
                    R.drawable.ic_color_azure, R.drawable.ic_color_blue,
                    R.drawable.ic_color_cyan, R.drawable.ic_color_green,
                    R.drawable.ic_color_magenta, R.drawable.ic_color_orange,
                    R.drawable.ic_color_red, R.drawable.ic_color_rose,
                    R.drawable.ic_color_violet, R.drawable.ic_color_yellow };

            if (view != null) {
                gridView = (GridView) view.findViewById(R.id.gridView);
                gridView.setAdapter(new ImageAdapter(view.getContext(), data));
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        getColor(i);

                        if (getDialog() != null) {
                            getDialog().dismiss();
                        }
                    }
                });
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
