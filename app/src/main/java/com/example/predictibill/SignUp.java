package com.example.predictibill;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.predictibill.controllers.AuthController;
import com.example.predictibill.utilities.FirebaseAuthHelper;
import com.google.android.material.button.MaterialButton;

public class SignUp extends AppCompatActivity implements AuthController.AuthCallback {
    private EditText firstNameEdit, lastNameEdit, emailEdit, passwordEdit, confirmEdit;
    private CheckBox agreeCheckbox;
    private TextView errorView;
    private MaterialButton signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        initializeViews();
        setupSignUpButton();
        setupLoginLink();
    }

    private void initializeViews() {
        firstNameEdit = findViewById(R.id.firstName_text);
        lastNameEdit = findViewById(R.id.lastName_text);
        emailEdit = findViewById(R.id.email_text);
        passwordEdit = findViewById(R.id.password_text);
        confirmEdit = findViewById(R.id.confirm_password_text);
        agreeCheckbox = findViewById(R.id.agree_checkbox);
        errorView = findViewById(R.id.subheading_txt);
        signUpButton = findViewById(R.id.signUp_button);
    }

    private void setupSignUpButton() {
        signUpButton.setOnClickListener(v -> {
            errorView.setText("");
            signUpButton.setEnabled(false);
            signUpButton.setText("Processing...");

            AuthController authController = new AuthController(new FirebaseAuthHelper(this));
            authController.handleRegistration(
                    firstNameEdit,
                    lastNameEdit,
                    emailEdit,
                    passwordEdit,
                    confirmEdit,
                    agreeCheckbox,
                    errorView,
                    this
            );
        });
    }

    private void setupLoginLink() {
        findViewById(R.id.loginFooter_txt).setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
        });
    }

    @Override
    public void onRegistrationSuccess() {
        runOnUiThread(() -> {
            errorView.setTextColor(Color.GREEN);
            errorView.setText("Registration successful! Please check your email.");
            startActivity(new Intent(this, AccountCreated.class));
            finish();
        });
    }

    @Override
    public void onRegistrationFailure(String errorCode, String errorMessage) {
        runOnUiThread(() -> {
            errorView.setTextColor(Color.RED);
            errorView.setText(errorMessage);
            signUpButton.setEnabled(true);
            signUpButton.setText("Sign Up");

            if ("network_error".equals(errorCode) || "max_retries".equals(errorCode)) {
                errorView.append("\n\nPlease check your internet connection and try again.");
            }
        });
    }

    @Override
    public void onValidationFailed() {
        runOnUiThread(() -> {
            signUpButton.setEnabled(true);
            signUpButton.setText("Sign Up");
        });
    }
}