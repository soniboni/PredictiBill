package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionsFragment extends Fragment implements SubscriptionAdapter.OnSubscriptionClickListener {

    private RecyclerView subscriptionsRecyclerView;
    private TextView noSubscriptionsTextView;
    private SubscriptionAdapter adapter;
    private FirebaseFirestore db;
    private List<Subscription> subscriptionList = new ArrayList<>();
    private List<Subscription> filteredList = new ArrayList<>();

    // Filter chips
    private ChipGroup filterChipGroup;
    private Chip categoryChip, statusChip, billingCycleChip;

    // Dummy user ID for development
    private final String dummyUserId = "dummy_user_123";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        subscriptionsRecyclerView = root.findViewById(R.id.subscription_recycler_view);
        noSubscriptionsTextView = root.findViewById(R.id.noSubscriptionsTextView);

        // Initialize filter chips
        filterChipGroup = root.findViewById(R.id.filter_chip_group);
        categoryChip = root.findViewById(R.id.chip_method);
        statusChip = root.findViewById(R.id.chip_category);
        billingCycleChip = root.findViewById(R.id.chip_billing_cycle);

        db = FirebaseFirestore.getInstance();

        // Setup RecyclerView with click listener
        subscriptionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubscriptionAdapter(filteredList, this);
        subscriptionsRecyclerView.setAdapter(adapter);

        Button submitButton = root.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this::onAddButtonClicked);

        // Set up filter chip listeners
        setupFilterChips();

        return root;
    }

    private void setupFilterChips() {
        categoryChip.setOnClickListener(v -> {
            showCategoryDropdownMenu(v);
        });

        statusChip.setOnClickListener(v -> {
            showStatusDropdownMenu(v);
        });

        billingCycleChip.setOnClickListener(v -> {
            showBillingCycleDropdownMenu(v);
        });
    }

    private void showCategoryDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.category_filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            String selectedCategory = "";

            if (itemId == R.id.category_all) {
                resetFilters();
                return true;
            } else if (itemId == R.id.category_entertainment) {
                selectedCategory = "Entertainment";
            } else if (itemId == R.id.category_productivity) {
                selectedCategory = "Productivity & Tools";
            } else if (itemId == R.id.category_cloud) {
                selectedCategory = "Cloud Storage";
            } else if (itemId == R.id.category_membership) {
                selectedCategory = "Membership";
            } else if (itemId == R.id.category_food) {
                selectedCategory = "Food Delivery";
            }

            filterSubscriptions("category", selectedCategory);
            return true;
        });

        popupMenu.show();
    }

    private void showStatusDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.status_filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            String selectedStatus = "";

            if (itemId == R.id.status_all) {
                resetFilters();
                return true;
            } else if (itemId == R.id.status_upcoming) {
                selectedStatus = "Upcoming";
            } else if (itemId == R.id.status_overdue) {
                selectedStatus = "Overdue";
            } else if (itemId == R.id.status_paid) {
                selectedStatus = "Paid";
            } else if (itemId == R.id.status_cancelled) {
                selectedStatus = "Cancelled";
            }

            filterSubscriptions("status", selectedStatus);
            return true;
        });

        popupMenu.show();
    }

    private void showBillingCycleDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.billing_cycle_filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            String selectedCycle = "";

            if (itemId == R.id.cycle_all) {
                resetFilters();
                return true;
            } else if (itemId == R.id.cycle_monthly) {
                selectedCycle = "Monthly";
            } else if (itemId == R.id.cycle_quarterly) {
                selectedCycle = "Quarterly";
            } else if (itemId == R.id.cycle_anually) {
                selectedCycle = "Annually";
            }

            filterSubscriptions("billingCycle", selectedCycle);
            return true;
        });

        popupMenu.show();
    }

    private void resetFilters() {
        filteredList.clear();
        filteredList.addAll(subscriptionList);
        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            showNoSubscriptionsMessage("No current subscriptions listed.");
        } else {
            displaySubscriptions();
        }
    }

    private void filterSubscriptions(String filterType, String filterValue) {
        filteredList.clear();

        for (Subscription subscription : subscriptionList) {
            switch (filterType) {
                case "category":
                    if (subscription.getCategory().equalsIgnoreCase(filterValue)) {
                        filteredList.add(subscription);
                    }
                    break;
                case "status":
                    if (subscription.getStatus().equalsIgnoreCase(filterValue)) {
                        filteredList.add(subscription);
                    }
                    break;
                case "billingCycle":
                    if (subscription.getBillingCycle().equalsIgnoreCase(filterValue)) {
                        filteredList.add(subscription);
                    }
                    break;
            }
        }

        if (filteredList.isEmpty()) {
            showNoSubscriptionsMessage("No subscriptions match the selected filter.");
        } else {
            displaySubscriptions();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDummyUserSubscriptions();
    }

    private void loadDummyUserSubscriptions() {
        fetchUserSubscriptions(dummyUserId);
    }

    private void fetchUserSubscriptions(String uid) {
        db.collection("subscriptions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        subscriptionList.clear();
                        filteredList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Subscription subscription = document.toObject(Subscription.class);
                            subscription.setId(document.getId());
                            subscriptionList.add(subscription);
                            filteredList.add(subscription); // Initially show all subscriptions
                        }
                        if (filteredList.isEmpty()) {
                            showNoSubscriptionsMessage("No current subscriptions listed.");
                        } else {
                            displaySubscriptions();
                        }
                    } else {
                        showNoSubscriptionsMessage("Failed to load subscriptions.");
                    }
                });
    }

    private void displaySubscriptions() {
        noSubscriptionsTextView.setVisibility(View.GONE);
        subscriptionsRecyclerView.setVisibility(View.VISIBLE);
        adapter.updateList(filteredList);
    }

    private void showNoSubscriptionsMessage(String message) {
        noSubscriptionsTextView.setText(message);
        noSubscriptionsTextView.setVisibility(View.VISIBLE);
        subscriptionsRecyclerView.setVisibility(View.GONE);
    }

    private void onAddButtonClicked(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
    }

    @Override
    public void onSubscriptionClick(Subscription subscription) {
        navigateToDetailsFragment(subscription);
    }

    private void navigateToDetailsFragment(Subscription subscription) {
        int destinationId;

        switch (subscription.getStatus()) {
            case "Upcoming":
                destinationId = R.id.navigation_subscription_details_upcoming;
                break;
            case "Paid":
                destinationId = R.id.navigation_subscription_details_paid;
                break;
            case "Overdue":
                destinationId = R.id.navigation_subscription_details_overdue;
                break;
            case "Cancelled":
                destinationId = R.id.navigation_subscription_details_cancelled;
                break;
            default:
                destinationId = R.id.navigation_subscription_details_upcoming;
        }

        // Create bundle with subscription data
        Bundle args = new Bundle();
        args.putString("subscriptionId", subscription.getId());
        args.putString("name", subscription.getName());
        args.putString("note", subscription.getNote());
        args.putString("category", subscription.getCategory());
        args.putString("status", subscription.getStatus());
        args.putString("startDate", subscription.getStartDate());
        args.putString("nextBillingDate", subscription.getNextBillingDate());
        args.putString("billingCycle", subscription.getBillingCycle());
        args.putDouble("price", subscription.getPrice());
        args.putString("paymentMethod", subscription.getPaymentMethod());
        args.putString("lastPaidDue", subscription.getLastPaidDue());

        // Navigate to the appropriate fragment
        Navigation.findNavController(requireView())
                .navigate(destinationId, args);
    }
}