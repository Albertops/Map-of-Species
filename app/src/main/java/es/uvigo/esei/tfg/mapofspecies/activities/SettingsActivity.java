package es.uvigo.esei.tfg.mapofspecies.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.widget.ImageView;

import es.uvigo.esei.tfg.mapofspecies.R;
import es.uvigo.esei.tfg.mapofspecies.dialogs.DeleteHistoryDialog;
import es.uvigo.esei.tfg.mapofspecies.dialogs.SelectColorDialog;
import es.uvigo.esei.tfg.mapofspecies.preferences.ColorPreference;

/**
 * Muestra la pantalla de preferencias.
 * @author Alberto Pardellas Soto
 */
public class SettingsActivity extends PreferenceActivity
        implements SelectColorDialog.SelectColorDialogListener {

    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settingsFragment = new SettingsFragment();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, settingsFragment)
                .commit();
    }

    @Override
    public void onDialogPositiveClickSelectColor(int color, int idIcon, String tag) {
        SharedPreferences sharedPreferences =
                getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (tag.equals("markers_color")) {

            editor.putInt("markers_color", color);
            editor.putInt("markers_color_id", idIcon);
            editor.commit();

            settingsFragment.saveDrawableMarkerColorId(idIcon);

        } else if (tag.equals("convex_hull_color")) {

            editor.putInt("convex_hull_color", color);
            editor.commit();

            settingsFragment.saveDrawableConvexHullColorId(idIcon);
        }
    }

    /**
     * Fragmento que contiene la pantalla de preferencias.
     */
    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            Preference deleteHistory = findPreference("delete_history");

            if (deleteHistory != null) {
                deleteHistory.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        DeleteHistoryDialog deleteHistoryDialog = new DeleteHistoryDialog();

                        if (getFragmentManager() != null) {
                            deleteHistoryDialog.show(getFragmentManager(), "");
                        }

                        return false;
                    }
                });
            }

            ColorPreference markersPreference = (ColorPreference)
                    findPreference("markers_color");


            if (markersPreference != null) {
                markersPreference.setPersistent(true);
                markersPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SelectColorDialog selectColorDialog = new SelectColorDialog();
                        selectColorDialog.setIcon(R.drawable.ic_action_place);

                        if (getFragmentManager() != null) {
                            selectColorDialog.show(getFragmentManager(), "markers_color");
                        }

                        return false;
                    }
                });
            }

            ColorPreference convexHullPreference = (ColorPreference)
                    findPreference("convex_hull_color");


            if (convexHullPreference != null) {
                convexHullPreference.setPersistent(true);
                convexHullPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SelectColorDialog selectColorDialog = new SelectColorDialog();
                        selectColorDialog.setIcon(R.drawable.ic_action_convex_hull);

                        if (getFragmentManager() != null) {
                            selectColorDialog.show(getFragmentManager(), "convex_hull_color");
                        }

                        return false;
                    }
                });
            }
        }

        /**
         * Guarda el color del marcador y actualiza el ícono.
         * @param id identificador de la imagen.
         */
        public void saveDrawableMarkerColorId(int id) {
            ColorPreference markersPreference = (ColorPreference)
                    findPreference("markers_color");

            if (markersPreference != null) {
                markersPreference.persistDrawableColorId(id);

                ImageView imageView = (ImageView)
                        markersPreference.getView().findViewById(R.id.imageView2);

                imageView.setImageResource(id);
            }
        }

        /**
         * Guarda el color para el relleno de la envolvente convexa y actualiza el ícono.
         * @param id identificador de la imagen.
         */
        public void saveDrawableConvexHullColorId(int id) {
            ColorPreference convexHullPreference = (ColorPreference)
                    findPreference("convex_hull_color");

            if (convexHullPreference != null) {
                convexHullPreference.persistDrawableColorId(id);

                ImageView imageView = (ImageView)
                        convexHullPreference.getView().findViewById(R.id.imageView2);

                imageView.setImageResource(id);
            }
        }
    }
}
