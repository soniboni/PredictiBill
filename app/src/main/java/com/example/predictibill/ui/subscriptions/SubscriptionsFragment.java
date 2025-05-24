package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;

import java.io.Serializable;
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

        Button Add_subButton = view.findViewById(R.id.submit_button);
        Add_subButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
        });
    }

    public static class Subscription implements Serializable {
        private String name;
        private double price;
        private String category;
        private String status;
        private String billingCycle;

        public Subscription(String name, double price, String category, String status, String billingCycle) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.status = status;
            this.billingCycle = billingCycle;
        }

        // Getters
        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getBillingCycle() { return billingCycle; }
    }


}
