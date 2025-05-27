package com.example.predictibill.ui.subscriptions;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.predictibill.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class AddSubscriptionFragment extends Fragment {

    // UI Elements
    private TextView startDateDisplay, nextBillingDateDisplay;
    private Spinner categorySpinner, statusSpinner, billingSpinner, paymentMethodSpinner;
    private EditText subscriptionNameEditText, subscriptionPriceEditText, noteEditText;
    private ImageView previewImageView;
    private Button submitBtn;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // Image URI
    private Uri selectedImageUri;

    // Image Picker Launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    previewImageView.setImageURI(selectedImageUri);
                    Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_add_subscriptions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI elements
        initializeViews(view);

        // Set up back button
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Set up image upload
        View imageUploadCard = view.findViewById(R.id.imageUploadCard);
        imageUploadCard.setOnClickListener(v -> openImagePicker());

        // Populate spinners
        populateSpinners();

        // Set up date pickers
        setupDatePicker(view, R.id.start_date_container, startDateDisplay, "Select Start Date");
        setupDatePicker(view, R.id.next_billing_date_container, nextBillingDateDisplay, "Select Next Billing Date");

        // Set up form submission
        submitBtn = view.findViewById(R.id.add_sub_button);
        submitBtn.setOnClickListener(v -> {
            if (isFormValid()) {
                saveSubscriptionToFirestore();
            } else {
                Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeViews(View view) {
        categorySpinner = view.findViewById(R.id.subscription_category_spinner);
        statusSpinner = view.findViewById(R.id.subscription_status_spinner);
        billingSpinner = view.findViewById(R.id.billing_cycle_spinner);
        paymentMethodSpinner = view.findViewById(R.id.payment_method_spinner);
        startDateDisplay = view.findViewById(R.id.start_date_display);
        nextBillingDateDisplay = view.findViewById(R.id.next_billing_date_display);
        subscriptionNameEditText = view.findViewById(R.id.subscription_name_input);
        subscriptionPriceEditText = view.findViewById(R.id.price_input);
        previewImageView = view.findViewById(R.id.previewImageView);
        noteEditText = view.findViewById(R.id.subscription_note_input);
    }

    private void populateSpinners() {
        populateSpinner(categorySpinner, Arrays.asList("Entertainment", "Productivity & Tools", "Cloud Storage", "Membership", "Food & Delivery"));
        populateSpinner(statusSpinner, Arrays.asList("Upcoming", "Overdue", "Paid", "Cancelled"));
        populateSpinner(billingSpinner, Arrays.asList("Monthly", "Quarterly", "Annually"));
        populateSpinner(paymentMethodSpinner, Arrays.asList("E-wallet (Gcash)", "Debit Card"));
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private void populateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.dropdown_items);
        spinner.setAdapter(adapter);
    }

    private void setupDatePicker(View view, int containerId, TextView display, String title) {
        LinearLayout dateContainer = view.findViewById(containerId);
        dateContainer.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (dialogView, selectedYear, selectedMonth, selectedDay) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(selectedYear, selectedMonth, selectedDay);

                        String formattedDate = String.format(Locale.getDefault(),
                                "%d/%d/%d", selectedDay, selectedMonth + 1, selectedYear);
                        display.setText(formattedDate);
                        display.setTextColor(Color.BLACK);

                        if (containerId == R.id.start_date_container) {
                            setNextBillingMinDate(selectedDate);
                        }
                    },
                    year, month, day
            );
            datePickerDialog.setTitle(title);
            datePickerDialog.show();
        });
    }

    private void setNextBillingMinDate(Calendar minDate) {
        // Implementation for setting minimum date for next billing date
    }

    private boolean isFormValid() {
        return !isEmpty(subscriptionNameEditText)
                && !isEmpty(subscriptionPriceEditText)
                && isSpinnerValid(categorySpinner)
                && isSpinnerValid(statusSpinner)
                && isSpinnerValid(billingSpinner)
                && isSpinnerValid(paymentMethodSpinner)
                && !isEmpty(startDateDisplay)
                && !isEmpty(nextBillingDateDisplay);
    }

    private boolean isEmpty(TextView view) {
        return view.getText().toString().trim().isEmpty();
    }

    private boolean isSpinnerValid(Spinner spinner) {
        return spinner.getSelectedItem() != null && !spinner.getSelectedItem().toString().trim().isEmpty();
    }

    private void saveSubscriptionToFirestore() {
        // Get current user ID
        String userId = mAuth.getCurrentUser().getUid();

        // Create subscription data
        Map<String, Object> subscription = new HashMap<>();
        subscription.put("userId", userId);
        subscription.put("name", subscriptionNameEditText.getText().toString());
        subscription.put("price", Double.parseDouble(subscriptionPriceEditText.getText().toString()));
        subscription.put("category", categorySpinner.getSelectedItem().toString());
        subscription.put("status", statusSpinner.getSelectedItem().toString());
        subscription.put("billingCycle", billingSpinner.getSelectedItem().toString());
        subscription.put("startDate", startDateDisplay.getText().toString());
        subscription.put("nextBillingDate", nextBillingDateDisplay.getText().toString());
        subscription.put("paymentMethod", paymentMethodSpinner.getSelectedItem().toString());
        subscription.put("note", noteEditText.getText().toString());
        subscription.put("createdAt", new Date());

        // Add loading state
        submitBtn.setEnabled(false);
        submitBtn.setText("Saving...");

        // Add to Firestore
        db.collection("subscriptions")
                .add(subscription)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Subscription added!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .addOnFailureListener(e -> {
                    submitBtn.setEnabled(true);
                    submitBtn.setText("Add Subscription");
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}