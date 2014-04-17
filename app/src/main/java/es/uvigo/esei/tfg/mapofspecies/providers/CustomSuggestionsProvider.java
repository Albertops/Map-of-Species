package es.uvigo.esei.tfg.mapofspecies.providers;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Crea un proveedor de sugerencias personalizado.
 * @author Alberto Pardellas Soto
 */
public class CustomSuggestionsProvider extends SearchRecentSuggestionsProvider{
    public final static String AUTHORITY = "es.uvigo.esei.tfg.mapofspecies.providers.CustomSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public CustomSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
