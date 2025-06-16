package com.example.predictibill.utils;

import android.util.Patterns;

public class AuthUtils {
    public static boolean isValidEmail(String email) {
        return email != null &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null &&
                password.length() >= 8 &&
                password.matches(".*\\d.*") && // At least 1 digit
                password.matches(".*[A-Z].*"); // At least 1 uppercase
    }

    public static boolean isValidName(String name) {
        return name != null &&
                name.length() >= 2 &&
                name.matches("^[a-zA-Z\\s'-]+$");
    }
}