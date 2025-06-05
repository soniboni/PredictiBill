package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.predictibill.models.Subscription;

import com.example.predictibill.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SubscriptionsFragment extends Fragment implements SubscriptionAdapter.OnSubscriptionClickListener {

    private RecyclerView subscriptionsRecyclerView;
    private TextView noSubscriptionsTextView;
    private SubscriptionAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Subscription> subscriptionList = new ArrayList<>();
    private List<Subscription> filteredList = new ArrayList<>();

    // Filter chips
    private ChipGroup filterChipGroup;
    private Chip categoryChip, statusChip, billingCycleChip;

    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        subscriptionsRecyclerView = root.findViewById(R.id.subscription_recycler_view);
        noSubscriptionsTextView = root.findViewById(R.id.noSubscriptionsTextView);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize filter chips
        filterChipGroup = root.findViewById(R.id.filter_chip_group);
        categoryChip = root.findViewById(R.id.chip_method);
        statusChip = root.findViewById(R.id.chip_category);
        billingCycleChip = root.findViewById(R.id.chip_billing_cycle);

        // Setup RecyclerView
        subscriptionsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SubscriptionAdapter(filteredList, this);
        subscriptionsRecyclerView.setAdapter(adapter);

        Button submitButton = root.findViewById(R.id.submit_button);
        submitButton.setOnClickListener(this::onAddButtonClicked);

        // Set up filter chip listeners
        setupFilterChips();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchUserSubscriptions();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Refresh data when fragment becomes visible
        fetchUserSubscriptions();
    }

    private void fetchUserSubscriptions() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showNoSubscriptionsMessage("Please sign in to view subscriptions");
            return;
        }

        String userId = currentUser.getUid();

        db.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        subscriptionList.clear();
                        filteredList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Subscription subscription = document.toObject(Subscription.class);
                            subscription.setId(document.getId());
                            subscriptionList.add(subscription);
                        }

                        // Initially show all subscriptions
                        filteredList.addAll(subscriptionList);

                        if (filteredList.isEmpty()) {
                            showNoSubscriptionsMessage("No subscriptions found. Add your first subscription!");
                        } else {
                            displaySubscriptions();
                        }
                    } else {
                        showNoSubscriptionsMessage("Failed to load subscriptions");
                        Toast.makeText(getContext(), "Error loading subscriptions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupFilterChips() {
        categoryChip.setOnClickListener(v -> showCategoryDropdownMenu(v));
        statusChip.setOnClickListener(v -> showStatusDropdownMenu(v));
        billingCycleChip.setOnClickListener(v -> showBillingCycleDropdownMenu(v));
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
        updateEmptyState();
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

        adapter.updateList(filteredList);
        updateEmptyState();
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

    private void updateEmptyState() {
        if (filteredList.isEmpty()) {
            showNoSubscriptionsMessage("No subscriptions match the selected filter");
        } else {
            displaySubscriptions();
        }
    }

    private void onAddButtonClicked(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
    }

    @Override
    public void onSubscriptionClick(Subscription subscription) {
        navigateToDetailsFragment(subscription);
    }

    private String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        try {
            Date date = inputDateFormat.parse(dateStr);
            if (date != null) {
                return outputDateFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr; // fallback to original if parsing fails
    }

    private void navigateToDetailsFragment(Subscription subscription) {
        int destinationId = R.id.navigation_subscription_details_upcoming; // default

        if (subscription.getStatus() != null) {
            switch (subscription.getStatus()) {
                case "Paid":
                    destinationId = R.id.navigation_subscription_details_paid;
                    break;
                case "Overdue":
                    destinationId = R.id.navigation_subscription_details_overdue;
                    break;
                case "Cancelled":
                    destinationId = R.id.navigation_subscription_details_cancelled;
                    break;
            }
        }

        // Format the dates before passing
        String formattedStartDate = formatDateString(subscription.getStartDate());
        String formattedNextBillingDate = formatDateString(subscription.getNextBillingDate());

        // Create bundle with subscription data
        Bundle args = new Bundle();
        args.putString("subscriptionId", subscription.getId());
        args.putString("name", subscription.getName());
        args.putString("note", subscription.getNote());
        args.putString("category", subscription.getCategory());
        args.putString("status", subscription.getStatus());
        args.putString("startDate", formattedStartDate);
        args.putString("nextBillingDate", formattedNextBillingDate);
        args.putString("billingCycle", subscription.getBillingCycle());
        args.putDouble("price", subscription.getPrice());
        args.putString("paymentMethod", subscription.getPaymentMethod());
        args.putString("lastPaidDue", subscription.getLastPaidDue());

        // Navigate to the appropriate fragment
        Navigation.findNavController(requireView()).navigate(destinationId, args);
    }
}