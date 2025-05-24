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
import android.widget.Toast;

import com.example.predictibill.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {

    private SwitchMaterial in_app_reminders_toggle_button;
    private SwitchMaterial email_alerts_toggle_button;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // A function for the Notifications Toggle Buttons (In-app reminders & Email Alerts)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize the toggle buttons
        in_app_reminders_toggle_button = view.findViewById(R.id.in_app_reminders_toggle_button);
        email_alerts_toggle_button = view.findViewById(R.id.email_alerts_toggle_button);

        // Load saved preferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE);
        boolean inAppReminders = prefs.getBoolean("in_app_reminders", false);
        boolean emailAlerts = prefs.getBoolean("email_alerts", false);

        // Set toggle states from preferences
        in_app_reminders_toggle_button.setChecked(inAppReminders);
        email_alerts_toggle_button.setChecked(emailAlerts);

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
}