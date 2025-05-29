package com.example.predictibill.ui.subscriptions;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.predictibill.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class EditSubscriptionFragment extends Fragment {

    // UI Elements
    private TextView startDateDisplay, nextBillingDateDisplay, subscriptionIdNumber;
    private Spinner categorySpinner, statusSpinner, billingSpinner, paymentMethodSpinner;
    private EditText subscriptionNameEditText, subscriptionPriceEditText, noteEditText;
    private Button updateBtn;
    private String subscriptionId;

    // Dialog
    private Dialog dialog;
    private Button save_changes_btn, discard_btn;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // To hold the earliest next billing date allowed (based on start date)
    private Calendar nextBillingMinDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_edit_subscription, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Get subscription ID from arguments
        if (getArguments() != null) {
            subscriptionId = getArguments().getString("subscriptionId");
        }

        // Initialize UI elements
        initializeViews(view);

        // Set up back button
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Populate spinners
        populateSpinners();

        // Load subscription data
        loadSubscriptionData();

        // Set up date pickers
        setupDatePicker(view, R.id.start_date_container, startDateDisplay, "Select Start Date");
        setupDatePicker(view, R.id.next_billing_date_container, nextBillingDateDisplay, "Select Next Billing Date");

        // Set up form submission
        updateBtn.setOnClickListener(v -> {
            if (isFormValid()) {
                showConfirmationSubsDialog();
            } else {
                Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationSubsDialog() {
        if (getContext() == null) return;

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.edit_information_dialog_box);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Drawable dialog_box_bg = ContextCompat.getDrawable(getContext(), R.drawable.dialog_box_bg);
            dialog.getWindow().setBackgroundDrawable(dialog_box_bg);
        }

        dialog.setCancelable(false);

        save_changes_btn = dialog.findViewById(R.id.save_changes_btn);
        discard_btn = dialog.findViewById(R.id.discard_btn);

        save_changes_btn.setOnClickListener(v -> {
            dialog.dismiss();
            updateSubscriptionInFirestore();
        });

        discard_btn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
        noteEditText = view.findViewById(R.id.subscription_note_input);
        subscriptionIdNumber = view.findViewById(R.id.subscription_id_number);
        updateBtn = view.findViewById(R.id.update_sub_button);
    }

    private void populateSpinners() {
        populateSpinner(categorySpinner, Arrays.asList("Entertainment", "Productivity & Tools", "Cloud Storage", "Membership", "Food & Delivery"));
        populateSpinner(statusSpinner, Arrays.asList("Upcoming", "Overdue", "Paid", "Cancelled"));
        populateSpinner(billingSpinner, Arrays.asList("Monthly", "Quarterly", "Annually"));
        populateSpinner(paymentMethodSpinner, Arrays.asList("Cash","E-wallet (Gcash)", "Debit Card"));
    }

    private void populateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.dropdown_items);
        spinner.setAdapter(adapter);
    }

    private void loadSubscriptionData() {
        if (subscriptionId != null && !subscriptionId.isEmpty()) {
            subscriptionIdNumber.setText(subscriptionId);

            db.collection("subscriptions").document(subscriptionId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            subscriptionNameEditText.setText(documentSnapshot.getString("name"));

                            Double price = documentSnapshot.getDouble("price");
                            if (price != null) {
                                subscriptionPriceEditText.setText(String.valueOf(price));
                            }

                            noteEditText.setText(documentSnapshot.getString("note"));

                            String startDateStr = documentSnapshot.getString("startDate");
                            if (startDateStr != null) {
                                Calendar startDateCal = parseDateStringToCalendar(startDateStr);
                                startDateDisplay.setText(formatCalendarToDateString(startDateCal));
                                setNextBillingMinDate(startDateCal);
                            }

                            String nextBillingStr = documentSnapshot.getString("nextBillingDate");
                            if (nextBillingStr != null) {
                                Calendar nextBillingCal = parseDateStringToCalendar(nextBillingStr);
                                nextBillingDateDisplay.setText(formatCalendarToDateString(nextBillingCal));
                            }

                            setSpinnerSelection(categorySpinner, documentSnapshot.getString("category"));
                            setSpinnerSelection(statusSpinner, documentSnapshot.getString("status"));
                            setSpinnerSelection(billingSpinner, documentSnapshot.getString("billingCycle"));
                            setSpinnerSelection(paymentMethodSpinner, documentSnapshot.getString("paymentMethod"));
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error loading subscription: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        if (value == null) return;

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
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

                        // Format date to "MMMM d, yyyy"
                        String formattedDate = formatCalendarToDateString(selectedDate);
                        display.setText(formattedDate);
                        display.setTextColor(Color.BLACK);

                        if (containerId == R.id.start_date_container) {
                            setNextBillingMinDate(selectedDate);
                        }
                    },
                    year, month, day
            );

            if (containerId == R.id.next_billing_date_container && nextBillingMinDate != null) {
                datePickerDialog.getDatePicker().setMinDate(nextBillingMinDate.getTimeInMillis());
            }

            datePickerDialog.setTitle(title);
            datePickerDialog.show();
        });
    }

    private void setNextBillingMinDate(Calendar minDate) {
        nextBillingMinDate = (Calendar) minDate.clone();
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

    private void updateSubscriptionInFirestore() {
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid subscription ID", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = statusSpinner.getSelectedItem().toString();
        String startDateStr = startDateDisplay.getText().toString();
        String nextBillingDateStr = nextBillingDateDisplay.getText().toString();

        Calendar startDateCal = parseDateStringToCalendar(startDateStr);

        if ("Upcoming".equalsIgnoreCase(status)) {
            String billingCycle = billingSpinner.getSelectedItem().toString();
            Calendar calculatedNextBilling = calculateNextBillingDate(startDateCal, billingCycle);
            nextBillingDateStr = formatCalendarToDateString(calculatedNextBilling);
        }

        Map<String, Object> subscription = new HashMap<>();
        subscription.put("name", subscriptionNameEditText.getText().toString());

        try {
            subscription.put("price", Double.parseDouble(subscriptionPriceEditText.getText().toString()));
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        subscription.put("category", categorySpinner.getSelectedItem().toString());
        subscription.put("status", status);
        subscription.put("billingCycle", billingSpinner.getSelectedItem().toString());
        subscription.put("startDate", startDateStr);
        subscription.put("nextBillingDate", nextBillingDateStr);
        subscription.put("paymentMethod", paymentMethodSpinner.getSelectedItem().toString());
        subscription.put("note", noteEditText.getText().toString());
        subscription.put("updatedAt", Timestamp.now());

        updateBtn.setEnabled(false);

        db.collection("subscriptions").document(subscriptionId)
                .update(subscription)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Subscription updated!", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigate(R.id.action_editSubscriptionFragment_to_subscriptionsFragment);
                })
                .addOnFailureListener(e -> {
                    updateBtn.setEnabled(true);
                    updateBtn.setText("Update Subscription");
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private Calendar parseDateStringToCalendar(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (Exception e) {
            return Calendar.getInstance();
        }
    }

    private String formatCalendarToDateString(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    private Calendar calculateNextBillingDate(Calendar startDate, String billingCycle) {
        Calendar nextBillingDate = (Calendar) startDate.clone();

        switch (billingCycle.toLowerCase(Locale.ROOT)) {
            case "monthly":
                nextBillingDate.add(Calendar.MONTH, 1);
                break;
            case "quarterly":
                nextBillingDate.add(Calendar.MONTH, 3);
                break;
            case "annually":
                nextBillingDate.add(Calendar.YEAR, 1);
                break;
            default:
                nextBillingDate.add(Calendar.MONTH, 1);
                break;
        }

        return nextBillingDate;
    }
}