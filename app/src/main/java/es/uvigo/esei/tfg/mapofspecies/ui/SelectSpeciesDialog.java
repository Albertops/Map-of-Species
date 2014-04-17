package es.uvigo.esei.tfg.mapofspecies.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SeekBar;

import java.util.ArrayList;

import es.uvigo.esei.tfg.mapofspecies.R;

/**
 * Crea un diálogo que permite seleccionar la especie y los parámetros para calcular la envolvente
 * convexa.
 * @author Alberto Pardellas Soto
 */
public class SelectSpeciesDialog extends DialogFragment{
    private String nameSelected = null;
    private ArrayList<String> names = new ArrayList<String>();

    public SelectSpeciesDialog() {
    }

    public SelectSpeciesDialog(ArrayList<String> names) {
        this.names = names;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("names")) {
                names = savedInstanceState.getStringArrayList("names");
            }
        }

        AlertDialog.Builder builder = null;

        if (getActivity() != null) {
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_select_species, null);
            builder.setView(view)
                    .setTitle(R.string.title_dialog_select_species)
                    .setIcon(R.drawable.ic_action_convex_hull)
                    .setPositiveButton(getString(R.string.action_accept),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CheckBox checkBox;

                                    if (view != null) {
                                        checkBox = (CheckBox) view.findViewById(R.id.checkBox);

                                        Boolean colorMarker = false;

                                        if(checkBox.isChecked()) {
                                            colorMarker = true;
                                        }

                                        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar);
                                        Integer alphaChannel = seekBar.getProgress();

                                        selectSpeciesDialogListener.onDialogPositiveClickSelectSpecies(
                                                nameSelected, colorMarker, alphaChannel);
                                    }
                                }
                            }
                    )
                    .setNegativeButton(getString(R.string.action_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            }
                    );

            final ListView listView;
            if (view != null) {
                listView = (ListView) view.findViewById(R.id.listView);

                if (view.getContext() != null) {
                    final ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
                            android.R.layout.select_dialog_singlechoice, names);

                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            nameSelected = adapterView.getAdapter().getItem(i).toString();
                            listView.setItemChecked(i, true);
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
    public interface SelectSpeciesDialogListener {
        public void onDialogPositiveClickSelectSpecies(String nameSelected, Boolean colorMarker, Integer alphaChannel);
    }

    SelectSpeciesDialogListener selectSpeciesDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            selectSpeciesDialogListener = (SelectSpeciesDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArrayList("names", names);
    }
}
