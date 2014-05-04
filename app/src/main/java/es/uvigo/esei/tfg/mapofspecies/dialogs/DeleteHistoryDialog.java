package es.uvigo.esei.tfg.mapofspecies.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import es.uvigo.esei.tfg.mapofspecies.R;
import es.uvigo.esei.tfg.mapofspecies.providers.CustomSuggestionsProvider;

/**
 * Crea un diálogo que permite borrar el historial reciente.
 * @author Alberto Pardellas Soto
 */
public class DeleteHistoryDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = null;

        if (getActivity() != null) {
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.dialog_delete_history, null);
            builder.setView(view)
                    .setTitle(R.string.title_delete_history)
                    .setIcon(R.drawable.ic_action_discard)
                    .setPositiveButton(getString(R.string.action_accept),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SearchRecentSuggestions suggestions =
                                            new SearchRecentSuggestions(
                                                    getActivity(),
                                                    CustomSuggestionsProvider.AUTHORITY,
                                                    CustomSuggestionsProvider.MODE);

                                    suggestions.clearHistory();
                                    Toast toast = Toast.makeText(getActivity(),
                                            getString(R.string.delete_history),
                                            Toast.LENGTH_LONG);

                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            })
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
}

