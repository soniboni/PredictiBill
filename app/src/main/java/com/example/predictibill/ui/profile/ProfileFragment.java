package com.example.predictibill.ui.profile;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import com.example.predictibill.R;

public class ProfileFragment extends Fragment {

    private SwitchMaterial in_app_reminders_toggle_button;
    private SwitchMaterial email_alerts_toggle_button;
    private Spinner currency_spinner;

    // Modal dialogs
    private Dialog logoutDialog;
    private Dialog deactivateAccountDialog;

    // Currency options
    private static final String[] currencies = {"PHP", "USD"};

    public ProfileFragment() {
        // Required empty public constructor
        super(R.layout.fragment_profile);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize modal dialogs
        setupLogoutDialog();
        setupDeactivateAccountDialog();

        // 2. Button Functionality (ID from fragment)

        // Change Password Button - Direct Navigation to Change Password Fragment
        Button changePassword = view.findViewById(R.id.profile_change_password_button);
        changePassword.setOnClickListener(v -> {
            try {
                // Navigate directly to the Change Password Fragment
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationProfile_to_changePasswordFragment);
            } catch (Exception e) {
                // Fallback error handling
                Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // Help Button - Direct Navigation to Help Fragment
        Button help = view.findViewById(R.id.profile_help_button);
        help.setOnClickListener(v -> {
            try {
                // Navigate directly to the Help Fragment
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationProfile_to_helpFragment);
            } catch (Exception e) {
                // Fallback error handling
                Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // Privacy Policy Button - Direct Navigation to Privacy Policy Fragment
        Button privacyPolicy = view.findViewById(R.id.profile_privacy_policy_button);
        privacyPolicy.setOnClickListener(v -> {
            try {
                // Navigate directly to the Privacy Policy Fragment
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationProfile_to_privacyPolicyFragment);
            } catch (Exception e) {
                // Fallback error handling
                Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });

        // Logout Button - Show Logout Modal
        Button logoutButton = view.findViewById(R.id.profile_logout_button);
        logoutButton.setOnClickListener(v -> {
            if (logoutDialog != null) {
                logoutDialog.show();
            }
        });

        // Deactivate Account Button - Show Deactivate Modal
        Button deactivateButton = view.findViewById(R.id.profile_deactivate_account_button);
        deactivateButton.setOnClickListener(v -> {
            if (deactivateAccountDialog != null) {
                deactivateAccountDialog.show();
            }
        });

        // Initialize the toggle buttons and spinner
        in_app_reminders_toggle_button = view.findViewById(R.id.in_app_reminders_toggle_button);
        email_alerts_toggle_button = view.findViewById(R.id.email_alerts_toggle_button);
        currency_spinner = view.findViewById(R.id.currency_spinner);

        // Load saved preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        boolean inAppReminders = prefs.getBoolean("in_app_reminders", false);
        boolean emailAlerts = prefs.getBoolean("email_alerts", false);

        // Set PHP as default currency only if it doesn't already exist
        if (!prefs.contains("selected_currency")) {
            editor.putString("selected_currency", "PHP");
            editor.apply();
        }

        String savedCurrency = prefs.getString("selected_currency", "PHP");

        // Set toggle states from preferences
        in_app_reminders_toggle_button.setChecked(inAppReminders);
        email_alerts_toggle_button.setChecked(emailAlerts);

        // Setup currency spinner
        setupCurrencySpinner(savedCurrency);

        // Set listeners to save preferences when toggles change and
        // display toast messages to confirm that the toggle buttons work
        in_app_reminders_toggle_button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("in_app_reminders", isChecked).apply();
            String message = "In-app reminders " + (isChecked ? "enabled" : "disabled");
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        email_alerts_toggle_button.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("email_alerts", isChecked).apply();
            String message = "Email alerts " + (isChecked ? "enabled" : "disabled");
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupLogoutDialog() {
        logoutDialog = new Dialog(requireContext());
        logoutDialog.setContentView(R.layout.logout_dialog_box);

        // Make background transparent
        if (logoutDialog.getWindow() != null) {
            logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        logoutDialog.setCancelable(false);

        // Get buttons from the logout dialog layout
        Button logoutBtn = logoutDialog.findViewById(R.id.logout_btn);
        Button stayLoggedInBtn = logoutDialog.findViewById(R.id.stay_logged_in_btn);

        // Logout button functionality
        logoutBtn.setOnClickListener(v -> {
            // Clear user session/preferences
            clearUserSession();

            // Close the dialog
            logoutDialog.dismiss();

            // Navigate to login screen or main activity
            navigateToLogin();

            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        });

        // Stay logged in button functionality
        stayLoggedInBtn.setOnClickListener(v -> {
            logoutDialog.dismiss();
            Toast.makeText(requireContext(), "You're still logged in", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDeactivateAccountDialog() {
        deactivateAccountDialog = new Dialog(requireContext());
        deactivateAccountDialog.setContentView(R.layout.deactivate_account_dialog_box);

        // Make background transparent
        if (deactivateAccountDialog.getWindow() != null) {
            deactivateAccountDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        deactivateAccountDialog.setCancelable(false);

        // Get buttons from the deactivate dialog layout
        Button deactivate_btn = deactivateAccountDialog.findViewById(R.id.deactivate_btn);
        Button cancel_btn = deactivateAccountDialog.findViewById(R.id.cancel_btn);

        // Deactivate button functionality
        deactivate_btn.setOnClickListener(v -> {
            // Perform account deactivation logic
            deactivateAccount();

            // Close the dialog
            deactivateAccountDialog.dismiss();

            // Navigate to login screen or main activity
            navigateToLogin();

            Toast.makeText(requireContext(), "Account deactivated", Toast.LENGTH_SHORT).show();
        });

        // Cancel button functionality
        cancel_btn.setOnClickListener(v -> {
            deactivateAccountDialog.dismiss();
            Toast.makeText(requireContext(), "Account deactivation cancelled", Toast.LENGTH_SHORT).show();
        });
    }

    private void clearUserSession() {
        // Clear all user preferences/session data
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        // You can also clear other app-specific data here
        // For example: clear database, clear cached data, etc.
    }

    private void deactivateAccount() {
        // Clear user session data
        clearUserSession();

        // Add additional deactivation logic here:
        // - Call API to deactivate account on server
        // - Clear local database
        // - Clear any cached user data

        // Example API call (uncomment and modify as needed):
        // ApiService.deactivateAccount(userId, new ApiCallback() {
        //     @Override
        //     public void onSuccess() {
        //         // Handle successful deactivation
        //     }
        //
        //     @Override
        //     public void onError(String error) {
        //         // Handle deactivation error
        //     }
        // });
    }

    private void navigateToLogin() {
        try {
            // Navigate to login screen
            // Replace with your actual login destination
            NavHostFragment.findNavController(this)
                    .navigate(R.id.navigation_profile);
        } catch (Exception e) {
            // If navigation fails, you might want to restart the app
            // or handle the error appropriately
            Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

            // Alternative: Close the app or restart main activity
            requireActivity().finishAffinity();
        }
    }

    private void setupCurrencySpinner(String savedCurrency) {
        // Custom adapter to apply gray text color
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                currencies
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(R.color.brown, null)); // Main spinner text
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(getResources().getColor(R.color.brown, null)); // Dropdown items
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currency_spinner.setAdapter(adapter);

        // Set default selection
        int savedPosition = getCurrencyPosition(savedCurrency);
        if (savedPosition == -1) savedPosition = 0; // fallback to PHP if invalid
        currency_spinner.setSelection(savedPosition, false); // false = avoid triggering listener

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);

        currency_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean isFirstSelection = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstSelection) {
                    isFirstSelection = false;
                    return; // skip first call on setup
                }

                String selectedCurrency = currencies[position];
                String previousCurrency = prefs.getString("selected_currency", "PHP");

                // Save new selection
                prefs.edit().putString("selected_currency", selectedCurrency).apply();

                if (!selectedCurrency.equals(previousCurrency)) {
                    Toast.makeText(requireContext(), "Currency changed to " + selectedCurrency, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private int getCurrencyPosition(String currency) {
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equalsIgnoreCase(currency)) {
                return i;
            }
        }
        return 0; // Default to PHP
    }

    // Utility method to get the currently selected currency from anywhere in your app
    public static String getSelectedCurrency(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        return prefs.getString("selected_currency", "PHP");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up dialogs to prevent memory leaks
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
        }
        if (deactivateAccountDialog != null && deactivateAccountDialog.isShowing()) {
            deactivateAccountDialog.dismiss();
        }
    }
}