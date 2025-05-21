package com.example.predictibill.ui.subscriptions;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddSubscriptionFragment extends Fragment {
    public AddSubscriptionFragment() {
        super(R.layout.fragments_add_subscriptions);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigateUp();
        });

        // Spinner for Subscription Category
        Spinner categorySpinner = view.findViewById(R.id.subscription_category_spinner);
        List<String> categoryList = new ArrayList<>();
        categoryList.add("Entertainment");
        categoryList.add("Productivity & Tools");
        categoryList.add("Cloud Storage");
        categoryList.add("Membership");
        categoryList.add("Food & Delivery");

        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryList
        );
        adapterCategory.setDropDownViewResource(R.layout.dropdown_items);
        categorySpinner.setAdapter(adapterCategory);

        // Spinner for Subscription Status
        Spinner statusSpinner = view.findViewById(R.id.subscription_status_spinner);
        List<String> statusList = new ArrayList<>();
        statusList.add("Upcoming");
        statusList.add("Overdue");
        statusList.add("Paid");
        statusList.add("Cancelled");

        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                statusList
        );
        adapterStatus.setDropDownViewResource(R.layout.dropdown_items);
        statusSpinner.setAdapter(adapterStatus);

        // Spinner for Billing Cycle
        Spinner billingSpinner = view.findViewById(R.id.billing_cycle_spinner);
        List<String> billingList = new ArrayList<>();
        billingList.add("Monthly");
        billingList.add("Annually");

        ArrayAdapter<String> adapterBilling = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                billingList
        );
        adapterBilling.setDropDownViewResource(R.layout.dropdown_items);
        billingSpinner.setAdapter(adapterBilling);

        setupDatePicker(view, R.id.start_date_container, R.id.start_date_display, "Select Start Date");
        setupDatePicker(view, R.id.next_billing_date_container, R.id.next_billing_date_display, "Select Next Billing Date");

        // Set initial minimum date for next billing (today)
        setNextBillingMinDate(Calendar.getInstance());
    }

    private void setupDatePicker(View view, int containerId, int displayId, String title) {
        LinearLayout dateContainer = view.findViewById(containerId);
        TextView dateDisplay = view.findViewById(displayId);

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
                        dateDisplay.setText(formattedDate);
                        dateDisplay.setTextColor(Color.BLACK);

                        // If this is the start date picker, update next billing minimum date
                        if (containerId == R.id.start_date_container) {
                            setNextBillingMinDate(selectedDate);
                        }
                    },
                    year, month, day
            );

            // Set dialog title
            datePickerDialog.setTitle(title);
            datePickerDialog.show();
        });
    }

    private void setNextBillingMinDate(Calendar minDate) {
        LinearLayout nextBillingContainer = getView().findViewById(R.id.next_billing_date_container);
        TextView nextBillingDisplay = getView().findViewById(R.id.next_billing_date_display);

    }
}