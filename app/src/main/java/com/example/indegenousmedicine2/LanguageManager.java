package com.example.indegenousmedicine2;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

/**
 * Central place for the app's locale handling.
 *
 * The reliable way to localise an Activity is to wrap its base context in
 * {@link #wrap(Context)} from {@code attachBaseContext()} — this rebuilds the
 * resource configuration *before* any layout is inflated, so XML-inflated text
 * and {@code getString()} calls all resolve in the same language. The old
 * {@code updateConfiguration()} approach only affected strings fetched *after*
 * the call, which is why English and Assamese used to bleed into each other.
 */
public class LanguageManager {
    private static final String PREFS_NAME = "app_preferences";
    private static final String LANGUAGE_PREF_KEY = "languagePref";
    private static final String DEFAULT_LANGUAGE = "en";

    public static void setLanguage(Context context, String languageCode) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().putString(LANGUAGE_PREF_KEY, languageCode).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getString(LANGUAGE_PREF_KEY, DEFAULT_LANGUAGE);
    }

    /**
     * Returns a context whose resources are configured for the user's chosen
     * language. Call from {@code Activity#attachBaseContext(Context)}:
     * <pre>{@code
     *   @Override
     *   protected void attachBaseContext(Context newBase) {
     *       super.attachBaseContext(LanguageManager.wrap(newBase));
     *   }
     * }</pre>
     */
    public static Context wrap(Context context) {
        String languageCode = getLanguage(context);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLayoutDirection(locale);
        }
        return context.createConfigurationContext(config);
    }

    /**
     * Legacy in-place locale apply, kept for any caller not yet migrated to
     * {@link #wrap(Context)}. Prefer wrap(); this only affects strings fetched
     * after it runs.
     */
    @SuppressWarnings("deprecation")
    public static void applyLanguage(Context context) {
        String languageCode = getLanguage(context);
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }
}
