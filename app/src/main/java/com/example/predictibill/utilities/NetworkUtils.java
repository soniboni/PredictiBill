package com.example.predictibill.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
            Log.d(TAG, "Network available: " + isConnected);
            return isConnected;
        } catch (Exception e) {
            Log.e(TAG, "Network check failed", e);
            return false;
        }
    }

    public static void testFirebaseConnectivity(Context context) {
        new Thread(() -> {
            try {
                URL url = new URL("https://identitytoolkit.googleapis.com");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.connect();
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Firebase Auth endpoint response: " + responseCode);
                connection.disconnect();
            } catch (IOException e) {
                Log.e(TAG, "Firebase Auth endpoint test failed", e);
            }
        }).start();
    }
}