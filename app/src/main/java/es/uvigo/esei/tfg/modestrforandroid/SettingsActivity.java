package es.uvigo.esei.tfg.modestrforandroid;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Muestra la pantalla de preferencias.
 */
public class SettingsActivity extends Activity {

    /**
     * Llamado cuando se crea la actividad por primera vez.
     * @param savedInstanceState contiene el estado de la actividad.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    /**
     * Fragmento que contiene la pantalla de preferencias.
     */
    public static class SettingsFragment extends PreferenceFragment {
        /**
         * Llamado cuando se crea el fragmento por primera vez.
         * @param savedInstanceState contiene el estado del fragmento.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);
        }
    }
}
