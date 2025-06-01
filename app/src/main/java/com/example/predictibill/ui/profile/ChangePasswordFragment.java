package com.example.predictibill.ui.profile;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.predictibill.R;

public class ChangePasswordFragment extends Fragment {

    private EditText currentPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button continueButton;
    private Button cancelButton;

    public ChangePasswordFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views with try-catch for debugging
        try {
            currentPasswordEditText = view.findViewById(R.id.enter_your_current_password_text);
            newPasswordEditText = view.findViewById(R.id.enter_your_new_password_text);
            confirmPasswordEditText = view.findViewById(R.id.confirm_your_new_password_text);
            continueButton = view.findViewById(R.id.profile_continue_button);
            cancelButton = view.findViewById(R.id.profile_cancel_button);

            // Verify views are found
            if (currentPasswordEditText == null || newPasswordEditText == null ||
                    confirmPasswordEditText == null || continueButton == null || cancelButton == null) {
                throw new IllegalStateException("One or more views not found in layout");
            }

            // Set click listeners
            continueButton.setOnClickListener(v -> showConfirmPasswordChangeModal());
            cancelButton.setOnClickListener(v -> showDiscardPasswordChangeModal());

        } catch (Exception e) {
            e.printStackTrace();
            // Log the error or handle gracefully
            Toast.makeText(requireContext(), "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Add error handling to modal methods
    private void showConfirmPasswordChangeModal() {
        try {
            // First validate inputs before showing confirmation modal
            String currentPassword = currentPasswordEditText.getText().toString().trim();
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            // Validate inputs first
            if (!validatePasswordInputs(currentPassword, newPassword, confirmPassword)) {
                return; // Stop if validation fails
            }

            // Create and configure the confirmation dialog
            Dialog confirmDialog = new Dialog(requireContext());
            confirmDialog.setContentView(R.layout.confirm_change_password_dialog_box);

            // Make background transparent
            if (confirmDialog.getWindow() != null) {
                confirmDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            confirmDialog.setCancelable(false);

            // Initialize modal buttons with null checks
            Button cancelModalButton = confirmDialog.findViewById(R.id.cancel_btn);
            Button confirmModalButton = confirmDialog.findViewById(R.id.confirm_btn);

            if (cancelModalButton == null || confirmModalButton == null) {
                Toast.makeText(requireContext(), "Error: Modal buttons not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Confirm button in modal - proceed with password change
            confirmModalButton.setOnClickListener(v -> {
                confirmDialog.dismiss();
                handlePasswordChange(); // Execute the actual password change logic
            });

            // Cancel button in modal - just close the modal
            cancelModalButton.setOnClickListener(v -> {
                confirmDialog.dismiss();
            });

            confirmDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error showing confirmation dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showDiscardPasswordChangeModal() {
        try {
            // Create and configure the discard confirmation dialog
            Dialog discardDialog = new Dialog(requireContext());
            discardDialog.setContentView(R.layout.discard_password_change_dialog_box);

            // Make background transparent
            if (discardDialog.getWindow() != null) {
                discardDialog.getWindow().setBackgroundDrawable(
                        new ColorDrawable(android.graphics.Color.TRANSPARENT));
            }

            discardDialog.setCancelable(false);

            // Initialize modal buttons with null checks
            Button discardButton = discardDialog.findViewById(R.id.discard_change_btn);
            Button stayButton = discardDialog.findViewById(R.id.go_back_btn);

            if (discardButton == null || stayButton == null) {
                Toast.makeText(requireContext(), "Error: Modal buttons not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Discard button - navigate back and lose changes
            discardButton.setOnClickListener(v -> {
                discardDialog.dismiss();
                navigateBackToProfile(); // Navigate away, discarding changes
            });

            // Stay button - just close the modal and remain on the page
            stayButton.setOnClickListener(v -> {
                discardDialog.dismiss();
            });

            discardDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error showing discard dialog: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    //  Navigation with error handling
    private void navigateBackToProfile() {
        try {
            // Check if NavController is available
            if (NavHostFragment.findNavController(this) != null) {
                // Try specific navigation action first
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_navigationChangePassword_to_profileFragment);
            }
        } catch (IllegalArgumentException e) {
            // Action doesn't exist, try alternative navigation
            try {
                NavHostFragment.findNavController(this).popBackStack();
            } catch (Exception ex) {
                // Last resort - finish activity or handle gracefully
                Toast.makeText(requireContext(), "Navigation error", Toast.LENGTH_SHORT).show();
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        // Validate current password
        if (currentPassword.isEmpty()) {
            currentPasswordEditText.setError("Current password is required");
            currentPasswordEditText.requestFocus();
            return false;
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            newPasswordEditText.setError("New password is required");
            newPasswordEditText.requestFocus();
            return false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("Please confirm your password");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            confirmPasswordEditText.requestFocus();
            return false;
        }

        // Check password length
        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters");
            newPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void handlePasswordChange() {
        // Get input values
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        // Verify current password with backend/database
        if (validateCurrentPassword(currentPassword)) {
            // Update password in backend/database
            updatePassword(newPassword);

            // Navigate back to Profile & Settings
            navigateBackToProfile();

            // Show success toast
            Toast.makeText(requireContext(), "Password Changed Successfully", Toast.LENGTH_LONG).show();
        } else {
            currentPasswordEditText.setError("Current password is incorrect");
            currentPasswordEditText.requestFocus();
        }
    }

    private boolean validateCurrentPassword(String currentPassword) {
        // TODO: Implement password validation logic
        return true; // Placeholder
    }

    private void updatePassword(String newPassword) {
        // TODO: Implement password update logic
        System.out.println("Password updated successfully");
    }
}