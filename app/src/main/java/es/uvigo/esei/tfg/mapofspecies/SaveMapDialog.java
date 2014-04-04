package es.uvigo.esei.tfg.mapofspecies;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Crea un diálogo que permite introducir el nombre del mapa que se va a guardar.
 */
public class SaveMapDialog extends DialogFragment {
    /**
     * Se encarga de construir el diálogo.
     * @param savedInstanceState contiene el estado de la actividad.
     * @return dialogo con la configuración establecida.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = null;

        if (getActivity() != null) {
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view = inflater.inflate(R.layout.dialog_save_map, null);

            builder.setView(view)
                    .setTitle(R.string.action_map_save)
                    .setIcon(R.drawable.ic_action_save)
                    .setPositiveButton(getString(R.string.action_accept),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    if (view != null) {
                                        EditText editText = (EditText) view.findViewById(R.id.editText);
                                        String mapName = editText.getText().toString();

                                        // Oculta el teclado virtual
                                        InputMethodManager inputMethodManager = (InputMethodManager)
                                                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                                        inputMethodManager.hideSoftInputFromWindow(
                                                editText.getWindowToken(),
                                                InputMethodManager.HIDE_NOT_ALWAYS);

                                        if (!mapName.equals("")) {
                                            saveMapDialogListener.onDialogPositiveClickSaveMap(mapName);

                                        } else {
                                            Toast.makeText(getActivity(), getString(R.string.empty_name),
                                                    Toast.LENGTH_LONG).show();
                                        }
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
        }

        if (builder != null) {
            return builder.create();
        }

        return null;
    }

    /**
     * Interfaz para el botón aceptar del diálogo.
     */
    public interface SaveMapDialogListener {
        public void onDialogPositiveClickSaveMap(String mapName);
    }

    SaveMapDialogListener saveMapDialogListener;

    /**
     * Llamado cuando un fragmento se une a su actividad.
     * @param activity actividad que se va a utilizar.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            saveMapDialogListener = (SaveMapDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
}
