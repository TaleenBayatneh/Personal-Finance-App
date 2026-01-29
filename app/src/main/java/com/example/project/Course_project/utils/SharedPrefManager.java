package com.example.project.Course_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {

    //shared preferences file name for storing app settings
    private static final String PREF_NAME = "app_settings_prefs";

    //key for storing theme preference light or dark
    private static final String KEY_THEME = "app_theme";
    //key for storing default report period preference
    private static final String KEY_DEFAULT_PERIOD = "default_period";
    //key for storing remembered email for login
    private static final String KEY_REMEMBER_EMAIL = "remember_email";

    //shared preferences instance for reading data
    private SharedPreferences prefs;
    //shared preferences editor for writing data
    private SharedPreferences.Editor editor;

    //constructor to initialize shared preferences
    public SharedPrefManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    //save theme preference light or dark
    public void setTheme(String theme) {
        //store theme preference in shared preferences
        editor.putString(KEY_THEME, theme);
        //apply changes to preferences
        editor.apply();
    }

    //retrieve stored theme preference with default as light
    public String getTheme() {
        return prefs.getString(KEY_THEME, "light");
    }

    //save user email for remember me functionality
    public void saveRememberedEmail(String email) {
        //store email in shared preferences
        editor.putString(KEY_REMEMBER_EMAIL, email);
        //apply changes to preferences
        editor.apply();
    }

    //retrieve remembered email if it was saved
    public String getRememberedEmail() {
        return prefs.getString(KEY_REMEMBER_EMAIL, null);
    }

    //clear remembered email from preferences
    public void clearRememberedEmail() {
        //remove email key from preferences
        editor.remove(KEY_REMEMBER_EMAIL);
        //apply changes to preferences
        editor.apply();
    }

    //save default report period preference (daily,weekly,monthly,yearly)
    public void setDefaultPeriod(String period) {
        //store period preference in shared preferences
        editor.putString(KEY_DEFAULT_PERIOD, period);
        //apply changes to preferences
        editor.apply();
    }

    //retrieve default report period with default as monthly
    public String getDefaultPeriod() {
        return prefs.getString(KEY_DEFAULT_PERIOD, "monthly");
    }
}