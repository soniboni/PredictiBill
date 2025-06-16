package com.example.predictibill.controllers;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.example.predictibill.utilities.AuthValidator;
import com.example.predictibill.utilities.FirebaseAuthHelper;

public class AuthController {
    private final FirebaseAuthHelper authHelper;

    public AuthController(FirebaseAuthHelper authHelper) {
        this.authHelper = authHelper;
    }

    public void handleRegistration(
            EditText firstNameEdit,
            EditText lastNameEdit,
            EditText emailEdit,
            EditText passwordEdit,
            EditText confirmEdit,
            CheckBox agreeCheckbox,
            TextView errorView,
            AuthCallback callback
    ) {
        if (!validateAllFields(firstNameEdit, lastNameEdit, emailEdit, passwordEdit, confirmEdit, agreeCheckbox, errorView)) {
            callback.onValidationFailed();
            return;
        }

        authHelper.registerUser(
                firstNameEdit.getText().toString().trim(),
                lastNameEdit.getText().toString().trim(),
                emailEdit.getText().toString().trim(),
                passwordEdit.getText().toString().trim(),
                new FirebaseAuthHelper.AuthCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onRegistrationSuccess();
                    }

                    @Override
                    public void onFailure(String errorCode, String errorMessage) {
                        callback.onRegistrationFailure(errorCode, errorMessage);
                    }
                }
        );
    }

    private boolean validateAllFields(
            EditText firstNameEdit,
            EditText lastNameEdit,
            EditText emailEdit,
            EditText passwordEdit,
            EditText confirmEdit,
            CheckBox agreeCheckbox,
            TextView errorView
    ) {
        boolean isValid = true;

        if (!AuthValidator.validateName(firstNameEdit, "First name")) isValid = false;
        if (!AuthValidator.validateName(lastNameEdit, "Last name")) isValid = false;
        if (!AuthValidator.validateEmail(emailEdit)) isValid = false;
        if (!AuthValidator.validatePassword(passwordEdit, confirmEdit, errorView)) isValid = false;
        if (!AuthValidator.validateCheckbox(agreeCheckbox)) isValid = false;

        return isValid;
    }

    public interface AuthCallback {
        void onRegistrationSuccess();
        void onRegistrationFailure(String errorCode, String errorMessage);
        void onValidationFailed();
    }
}