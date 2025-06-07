package com.example.predictibill.ui.profile;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.ui.auth.Login;
import com.example.predictibill.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {

    private TextInputEditText currentPasswordEditText,
            newPasswordEditText, confirmPasswordEditText;
    private MaterialButton continueButton, cancelButton;

    private View checkIcon1, checkIcon2, checkIcon3;

    public ChangePasswordFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            currentPasswordEditText = view.findViewById(R.id.enter_your_current_password_text);
            newPasswordEditText = view.findViewById(R.id.enter_your_new_password_text);
            confirmPasswordEditText = view.findViewById(R.id.confirm_your_new_password_text);
            continueButton = view.findViewById(R.id.profile_continue_button);
            cancelButton = view.findViewById(R.id.profile_cancel_button);

            checkIcon1 = view.findViewById(R.id.check_Icon_1);
            checkIcon2 = view.findViewById(R.id.check_Icon_2);
            checkIcon3 = view.findViewById(R.id.check_Icon_3);

            resetChecklistIcons();

            continueButton.setOnClickListener(v -> showConfirmPasswordChangeModal());
            cancelButton.setOnClickListener(v -> showDiscardPasswordChangeModal());

            newPasswordEditText.addTextChangedListener(passwordWatcher);
            confirmPasswordEditText.addTextChangedListener(passwordWatcher);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error initializing views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void resetChecklistIcons() {
        checkIcon1.setBackgroundResource(R.drawable.uncheck_icon);
        checkIcon2.setBackgroundResource(R.drawable.uncheck_icon);
        checkIcon3.setBackgroundResource(R.drawable.uncheck_icon);
    }

    private final TextWatcher passwordWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            updatePasswordChecklist();
        }
        @Override public void afterTextChanged(Editable s) {}
    };

    private void updatePasswordChecklist() {
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (newPassword.length() >= 6) {
            checkIcon1.setBackgroundResource(R.drawable.check_icon);
        } else {
            checkIcon1.setBackgroundResource(R.drawable.uncheck_icon);
        }

        if (newPassword.matches(".*\\d.*")) {
            checkIcon2.setBackgroundResource(R.drawable.check_icon);
        } else {
            checkIcon2.setBackgroundResource(R.drawable.uncheck_icon);
        }

        if (newPassword.matches(".*[a-z].*") && newPassword.matches(".*[A-Z].*")) {
            checkIcon3.setBackgroundResource(R.drawable.check_icon);
        } else {
            checkIcon3.setBackgroundResource(R.drawable.uncheck_icon);
        }
    }

    private void showConfirmPasswordChangeModal() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validatePasswordInputs(currentPassword, newPassword, confirmPassword)) return;

        // Create custom dialog
        Dialog confirmDialog = new Dialog(requireContext());
        confirmDialog.setContentView(R.layout.confirm_change_password_dialog_box);
        if (confirmDialog.getWindow() != null) {
            confirmDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        confirmDialog.setCancelable(false);

        // Find custom buttons by ID
        MaterialButton cancelModalButton = confirmDialog.findViewById(R.id.cancel_btn);
        MaterialButton confirmModalButton = confirmDialog.findViewById(R.id.change_password_btn);

        if (cancelModalButton == null || confirmModalButton == null) {
            Toast.makeText(requireContext(), "Dialog button(s) not found in layout!", Toast.LENGTH_SHORT).show();
            confirmDialog.dismiss();
            return;
        }

        cancelModalButton.setOnClickListener(v -> confirmDialog.dismiss());

        confirmModalButton.setOnClickListener(v -> {
            confirmDialog.dismiss();
            handlePasswordChange();
        });

        confirmDialog.show();
    }

    private void showDiscardPasswordChangeModal() {
        Dialog discardDialog = new Dialog(requireContext());
        discardDialog.setContentView(R.layout.discard_password_change_dialog_box);
        if (discardDialog.getWindow() != null) {
            discardDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        discardDialog.setCancelable(false);

        MaterialButton discardButton = discardDialog.findViewById(R.id.discard_change_btn);
        MaterialButton stayButton = discardDialog.findViewById(R.id.go_back_btn);

        discardButton.setOnClickListener(v -> {
            discardDialog.dismiss();
            navigateBackToProfile();
        });

        stayButton.setOnClickListener(v -> discardDialog.dismiss());

        discardDialog.show();
    }

    private void navigateBackToProfile() {
        try {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_navigationChangePassword_to_profileFragment);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            currentPasswordEditText.setError("Current password is required");
            return false;
        }

        if (newPassword.length() < 6) {
            newPasswordEditText.setError("Password must be at least 6 characters");
            return false;
        }

        if (!newPassword.matches(".*\\d.*")) {
            newPasswordEditText.setError("Password must contain at least one number");
            return false;
        }

        if (!(newPassword.matches(".*[a-z].*") && newPassword.matches(".*[A-Z].*"))) {
            newPasswordEditText.setError("Password must contain uppercase and lowercase letters");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void handlePasswordChange() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getEmail() != null) {
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(updateTask -> {
                                        if (updateTask.isSuccessful()) {
                                            FirebaseAuth.getInstance().signOut();
                                            Toast.makeText(requireContext(), "Password changed. Please log in again.", Toast.LENGTH_LONG).show();
                                            Intent intent = new Intent(requireContext(), Login.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(requireContext(), "Failed to update password: " + updateTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            currentPasswordEditText.setError("Current password is incorrect");
                            currentPasswordEditText.requestFocus();
                        }
                    });
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show();
        }
    }
}
