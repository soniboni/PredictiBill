package com.example.predictibill.ui.profile;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.predictibill.R;
import com.google.android.material.switchmaterial.SwitchMaterial;


public class ProfileFragment extends Fragment {

    private SwitchMaterial in_app_reminders_toggle_button;
    private SwitchMaterial email_alerts_toggle_button;
    private Spinner currency_spinner;


    // Currency options
    private static final String[] currencies = {"PHP", "USD"};


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // A function for the Notifications Toggle Buttons (In-app reminders & Email Alerts)
    // and Currency Spinner
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
}