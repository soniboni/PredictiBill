package com.example.predictibill;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotPassword extends AppCompatActivity {
    private static final String TAG = "ForgotPassword";
    private EditText emailEditText;
    private TextView errorMessageTextView;
    private Button continueButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.email_text);
        errorMessageTextView = findViewById(R.id.errorMessage_txt);
        Button cancelButton = findViewById(R.id.cancel_button);
        continueButton = findViewById(R.id.continue_button);

        errorMessageTextView.setVisibility(View.INVISIBLE);

        // Set click listeners
        cancelButton.setOnClickListener(v -> navigateToLogin());

        continueButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                showError("Please enter your email address");
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Please enter a valid email address");
                return;
            }

            sendPasswordResetEmail(email);
        });
    }

    private void sendPasswordResetEmail(String email) {
        continueButton.setEnabled(false);
        showLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    continueButton.setEnabled(true);
                    showLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this,
                                "Password reset email sent to " + email,
                                Toast.LENGTH_SHORT).show();
                        navigateToPasswordRecovery(email);
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                            showError("This email is not registered");
                        } else {
                            showError("Failed to send reset email. Please try again.");
                        }
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        continueButton.setText(isLoading ? "Processing..." : "Continue");
    }

    private void navigateToPasswordRecovery(String email) {
        Intent intent = new Intent(this, PasswordRecovery.class);
        intent.putExtra("email", email);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void showError(String message) {
        errorMessageTextView.setText(message);
        errorMessageTextView.setVisibility(View.VISIBLE);
    }
}