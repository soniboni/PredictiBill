package com.example.predictibill.ui.auth;

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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.predictibill.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";
    private static final int RC_SIGN_IN = 1234;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailText, passwordText;
    private TextView subheadingText;
    private MaterialButton loginButton, googleLoginButton;
    private TextView createHereText, forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        emailText = findViewById(R.id.email_text);
        passwordText = findViewById(R.id.password_text);
        subheadingText = findViewById(R.id.subheading_txt);
        loginButton = findViewById(R.id.login_button);
        googleLoginButton = findViewById(R.id.loginGoogle_button);
        createHereText = findViewById(R.id.signUpFooter_txt);
        forgotPasswordText = findViewById(R.id.forgotPassword_txt);

        loginButton.setOnClickListener(v -> attemptLogin());
        googleLoginButton.setOnClickListener(v -> signInWithGoogle());

        createHereText.setOnClickListener(v -> startActivity(new Intent(Login.this, SignUp.class)));
        forgotPasswordText.setOnClickListener(v -> startActivity(new Intent(Login.this, ForgotPassword.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (user.getProviderData().size() == 1 && !user.isEmailVerified()) {
                Log.d(TAG, "Email user not verified");
                return;
            }
            navigateToHomeActivity();
        }
    }

    private void signInWithGoogle() {
        if (!isNetworkAvailable()) {
            showSnackbar("No internet connection");
            return;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                showSnackbar("Google sign in failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showProgress(true);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        showSnackbar("Authentication failed: " + task.getException().getMessage());
                    }
                });
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
                            navigateToHomeActivity();
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
                            showSnackbar("Verification email sent to " + user.getEmail());
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

    private void navigateToHomeActivity() {
        Intent intent = new Intent(this, com.example.predictibill.ui.home.HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showProgress(boolean show) {
        loginButton.setEnabled(!show);
        googleLoginButton.setEnabled(!show);
        loginButton.setText(show ? "Authenticating..." : "Login");
        googleLoginButton.setText(show ? "Authenticating..." : "Login with Google");
    }

    private void showError(String message) {
        subheadingText.setText(message);
        subheadingText.setVisibility(View.VISIBLE);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
