package com.example.predictibill;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private FirebaseAuth mAuth;
    private EditText emailText, passwordText;
    private TextView subheadingText;
    private MaterialButton loginButton;

    private TextView createHereText;

    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        emailText = findViewById(R.id.email_text);
        passwordText = findViewById(R.id.password_text);
        subheadingText = findViewById(R.id.subheading_txt);
        loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(v -> attemptLogin());

        createHereText = findViewById(R.id.signUpFooter_txt);
        createHereText.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, SignUp.class); // replace SignUp with your actual sign-up activity class name
            startActivity(intent);
        });

        forgotPasswordText = findViewById(R.id.forgotPassword_txt);
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, ForgotPassword.class); // Replace with your actual PasswordReset activity
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        // Only redirect if user is verified
        if (currentUser != null && currentUser.isEmailVerified()) {
            Log.d(TAG, "User is verified, redirecting to MainActivity");
            navigateToMainActivity();
        } else if (currentUser != null) {
            Log.d(TAG, "User exists but is not verified, staying on login screen");
        }
    }

    private void attemptLogin() {
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString().trim();

        if (!validateInputs(email, password)) return;
        if (!isNetworkAvailable()) {
            showError("No internet connection");
            return;
        }

        showProgress(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null && user.isEmailVerified()) {
                            navigateToMainActivity();
                        } else {
                            showError("Please verify your email first");
                            sendEmailVerification(user);
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            showError("Email is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Invalid email format");
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            showError("Password is required");
            return false;
        }
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void handleLoginError(Exception exception) {
        String errorMessage = "Login failed. Please try again.";
        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid email or password";
        }
        showError(errorMessage);
    }

    private void navigateToMainActivity() {
        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    private void showProgress(boolean show) {
        loginButton.setEnabled(!show);
        loginButton.setText(show ? "Authenticating..." : "Login");
    }

    private void showError(String message) {
        subheadingText.setText(message);
        subheadingText.setVisibility(View.VISIBLE);
    }
}