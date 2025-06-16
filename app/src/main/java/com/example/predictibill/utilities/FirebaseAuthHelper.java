package com.example.predictibill.utilities;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.predictibill.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseAuthHelper {
    private static final String TAG = "FirebaseAuthHelper";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;
    private final Activity activity;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    public FirebaseAuthHelper(Activity activity) {
        this.activity = activity;
        this.mAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String firstName, String lastName, String email, String password, AuthCallback callback) {
        if (retryCount >= MAX_RETRIES) {
            callback.onFailure("max_retries", "Maximum retry attempts reached. Please check your connection.");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        retryCount = 0; // Reset on success
                        handleRegistrationSuccess(firstName, lastName, email, callback);
                    } else {
                        handleRegistrationError(task, callback, firstName, lastName, email, password);
                    }
                });
    }

    private void handleRegistrationSuccess(String firstName, String lastName, String email, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            updateUserProfile(user, firstName, lastName, email, callback);
        } else {
            callback.onFailure("user_not_found", "Registration failed - please try again");
        }
    }

    private void updateUserProfile(FirebaseUser user, String firstName, String lastName, String email, AuthCallback callback) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(firstName + " " + lastName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storeUserData(user.getUid(), firstName, lastName, email, callback);
                    } else {
                        callback.onFailure("profile_update_failed", "Failed to complete registration");
                    }
                });
    }

    private void storeUserData(String userId, String firstName, String lastName, String email, AuthCallback callback) {
        User user = new User(userId, firstName, lastName, email);
        db.collection("users").document(userId)
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        sendVerificationEmail(callback);
                    } else {
                        callback.onFailure("data_store_failed", "Failed to complete registration");
                    }
                });
    }

    private void sendVerificationEmail(AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure("verification_failed", "Registration complete but verification email failed");
                        }
                    });
        }
    }

    private void handleRegistrationError(Task<AuthResult> task, AuthCallback callback,
                                         String firstName, String lastName, String email, String password) {
        Exception exception = task.getException();
        if (exception instanceof FirebaseNetworkException) {
            retryCount++;
            Log.w(TAG, "Network error detected. Retry attempt: " + retryCount);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (retryCount <= MAX_RETRIES) {
                    registerUser(firstName, lastName, email, password, callback);
                } else {
                    callback.onFailure("network_error", "Unable to connect to authentication server");
                }
            }, 3000); // Retry after 3 seconds
        } else {
            String errorMessage = exception != null ? exception.getMessage() : "Registration failed";
            callback.onFailure("registration_failed", errorMessage);
        }
    }

    public interface AuthCallback {
        void onSuccess();
        void onFailure(String errorCode, String errorMessage);
    }
}