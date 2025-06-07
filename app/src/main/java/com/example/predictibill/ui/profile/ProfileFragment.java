package com.example.predictibill.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.ui.auth.Login;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.predictibill.R;

public class ProfileFragment extends Fragment {

    private SwitchMaterial in_app_reminders_toggle_button;
    private SwitchMaterial email_alerts_toggle_button;
    private Spinner currency_spinner;

    // Modal dialogs
    private Dialog logoutDialog;
    private Dialog deactivateAccountDialog;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Currency options
    private static final String[] currencies = {"PHP", "USD"};

    public ProfileFragment() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupLogoutDialog();
        setupDeactivateAccountDialog();
        setupButtonListeners(view);
        initializeUIComponents(view);
        loadUserPreferences();
    }

    private void setupButtonListeners(View view) {
        Button changePassword = view.findViewById(R.id.profile_change_password_button);
        changePassword.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationProfile_to_changePasswordFragment);
            } catch (Exception e) {
                showToast("Navigation error: " + e.getMessage());
            }
        });

        Button help = view.findViewById(R.id.profile_help_button);
        help.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationProfile_to_helpFragment);
            } catch (Exception e) {
                showToast("Navigation error: " + e.getMessage());
            }
        });

        Button privacyPolicy = view.findViewById(R.id.profile_privacy_policy_button);
        privacyPolicy.setOnClickListener(v -> {
            try {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationProfile_to_privacyPolicyFragment);
            } catch (Exception e) {
                showToast("Navigation error: " + e.getMessage());
            }
        });

        Button logoutButton = view.findViewById(R.id.profile_logout_button);
        logoutButton.setOnClickListener(v -> {
            if (logoutDialog != null) {
                logoutDialog.show();
            }
        });

        Button deactivateButton = view.findViewById(R.id.profile_deactivate_account_button);
        deactivateButton.setOnClickListener(v -> {
            if (deactivateAccountDialog != null) {
                deactivateAccountDialog.show();
            }
        });
    }

    private void initializeUIComponents(View view) {
        in_app_reminders_toggle_button = view.findViewById(R.id.in_app_reminders_toggle_button);
        email_alerts_toggle_button = view.findViewById(R.id.email_alerts_toggle_button);
        currency_spinner = view.findViewById(R.id.currency_spinner);
    }

    private void loadUserPreferences() {
        if (currentUser == null) return;

        // First try to load from Firestore
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load from Firestore
                        Boolean inAppReminders = documentSnapshot.getBoolean("inAppReminders");
                        Boolean emailAlerts = documentSnapshot.getBoolean("emailAlerts");
                        String currency = documentSnapshot.getString("currency");

                        // Update local preferences
                        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        if (inAppReminders != null) {
                            editor.putBoolean("in_app_reminders", inAppReminders);
                            in_app_reminders_toggle_button.setChecked(inAppReminders);
                        }

                        if (emailAlerts != null) {
                            editor.putBoolean("email_alerts", emailAlerts);
                            email_alerts_toggle_button.setChecked(emailAlerts);
                        }

                        if (currency != null) {
                            editor.putString("selected_currency", currency);
                            setupCurrencySpinner(currency);
                        } else {
                            setupCurrencySpinner("PHP");
                        }

                        editor.apply();
                    } else {
                        // Fallback to local preferences if Firestore doesn't have data
                        loadFromLocalPreferences();
                    }
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to load preferences");
                    loadFromLocalPreferences();
                });

        // Set up toggle listeners after loading
        setupToggleListeners();
    }

    private void loadFromLocalPreferences() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        boolean inAppReminders = prefs.getBoolean("in_app_reminders", false);
        boolean emailAlerts = prefs.getBoolean("email_alerts", false);
        String currency = prefs.getString("selected_currency", "PHP");

        in_app_reminders_toggle_button.setChecked(inAppReminders);
        email_alerts_toggle_button.setChecked(emailAlerts);
        setupCurrencySpinner(currency);
    }

    private void setupToggleListeners() {
        in_app_reminders_toggle_button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreferenceToFirestore("inAppReminders", isChecked);

            // Implement in-app reminder logic
            if (isChecked) {
                // Enable in-app reminders
                // You would typically schedule notifications here
                showToast("In-app reminders enabled");
            } else {
                // Disable in-app reminders
                // Cancel scheduled notifications
                showToast("In-app reminders disabled");
            }
        });

        email_alerts_toggle_button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreferenceToFirestore("emailAlerts", isChecked);

            // Implement email alert logic
            if (isChecked) {
                // Enable email alerts
                showToast("Email alerts enabled");
            } else {
                // Disable email alerts
                showToast("Email alerts disabled");
            }
        });
    }

    private void savePreferenceToFirestore(String key, Object value) {
        if (currentUser == null) return;

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Update local preferences
        if (value instanceof Boolean) {
            editor.putBoolean(key.equals("inAppReminders") ? "in_app_reminders" : "email_alerts", (Boolean) value);
        } else if (value instanceof String) {
            editor.putString("selected_currency", (String) value);
        }
        editor.apply();

        // Update Firestore
        db.collection("users").document(currentUser.getUid())
                .update(key, value)
                .addOnSuccessListener(aVoid -> {
                    // Successfully updated
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save preference");
                });
    }

    private void setupLogoutDialog() {
        logoutDialog = new Dialog(requireContext());
        logoutDialog.setContentView(R.layout.logout_dialog_box);

        if (logoutDialog.getWindow() != null) {
            logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        logoutDialog.setCancelable(false);

        Button logoutBtn = logoutDialog.findViewById(R.id.logout_btn);
        Button stayLoggedInBtn = logoutDialog.findViewById(R.id.stay_logged_in_btn);

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            clearUserSession();
            logoutDialog.dismiss();
            navigateToLogin();
            showToast("Logged out successfully");
        });

        stayLoggedInBtn.setOnClickListener(v -> {
            logoutDialog.dismiss();
            showToast("You're still logged in");
        });
    }

    private void setupDeactivateAccountDialog() {
        deactivateAccountDialog = new Dialog(requireContext());
        deactivateAccountDialog.setContentView(R.layout.deactivate_account_dialog_box);

        if (deactivateAccountDialog.getWindow() != null) {
            deactivateAccountDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        deactivateAccountDialog.setCancelable(false);

        Button deactivate_btn = deactivateAccountDialog.findViewById(R.id.deactivate_btn);
        Button cancel_btn = deactivateAccountDialog.findViewById(R.id.cancel_btn);

        deactivate_btn.setOnClickListener(v -> {
            deactivateAccount();
            deactivateAccountDialog.dismiss();
        });

        cancel_btn.setOnClickListener(v -> {
            deactivateAccountDialog.dismiss();
            showToast("Account deactivation cancelled");
        });
    }

    private void clearUserSession() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    private void deactivateAccount() {
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        currentUser.delete()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        clearUserSession();
                                        navigateToLogin();
                                        showToast("Account deactivated successfully");
                                    } else {
                                        showToast("Failed to deactivate account: " + task.getException().getMessage());
                                    }
                                });
                    })
                    .addOnFailureListener(e -> {
                        showToast("Failed to delete user data: " + e.getMessage());
                    });
        } else {
            showToast("No user logged in");
        }
    }

    private void navigateToLogin() {
        try {
            Intent intent = new Intent(requireActivity(), Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        } catch (Exception e) {
            showToast("Error: " + e.getMessage());
            requireActivity().finishAffinity();
        }
    }

    private void setupCurrencySpinner(String savedCurrency) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                currencies
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(R.color.brown, null));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(R.color.brown, null));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency_spinner.setAdapter(adapter);

        int savedPosition = getCurrencyPosition(savedCurrency);
        currency_spinner.setSelection(savedPosition, false);

        currency_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return;
                }

                String selectedCurrency = currencies[position];
                savePreferenceToFirestore("currency", selectedCurrency);
                showToast("Currency changed to " + selectedCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private int getCurrencyPosition(String currency) {
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equalsIgnoreCase(currency)) {
                return i;
            }
        }
        return 0;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static String getSelectedCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        return prefs.getString("selected_currency", "PHP");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
        }
        if (deactivateAccountDialog != null && deactivateAccountDialog.isShowing()) {
            deactivateAccountDialog.dismiss();
        }
    }
}