package com.example.teledentistry_project.utils;

import android.content.Context;
import android.content.SharedPreferences;

//Session storage
//It securely keeps the logged-in user's access token, refresh token, user ID, and email.
public class SessionManager {

    private static final String PREF_NAME = "smile_session";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {

        sharedPreferences = context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
        );

        editor = sharedPreferences.edit();
    }

    public void saveSession(
            String authToken,
            String userId,
            String email,
            String role
    ) {

        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_ROLE, role);

        editor.apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(
                KEY_AUTH_TOKEN,
                null
        );
    }

    public String getUserId() {
        return sharedPreferences.getString(
                KEY_USER_ID,
                null
        );
    }

    public String getEmail() {
        return sharedPreferences.getString(
                KEY_EMAIL,
                null
        );
    }

    public String getRole() {
        return sharedPreferences.getString(
                KEY_ROLE,
                null
        );
    }

    public boolean isLoggedIn() {

        String token = getAuthToken();

        return token != null && !token.isEmpty();
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}