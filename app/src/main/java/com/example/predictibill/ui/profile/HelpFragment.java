package com.example.predictibill.ui.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.predictibill.R;
import com.google.android.material.button.MaterialButton;

public class HelpFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the close button
        MaterialButton closeButton = view.findViewById(R.id.close_button);

        // Set click listener for the close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to Profile & Settings fragment
                navigateBackToProfileSettings();
            }
        });
    }

    private void navigateBackToProfileSettings() {
        // Pop back to the previous fragment (Profile & Settings)
        getParentFragmentManager().popBackStack();
    }
}