package com.example.predictibill.utilities;

import android.text.TextUtils;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class AuthValidator {
    public static boolean validateName(EditText editText, String fieldName) {
        String value = editText.getText().toString().trim();
        if (TextUtils.isEmpty(value)) {
            editText.setError(fieldName + " is required");
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean validateEmail(EditText editText) {
        String email = editText.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            editText.setError("Email is required");
            editText.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editText.setError("Please enter a valid email");
            editText.requestFocus();
            return false;
        }
        return true;
    }

    public static boolean validatePassword(EditText passwordEdit, EditText confirmEdit, TextView errorView) {
        String password = passwordEdit.getText().toString().trim();
        String confirm = confirmEdit.getText().toString().trim();

        if (TextUtils.isEmpty(password)) {
            passwordEdit.setError("Password is required");
            passwordEdit.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            errorView.setText("Password must be at least 8 characters");
            passwordEdit.setError("Too short");
            passwordEdit.requestFocus();
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            errorView.setText("Password must contain at least 1 number");
            passwordEdit.setError("Missing number");
            passwordEdit.requestFocus();
            return false;
        }

        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*")) {
            errorView.setText("Password must contain both upper and lower case letters");
            passwordEdit.setError("Invalid case");
            passwordEdit.requestFocus();
            return false;
        }

        if (!password.equals(confirm)) {
            confirmEdit.setError("Passwords don't match");
            confirmEdit.requestFocus();
            return false;
        }

        errorView.setText("");
        return true;
    }

    public static boolean validateCheckbox(CheckBox checkBox) {
        if (!checkBox.isChecked()) {
            checkBox.setError("You must agree to the terms");
            return false;
        }
        return true;
    }
}