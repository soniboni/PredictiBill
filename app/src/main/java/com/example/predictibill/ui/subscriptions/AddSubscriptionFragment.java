package com.example.predictibill.ui.subscriptions;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddSubscriptionFragment extends Fragment {
    private TextView startDateDisplay, nextBillingDateDisplay;
    private Spinner categorySpinner, statusSpinner, billingSpinner, paymentMethodSpinner;
    private EditText subscriptionNameEditText, subscriptionPriceEditText;
    private ImageView previewImageView;
    private Uri selectedImageUri;

    public AddSubscriptionFragment() {
        super(R.layout.fragments_add_subscriptions);
    }

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

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        // Link input fields
        categorySpinner = view.findViewById(R.id.subscription_category_spinner);
        statusSpinner = view.findViewById(R.id.subscription_status_spinner);
        billingSpinner = view.findViewById(R.id.billing_cycle_spinner);
        paymentMethodSpinner = view.findViewById(R.id.payment_method_spinner);
        startDateDisplay = view.findViewById(R.id.start_date_display);
        nextBillingDateDisplay = view.findViewById(R.id.next_billing_date_display);
        subscriptionNameEditText = view.findViewById(R.id.subscription_name_input);
        subscriptionPriceEditText = view.findViewById(R.id.price_input);
        previewImageView = view.findViewById(R.id.previewImageView);

        // Image upload logic
        View imageUploadCard = view.findViewById(R.id.imageUploadCard);
        imageUploadCard.setOnClickListener(v -> openImagePicker());

        // Populate spinners
        populateSpinner(categorySpinner, List.of("Entertainment", "Productivity & Tools", "Cloud Storage", "Membership", "Food & Delivery"));
        populateSpinner(statusSpinner, List.of("Upcoming", "Overdue", "Paid", "Cancelled"));
        populateSpinner(billingSpinner, List.of("Monthly", "Quarterly", "Annually"));
        populateSpinner(paymentMethodSpinner, List.of("E-wallet (Gcash)", "Debit Card"));

        // Date pickers
        setupDatePicker(view, R.id.start_date_container, startDateDisplay, "Select Start Date");
        setupDatePicker(view, R.id.next_billing_date_container, nextBillingDateDisplay, "Select Next Billing Date");
        setNextBillingMinDate(Calendar.getInstance());

        // Submit button
        Button submitBtn = view.findViewById(R.id.add_sub_button);
        submitBtn.setOnClickListener(v -> {
            if (isFormValid()) {
                // Create subscription from inputs
                String name = subscriptionNameEditText.getText().toString();
                double price = Double.parseDouble(subscriptionPriceEditText.getText().toString());
                String category = categorySpinner.toString();
                String status = statusSpinner.toString();
                String billingCycle = billingSpinner.toString();
                String startDate = startDateDisplay.getText().toString();
                String dueDate = nextBillingDateDisplay.getText().toString();

                SubscriptionsFragment.Subscription newSub = new SubscriptionsFragment.Subscription(name, price, category, status, billingCycle, startDate, dueDate);

                Bundle bundle = new Bundle();
                bundle.putSerializable("new_subscription", newSub);

                Navigation.findNavController(v).navigate(R.id.action_addSubscriptions_to_subscriptionsFragment, bundle);
            } else {
                Toast.makeText(requireContext(), "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private void populateSpinner(Spinner spinner, List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(items));
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
        // Future: Restrict the next billing date based on selected start + cycle
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


}
