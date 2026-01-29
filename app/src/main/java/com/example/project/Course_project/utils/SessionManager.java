package com.example.project.Course_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    //shared preferences file name for storing session data
    private static final String PREF_NAME = "user_session_prefs";

    //key for storing user email in preferences
    private static final String KEY_USER_EMAIL = "user_email";

    //key for storing login state in preferences
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    //shared preferences instance for reading data
    private SharedPreferences prefs;
    //shared preferences editor for writing data
    private SharedPreferences.Editor editor;

    //constructor to initialize shared preferences
    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    //create and save login session for authenticated user
    public void createLoginSession(String email) {
        //set logged in flag to true
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        //store user email in session
        editor.putString(KEY_USER_EMAIL, email);
        //apply changes to preferences
        editor.apply();
    }

    //check if user is currently logged in
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    //save user email to session preferences
    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    //retrieve stored user email from session

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    //clear all session data and logout user
    public void logout() {
        editor.clear();
        editor.apply();
    }
}