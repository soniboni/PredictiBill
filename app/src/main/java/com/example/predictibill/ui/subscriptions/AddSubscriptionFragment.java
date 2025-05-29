package com.example.predictibill.ui.subscriptions;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.predictibill.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.*;

public class AddSubscriptionFragment extends Fragment {

    // UI Elements
    private TextView startDateDisplay, nextBillingDateDisplay;
    private Spinner categorySpinner, statusSpinner, billingSpinner, paymentMethodSpinner;
    private EditText subscriptionNameEditText, subscriptionPriceEditText, noteEditText;
    private Button submitBtn;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // To hold the earliest next billing date allowed (based on start date)
    private Calendar nextBillingMinDate;

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
        noteEditText = view.findViewById(R.id.subscription_note_input);
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

                        // Format date as "March 29, 2025"
                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
                        String formattedDate = sdf.format(selectedDate.getTime());

                        display.setText(formattedDate);
                        display.setTextColor(Color.BLACK);

                        if (containerId == R.id.start_date_container) {
                            setNextBillingMinDate(selectedDate);
                            // Optionally, reset next billing date when start date changes
                            nextBillingDateDisplay.setText("");
                        }
                    },
                    year, month, day
            );

            // If nextBillingMinDate is set, limit the minimum date selectable for next billing date
            if (containerId == R.id.next_billing_date_container && nextBillingMinDate != null) {
                datePickerDialog.getDatePicker().setMinDate(nextBillingMinDate.getTimeInMillis());
            }

            datePickerDialog.setTitle(title);
            datePickerDialog.show();
        });
    }

    private void setNextBillingMinDate(Calendar minDate) {
        nextBillingMinDate = (Calendar) minDate.clone();
        // The next billing date must be at least the start date or later
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
        String status = statusSpinner.getSelectedItem().toString();
        String startDateStr = startDateDisplay.getText().toString();
        String nextBillingDateStr = nextBillingDateDisplay.getText().toString();

        // Parse startDate string to Calendar
        Calendar startDateCal = parseDateStringToCalendar(startDateStr);

        // Calculate nextBillingDate based on billing cycle and start date if status is "Upcoming"
        if ("Upcoming".equalsIgnoreCase(status)) {
            String billingCycle = billingSpinner.getSelectedItem().toString();
            Calendar calculatedNextBilling = calculateNextBillingDate(startDateCal, billingCycle);
            nextBillingDateStr = formatCalendarToDateString(calculatedNextBilling);
        }

        Map<String, Object> subscription = new HashMap<>();
        subscription.put("name", subscriptionNameEditText.getText().toString());
        subscription.put("price", Double.parseDouble(subscriptionPriceEditText.getText().toString()));
        subscription.put("category", categorySpinner.getSelectedItem().toString());
        subscription.put("status", status);
        subscription.put("billingCycle", billingSpinner.getSelectedItem().toString());
        subscription.put("startDate", startDateStr);
        subscription.put("nextBillingDate", nextBillingDateStr);
        subscription.put("paymentMethod", paymentMethodSpinner.getSelectedItem().toString());
        subscription.put("note", noteEditText.getText().toString());
        subscription.put("createdAt", new Date());

        submitBtn.setEnabled(false);
        submitBtn.setText("Saving...");

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

    private Calendar parseDateStringToCalendar(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (Exception e) {
            // If parsing fails, return current date as fallback
            return Calendar.getInstance();
        }
    }

    private String formatCalendarToDateString(Calendar cal) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        return sdf.format(cal.getTime());
    }

    private Calendar calculateNextBillingDate(Calendar startDate, String billingCycle) {
        Calendar nextDate = (Calendar) startDate.clone();
        switch (billingCycle.toLowerCase()) {
            case "monthly":
                nextDate.add(Calendar.MONTH, 1);
                break;
            case "quarterly":
                nextDate.add(Calendar.MONTH, 3);
                break;
            case "annually":
                nextDate.add(Calendar.YEAR, 1);
                break;
            default:
                // If unknown billing cycle, just add one month as fallback
                nextDate.add(Calendar.MONTH, 1);
        }
        return nextDate;
    }
}
