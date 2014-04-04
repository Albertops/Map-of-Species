package es.uvigo.esei.tfg.mapofspecies;

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

/**
 * Crea un diálogo que permite seleccionar el mapa que se va a cargar.
 */
public class LoadMapDialog extends DialogFragment{
    private String mapSelected = "";
    private ArrayList<String> results;

    /**
     * Constructor por defecto.
     */
    public LoadMapDialog() {
    }

    /**
     * Constructor parametrizado con la lista de mapas.
     * @param results
     */
    public LoadMapDialog(ArrayList<String> results) {
        this.results = results;
    }

    /**
     * Se encarga de construir el diálogo.
     * @param savedInstanceState contiene el estado de la actividad.
     * @return dialogo con la configuración establecida.
     */
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

    /**
     * Llamado cuando un fragmento se une a su actividad.
     * @param activity actividad que se va a utilizar.
     */
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

    /**
     * Llamado para guardar el estado de una actividad.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putStringArrayList("results", results);
    }
}
