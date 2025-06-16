package com.example.predictibill.controllers;

import android.content.Context;
import com.example.predictibill.models.User;
import com.example.predictibill.services.AuthService;
import com.example.predictibill.utils.AuthUtils;
import com.example.predictibill.utils.SessionManager;

public class AuthController {
    private final AuthService authService = new AuthService();
    private final SessionManager sessionManager;

    public AuthController(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    public void handleRegistration(String name, String email,
                                   String password, String confirmPassword,
                                   AuthService.AuthCallback callback) {
        if (!AuthUtils.isValidName(name)) {
            callback.onFailure("Invalid name");
            return;
        }

        if (!AuthUtils.isValidEmail(email)) {
            callback.onFailure("Invalid email");
            return;
        }

        if (!AuthUtils.isValidPassword(password)) {
            callback.onFailure("Password must contain:\n- 8+ characters\n- 1 uppercase\n- 1 number");
            return;
        }

        if (!password.equals(confirmPassword)) {
            callback.onFailure("Passwords don't match");
            return;
        }

        authService.registerUser(name, email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                sessionManager.createSession(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String error) {
                sessionManager.clearSession();
                callback.onFailure(error);
            }
        });
    }

    public void handleLogin(String email, String password,
                            AuthService.AuthCallback callback) {
        authService.loginUser(email, password, new AuthService.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                sessionManager.createSession(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(String error) {
                sessionManager.clearSession();
                callback.onFailure(error);
            }
        });
    }

    public void checkSession(AuthService.AuthCallback callback) {
        if (sessionManager.isLoggedIn()) {
            authService.checkActiveSession(new AuthService.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    sessionManager.createSession(user);
                    callback.onSuccess(user);
                }

                @Override
                public void onFailure(String error) {
                    sessionManager.clearSession();
                    callback.onFailure(error);
                }
            });
        } else {
            callback.onFailure("No active session");
        }
    }

    public void logout() {
        authService.logout();
        sessionManager.clearSession();
    }
}