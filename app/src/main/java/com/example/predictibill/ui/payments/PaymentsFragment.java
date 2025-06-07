package com.example.predictibill.ui.payments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentsFragment extends Fragment implements PaymentsAdapter.OnPaymentClickListener {

    private RecyclerView recyclerView;
    private TextView noPaymentsTextView;
    private PaymentsAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private List<Subscription> paidSubscriptions = new ArrayList<>();
    private List<Subscription> filteredPaidSubscriptions = new ArrayList<>();

    // Filter chips
    private ChipGroup filterChipGroup;
    private Chip methodChip, categoryChip, billingCycleChip;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_payments, container, false);

        recyclerView = root.findViewById(R.id.payments_recycler_view);
        noPaymentsTextView = root.findViewById(R.id.noPaymentsTextView);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize filter chips
        filterChipGroup = root.findViewById(R.id.filter_chip_group);
        methodChip = root.findViewById(R.id.chip_method);
        categoryChip = root.findViewById(R.id.chip_category);
        billingCycleChip = root.findViewById(R.id.chip_billing_cycle);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PaymentsAdapter(filteredPaidSubscriptions, this);
        recyclerView.setAdapter(adapter);

        // Set up filter chip listeners
        setupFilterChips();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPaidSubscriptions();
    }

    private void loadPaidSubscriptions() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            showNoPaymentsMessage("Please sign in to view payment history");
            return;
        }

        String userId = currentUser.getUid();

        db.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Paid")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        paidSubscriptions.clear();
                        filteredPaidSubscriptions.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Subscription subscription = document.toObject(Subscription.class);

                            String firestoreId = document.getId();
                            subscription.setId(firestoreId);
                            subscription.setSubscriptionId(firestoreId);

                            // Update subscriptionId in Firestore only if not already correct
                            if (document.getString("subscriptionId") == null ||
                                    !document.getString("subscriptionId").equals(firestoreId)) {
                                db.collection("subscriptions")
                                        .document(firestoreId)
                                        .update("subscriptionId", firestoreId);
                            }

                            // Set last paid date
                            if (document.contains("updatedAt") && document.getTimestamp("updatedAt") != null) {
                                Date updatedDate = document.getTimestamp("updatedAt").toDate();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                subscription.setLastPaidDue(sdf.format(updatedDate));
                            }

                            paidSubscriptions.add(subscription);
                        }

                        filteredPaidSubscriptions.addAll(paidSubscriptions);
                        adapter.updateList(filteredPaidSubscriptions);

                        if (filteredPaidSubscriptions.isEmpty()) {
                            showNoPaymentsMessage("No payment history found");
                        } else {
                            displayPaidSubscriptions();
                        }
                    } else {
                        showNoPaymentsMessage("Failed to load payment history");
                    }
                });
    }

    private void setupFilterChips() {
        methodChip.setOnClickListener(v -> showMethodDropdownMenu(v));
        categoryChip.setOnClickListener(v -> showCategoryDropdownMenu(v));
        billingCycleChip.setOnClickListener(v -> showBillingCycleDropdownMenu(v));
    }

    private void showMethodDropdownMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchor);
        popupMenu.getMenuInflater().inflate(R.menu.method_filter_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            String selectedMethod = "";

            if (itemId == R.id.method_all) {
                resetFilters();
                return true;
            } else if (itemId == R.id.method_cash) {
                selectedMethod = "Cash";
            } else if (itemId == R.id.method_debit_card) {
                selectedMethod = "Debit Card";
            } else if (itemId == R.id.method_gcash) {
                selectedMethod = "E-wallet (Gcash)";
            }

            filterPayments("paymentMethod", selectedMethod);
            return true;
        });

        popupMenu.show();
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

            filterPayments("category", selectedCategory);
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

            filterPayments("billingCycle", selectedCycle);
            return true;
        });

        popupMenu.show();
    }

    private void resetFilters() {
        filteredPaidSubscriptions.clear();
        filteredPaidSubscriptions.addAll(paidSubscriptions);
        adapter.updateList(filteredPaidSubscriptions);
        updateEmptyState();
    }

    private void filterPayments(String filterType, String filterValue) {
        filteredPaidSubscriptions.clear();

        for (Subscription subscription : paidSubscriptions) {
            switch (filterType) {
                case "paymentMethod":
                    if (subscription.getPaymentMethod() != null &&
                            subscription.getPaymentMethod().equalsIgnoreCase(filterValue)) {
                        filteredPaidSubscriptions.add(subscription);
                    }
                    break;
                case "category":
                    if (subscription.getCategory() != null &&
                            subscription.getCategory().equalsIgnoreCase(filterValue)) {
                        filteredPaidSubscriptions.add(subscription);
                    }
                    break;
                case "billingCycle":
                    if (subscription.getBillingCycle() != null &&
                            subscription.getBillingCycle().equalsIgnoreCase(filterValue)) {
                        filteredPaidSubscriptions.add(subscription);
                    }
                    break;
            }
        }

        adapter.updateList(filteredPaidSubscriptions);
        updateEmptyState();
    }

    private void displayPaidSubscriptions() {
        noPaymentsTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showNoPaymentsMessage(String message) {
        noPaymentsTextView.setText(message);
        noPaymentsTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void updateEmptyState() {
        if (filteredPaidSubscriptions.isEmpty()) {
            showNoPaymentsMessage("No payments match the selected filter");
        } else {
            displayPaidSubscriptions();
        }
    }

    @Override
    public void onPaymentClick(Subscription subscription) {
        // Handle payment item click if needed
    }
}