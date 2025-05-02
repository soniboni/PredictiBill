package com.example.predictibill.services;

import android.util.Log;
import com.example.predictibill.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import java.util.HashMap;
import java.util.Map;

public class AuthService {
    private static final String TAG = "AuthService";
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface AuthCallback {
        void onSuccess(User user);
        void onFailure(String error);
    }

    // Email Registration
    public void registerUser(String name, String email, String password, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            createFirestoreUser(firebaseUser, name, callback);
                            sendVerificationEmail(firebaseUser);
                        }
                    } else {
                        callback.onFailure(parseError(task.getException()));
                    }
                });
    }

    // Email Login
    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            fetchFirestoreUser(firebaseUser.getUid(), callback);
                        }
                    } else {
                        callback.onFailure(parseError(task.getException()));
                    }
                });
    }

    // Session Check
    public void checkActiveSession(AuthCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            fetchFirestoreUser(firebaseUser.getUid(), callback);
        } else {
            callback.onFailure("No active session");
        }
    }

    private void createFirestoreUser(FirebaseUser firebaseUser, String name, AuthCallback callback) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", firebaseUser.getUid());
        user.put("name", name);
        user.put("email", firebaseUser.getEmail());
        user.put("createdDate", com.google.firebase.Timestamp.now());

        db.collection("users").document(firebaseUser.getUid())
                .set(user, SetOptions.merge())
                .addOnSuccessListener(aVoid -> callback.onSuccess(new User(
                        firebaseUser.getUid(),
                        name,
                        firebaseUser.getEmail(),
                        com.google.firebase.Timestamp.now()
                )))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore user creation failed", e);
                    callback.onFailure("Failed to create user profile");
                });
    }

    private void fetchFirestoreUser(String userId, AuthCallback callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User profile not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Database error"));
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) Log.d(TAG, "Verification email sent");
                });
    }

    private String parseError(Exception ex) {
        if (ex instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException)
            return "Email already exists";
        if (ex instanceof com.google.firebase.auth.FirebaseAuthInvalidCredentialsException)
            return "Invalid credentials";
        if (ex instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException)
            return "Account not found";
        return "Authentication failed";
    }

    public void logout() {
        auth.signOut();
    }
}