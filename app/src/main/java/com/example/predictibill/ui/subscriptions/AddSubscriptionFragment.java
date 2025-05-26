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
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import com.example.predictibill.R;
import java.util.*;

/**
 * Fragment for adding a new subscription.
 * Handles form inputs, image selection, and date picking before navigating back with the result.
 */
public class AddSubscriptionFragment extends Fragment {

    // UI
    private TextView startDateDisplay, nextBillingDateDisplay;
    private Spinner categorySpinner, statusSpinner, billingSpinner, paymentMethodSpinner;
    private EditText subscriptionNameEditText, subscriptionPriceEditText, noteEditText;
    private ImageView previewImageView;

    // URI to hold the selected image
    private Uri selectedImageUri;

    public AddSubscriptionFragment() {
        super(R.layout.fragments_add_subscriptions); // Link fragment to its layout
    }

    // Register a launcher for image picking from gallery
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData(); // Store selected image URI
                    previewImageView.setImageURI(selectedImageUri); // Display selected image
                    Toast.makeText(requireContext(), "Image selected!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragments_add_subscriptions, container, false); // Inflate layout
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up back button to navigate back
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Link form fields with XML
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

        // Image upload card click triggers gallery picker
        View imageUploadCard = view.findViewById(R.id.imageUploadCard);
        imageUploadCard.setOnClickListener(v -> openImagePicker());

        // Fill spinners with predefined options (To be changed once may db)
        populateSpinner(categorySpinner, List.of("Entertainment", "Productivity & Tools", "Cloud Storage", "Membership", "Food & Delivery"));
        populateSpinner(statusSpinner, List.of("Upcoming", "Overdue", "Paid", "Cancelled"));
        populateSpinner(billingSpinner, List.of("Monthly", "Quarterly", "Annually"));
        populateSpinner(paymentMethodSpinner, List.of("E-wallet (Gcash)", "Debit Card"));

        // Setup date pickers for start date and next billing date
        setupDatePicker(view, R.id.start_date_container, startDateDisplay, "Select Start Date");
        setupDatePicker(view, R.id.next_billing_date_container, nextBillingDateDisplay, "Select Next Billing Date");

        // for constraint of billing but waley pa siya now:p
        setNextBillingMinDate(Calendar.getInstance());

        // Handle the submission of the form
        Button submitBtn = view.findViewById(R.id.add_sub_button);
        submitBtn.setOnClickListener(v -> {
            if (isFormValid()) {
                // Collect input values
                String name = subscriptionNameEditText.getText().toString();
                double price = Double.parseDouble(subscriptionPriceEditText.getText().toString());
                String category = categorySpinner.getSelectedItem().toString();
                String status = statusSpinner.getSelectedItem().toString();
                String billingCycle = billingSpinner.getSelectedItem().toString();
                String startDate = startDateDisplay.getText().toString();
                String dueDate = nextBillingDateDisplay.getText().toString();
                String paymentMethod = paymentMethodSpinner.getSelectedItem().toString();
                String note = noteEditText.getText().toString();

                // Generate a unique ID for the new subscription (to be change rin once may db)
                String subscriptionId = "#PAY" + System.currentTimeMillis();

                // Create a subscription object to pass to the next fragment
                SubscriptionsFragment.Subscription newSub = new SubscriptionsFragment.Subscription(
                        subscriptionId, name, price, category, status, billingCycle, startDate, dueDate, paymentMethod, note
                );

                // Send the new subscription to the SubscriptionsFragment using a Bundle
                Bundle bundle = new Bundle();
                bundle.putSerializable("new_subscription", newSub);
                Navigation.findNavController(v).navigate(R.id.action_addSubscriptions_to_subscriptionsFragment, bundle);
            } else {
                Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Launches the image picker for selecting a subscription image.
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    /**
     * Populates a Spinner with a list of string items.
     */
    private void populateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(items));
        adapter.setDropDownViewResource(R.layout.dropdown_items);
        spinner.setAdapter(adapter);
    }

    /**
     * Sets up a date picker dialog for a date field.
     */
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

                        // When start date changes, adjust constraints for billing date
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

    /**
     * for constraint of maxBilling date pero waley pa rin siya hehe
     */
    private void setNextBillingMinDate(Calendar minDate) {
        // Placeholder for logic that restricts the next billing date based on start date + cycle
    }

    /**
     * Validates if all required fields are filled in.
     */
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
}
