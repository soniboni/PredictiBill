package com.example.predictibill.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.predictibill.models.User;

public class SessionManager {
    private static final String PREF_NAME = "PredictiBillSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(User user) {
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.apply();
    }

    public User getSession() {
        return new User(
                pref.getString(KEY_USER_ID, null),
                pref.getString(KEY_USER_NAME, null),
                pref.getString(KEY_USER_EMAIL, null),
                null
        );
    }

    public void clearSession() {
        editor.clear().apply();
    }

    public boolean isLoggedIn() {
        return pref.getString(KEY_USER_ID, null) != null;
    }
}