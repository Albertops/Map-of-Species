package es.uvigo.esei.tfg.mapofspecies.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import es.uvigo.esei.tfg.mapofspecies.R;

/**
 * Se encarga de crear un diálogo que muestra los términos de uso.
 * @author Alberto Pardellas Soto
 */
public class WelcomeDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = null;

        if (getActivity() != null) {
            builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View view;
            view = inflater.inflate(R.layout.dialog_welcome, null);

            builder.setView(view)
                    .setTitle(R.string.welcome_dialog_title)
                    .setIcon(R.drawable.ic_action_about)
                    .setPositiveButton(getString(R.string.action_accept),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SharedPreferences sharedPreferences = getActivity()
                                            .getSharedPreferences("Preferences", Context.MODE_PRIVATE);

                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("show_welcome_dialog", false);
                                    editor.commit();
                                }
                            }
                    )
                    .setNegativeButton(getString(R.string.action_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Intent.ACTION_MAIN);
                                    intent.addCategory(Intent.CATEGORY_HOME);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
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

