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
import android.widget.ListView;

import java.util.ArrayList;

import es.uvigo.esei.tfg.mapofspecies.R;

/**
 * Crea un diálogo que permite seleccionar el mapa que se va a cargar.
 * @author Alberto Pardellas Soto
 */
public class LoadMapDialog extends DialogFragment{
    private String mapSelected = "";
    private ArrayList<String> results = new ArrayList<String>();

    public LoadMapDialog() {
    }

    public LoadMapDialog(ArrayList<String> results) {
        this.results = results;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("results")) {
                results = savedInstanceState.getStringArrayList("results");
            }
        }

        AlertDialog.Builder builder = null;

        if (getActivity() != null) {
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_load_map, null);
            builder.setView(view)
                    .setTitle(R.string.action_map_load)
                    .setIcon(R.drawable.ic_action_storage)
                    .setPositiveButton(getString(R.string.action_accept),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    loadMapDialogListener.onDialogPositiveClickLoadMap(mapSelected);
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
                            android.R.layout.select_dialog_singlechoice, results);

                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            mapSelected = adapterView.getAdapter().getItem(i).toString();
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
    public interface LoadMapDialogListener {
        public void onDialogPositiveClickLoadMap(String mapSelected);
    }

    LoadMapDialogListener loadMapDialogListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            loadMapDialogListener = (LoadMapDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArrayList("results", results);
    }
}
