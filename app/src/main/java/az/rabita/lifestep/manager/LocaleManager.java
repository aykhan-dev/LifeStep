package az.rabita.lifestep.manager;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

public class LocaleManager {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);
        return updateResources(context, language);
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        PreferenceManager preferences = PreferenceManager.Companion.getInstance(context);
        return preferences.getStringElement(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        PreferenceManager preferences = PreferenceManager.Companion.getInstance(context);
        preferences.setStringElement(SELECTED_LANGUAGE, language);
    }

    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        return context.createConfigurationContext(configuration);
    }

}
