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

    public SubscriptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscriptions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Subscription> subscriptionList = new ArrayList<>();

        // Dummy data
        subscriptionList.add(new Subscription("Netflix", 299.0, "Entertainment", "Upcoming", "Monthly",
                "April 1, 2025", "May 1, 2025", "Credit Card", "Watch on weekends only"));

        subscriptionList.add(new Subscription("Spotify", 149.0, "Music", "Upcoming", "Monthly",
                "April 16, 2025", "May 16, 2025", "E-wallet", "Premium plan"));

        // Handle passed subscription from addSubscription screen
        if (getArguments() != null && getArguments().containsKey("new_subscription")) {
            Subscription newSub = (Subscription) getArguments().getSerializable("new_subscription");
            subscriptionList.add(newSub);
        }

        RecyclerView recyclerView = view.findViewById(R.id.subscription_recycler_view);
        SubscriptionAdapter adapter = new SubscriptionAdapter(subscriptionList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Button to add new subscriptions
        Button addButton = view.findViewById(R.id.submit_button);
        addButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
        });
    }

    public static class Subscription implements Serializable {
        private final String name;
        private final double price;
        private final String category;
        private final String status;
        private final String billingCycle;
        private final String startDate;
        private final String dueDate;
        private final String paymentMethod;
        private final String note;

        public Subscription(String name, double price, String category, String status, String billingCycle,
                            String startDate, String dueDate, String paymentMethod, String note) {
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