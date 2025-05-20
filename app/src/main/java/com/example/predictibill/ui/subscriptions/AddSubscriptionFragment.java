package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.R;

import java.util.ArrayList;
import java.util.List;

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

        Spinner billingSpinner = view.findViewById(R.id.billing_cycle_spinner);
        List<String> billingList = new ArrayList<>();
        billingList.add("Monthly");
        billingList.add("Annualy");


        ArrayAdapter<String> adapterBilling = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                billingList
        );
        adapterBilling.setDropDownViewResource(R.layout.dropdown_items);
       billingSpinner.setAdapter(adapterBilling);
    }
}
