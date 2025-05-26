package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.predictibill.R;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionsFragment extends Fragment {

    // Shared list of subscriptions accessible across fragments
    public static List<Subscription> subscriptionList = new ArrayList<>();

    // Static block to populate the list with initial dummy data
    static {
        subscriptionList.add(new Subscription("#PAY0001", "Netflix", 299.0, "Entertainment", "Upcoming", "Monthly",
                "April 1, 2025", "May 1, 2025", "Credit Card", "Watch on weekends only"));
        subscriptionList.add(new Subscription("#PAY0002", "Spotify", 149.0, "Music", "Upcoming", "Monthly",
                "April 16, 2025", "May 16, 2025", "E-wallet", "Premium plan"));
    }

    // Default constructor (required for Fragment)
    public SubscriptionsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate and return the layout for this fragment
        return inflater.inflate(R.layout.fragment_subscriptions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Check if a new subscription was passed via arguments
        if (getArguments() != null && getArguments().containsKey("new_subscription")) {
            // Get the passed subscription and add it to the list
            Subscription newSub = (Subscription) getArguments().getSerializable("new_subscription");
            subscriptionList.add(newSub);
        }

        // Set up RecyclerView to display subscriptions
        RecyclerView recyclerView = view.findViewById(R.id.subscription_recycler_view);
        SubscriptionAdapter adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up button click to navigate to AddSubscription screen
        Button addButton = view.findViewById(R.id.submit_button);
        addButton.setOnClickListener(v -> {
            // Navigate to add subscription screen using Navigation component
            Navigation.findNavController(view).navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
        });
    }

    /**
     * Model class for storing subscription details.
     * Implements Serializable so it can be passed in fragment arguments.
     */
    public static class Subscription implements Serializable {
        private final String subscriptionId;  // Unique subscription identifier
        private final String name;            // Name of the service (e.g., Netflix)
        private final double price;           // Cost of subscription
        private final String category;        // Category (e.g., Music, Entertainment)
        private final String status;          // Status (e.g., Upcoming, Paid)
        private final String billingCycle;    // Billing frequency (e.g., Monthly)
        private final String startDate;       // Subscription start date
        private final String dueDate;         // Next due date
        private final String paymentMethod;   // Payment method used
        private final String note;            // Additional notes

        public Subscription(String subscriptionId, String name, double price, String category, String status, String billingCycle,
                            String startDate, String dueDate, String paymentMethod, String note) {
            this.subscriptionId = subscriptionId;
            this.name = name;
            this.price = price;
            this.category = category;
            this.status = status;
            this.billingCycle = billingCycle;
            this.startDate = startDate;
            this.dueDate = dueDate;
            this.paymentMethod = paymentMethod;
            this.note = note;
        }

        // Getter methods for accessing subscription details
        public String getSubscriptionId() { return subscriptionId; }
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getBillingCycle() { return billingCycle; }
        public String getStartDate() { return startDate; }
        public String getDueDate() { return dueDate; }
        public String getPaymentMethod() { return paymentMethod; }
        public String getNote() { return note; }
    }
}
