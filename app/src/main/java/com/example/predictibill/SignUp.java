package com.example.predictibill;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private EditText firstNameText, lastNameText, emailText, passwordText, confirmPasswordText;
    private TextView subheadingText, loginFooterText;
    private ImageView checkIcon1, checkIcon2, checkIcon3;
    private MaterialButton signUpButton;
    private CheckBox agreeCheckbox;

    // Firebase variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize all views
        initializeViews();

        // Set up password validation
        setupPasswordValidation();

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        firstNameText = findViewById(R.id.firstName_text);
        lastNameText = findViewById(R.id.lastName_text);
        emailText = findViewById(R.id.email_text);
        passwordText = findViewById(R.id.password_text);
        confirmPasswordText = findViewById(R.id.confirm_password_text);
        subheadingText = findViewById(R.id.subheading_txt);
        checkIcon1 = findViewById(R.id.checkIcon4);
        checkIcon2 = findViewById(R.id.checkIcon);
        checkIcon3 = findViewById(R.id.checkIcon6);
        signUpButton = findViewById(R.id.signUp_button);
        loginFooterText = findViewById(R.id.loginFooter_txt);
        agreeCheckbox = findViewById(R.id.agree_checkbox);

        // Initially hide all check icons
        checkIcon1.setVisibility(View.INVISIBLE);
        checkIcon2.setVisibility(View.INVISIBLE);
        checkIcon3.setVisibility(View.INVISIBLE);
    }

    private void setupPasswordValidation() {
        passwordText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordRequirements();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupClickListeners() {
        loginFooterText.setOnClickListener(v -> {
            // Add visual feedback
            v.setAlpha(0.7f);
            v.postDelayed(() -> v.setAlpha(1f), 100);
            navigateToLogin();
        });

        signUpButton.setOnClickListener(v -> {
            subheadingText.setVisibility(View.INVISIBLE);
            attemptSignUp();
        });
    }

    private void validatePasswordRequirements() {
        String password = passwordText.getText().toString();

        // Check length requirement (8+ characters)
        boolean isLengthValid = password.length() >= 8;
        checkIcon1.setVisibility(isLengthValid ? View.VISIBLE : View.INVISIBLE);

        // Check number requirement
        boolean hasNumber = password.matches(".*\\d.*");
        checkIcon2.setVisibility(hasNumber ? View.VISIBLE : View.INVISIBLE);

        // Check case requirement
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        checkIcon3.setVisibility((hasUpper && hasLower) ? View.VISIBLE : View.INVISIBLE);
    }

    private void navigateToLogin() {
        Log.d(TAG, "Navigating to Login activity");
        // Clear any existing authentication when going to login
        mAuth.signOut();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    private void attemptSignUp() {
        String firstName = firstNameText.getText().toString().trim();
        String lastName = lastNameText.getText().toString().trim();
        String email = emailText.getText().toString().trim();
        String password = passwordText.getText().toString();
        String confirmPassword = confirmPasswordText.getText().toString();
        boolean isAgreed = agreeCheckbox.isChecked();

        if (!validateInputs(firstName, lastName, email, password, confirmPassword, isAgreed)) {
            return;
        }

        showProgress(true);

        // Create user with Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            saveUserToFirestore(user.getUid(), firstName, lastName, email);
                                        } else {
                                            showError("Account created but failed to send verification email");
                                            saveUserToFirestore(user.getUid(), firstName, lastName, email);
                                        }
                                    });
                        }
                    } else {
                        showProgress(false);
                        showError("Sign up failed: " + getErrorMessage(task.getException()));
                    }
                });
    }

    private void saveUserToFirestore(String userId, String firstName, String lastName, String email) {
        Map<String, Object> user = new HashMap<>();
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("email", email);
        user.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    navigateToAccountCreated(firstName, lastName, email);
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Log.w(TAG, "Error saving user data", e);
                    navigateToAccountCreated(firstName, lastName, email);
                });
    }

    private String getErrorMessage(Exception e) {
        if (e == null) return "Unknown error occurred";
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Email already in use";
        }
        return e.getMessage();
    }

    private void showProgress(boolean show) {
        signUpButton.setEnabled(!show);
        signUpButton.setText(show ? "Creating account..." : "Sign Up");
    }

    private boolean validateInputs(String firstName, String lastName, String email,
                                   String password, String confirmPassword, boolean isAgreed) {
        if (TextUtils.isEmpty(firstName)) {
            showError("First name is required");
            return false;
        }

        if (TextUtils.isEmpty(lastName)) {
            showError("Last name is required");
            return false;
        }

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

        if (!password.equals(confirmPassword)) {
            showError("Passwords don't match");
            return false;
        }

        if (!validatePassword(password)) {
            showError("Password doesn't meet all requirements");
            return false;
        }

        if (!isAgreed) {
            showError("You must agree to the terms and conditions");
            return false;
        }

        return true;
    }

    private boolean validatePassword(String password) {
        boolean isLengthValid = password.length() >= 8;
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");

        return isLengthValid && hasNumber && hasUpper && hasLower;
    }

    private void navigateToAccountCreated(String firstName, String lastName, String email) {
        Intent intent = new Intent(this, AccountCreated.class);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("email", email);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        subheadingText.setText(message);
        subheadingText.setVisibility(View.VISIBLE);
    }
}