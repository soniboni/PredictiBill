package com.example.predictibill.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private TextView greetingTxt, expectedMoneyTxt, spentMoneyTxt, percentTxt, homeDateTxt;
    private TextView[] rectangleViews;

    private RecyclerView upcomingBillsRecyclerView;
    private RecyclerView overdueBillsRecyclerView;
    private HomeAdapter upcomingBillsAdapter;
    private HomeAdapter overdueBillsAdapter;
    private List<Subscription> upcomingBillsList = new ArrayList<>();
    private List<Subscription> overdueBillsList = new ArrayList<>();

    private double totalExpected = 0.0;
    private double totalPaid = 0.0;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        greetingTxt = view.findViewById(R.id.home_hi_lbl);
        expectedMoneyTxt = view.findViewById(R.id.home_expectedmoney_txt);
        spentMoneyTxt = view.findViewById(R.id.home_spentmoney_txt);
        percentTxt = view.findViewById(R.id.home_percent_txt);
        homeDateTxt = view.findViewById(R.id.home_date_txt);

        rectangleViews = new TextView[]{
                view.findViewById(R.id.home_rectangle_2),
                view.findViewById(R.id.home_rectangle_3),
                view.findViewById(R.id.home_rectangle_4),
                view.findViewById(R.id.home_rectangle_5),
                view.findViewById(R.id.home_rectangle_6),
                view.findViewById(R.id.home_rectangle_7),
                view.findViewById(R.id.home_rectangle_8),
                view.findViewById(R.id.home_rectangle_9),
        };

        upcomingBillsRecyclerView = view.findViewById(R.id.upcoming_bill_container);
        overdueBillsRecyclerView = view.findViewById(R.id.overdue_bills_container);

        setupRecyclerViews();

        return view;
    }

    private void setupRecyclerViews() {
        upcomingBillsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        upcomingBillsAdapter = new HomeAdapter(upcomingBillsList);
        upcomingBillsRecyclerView.setAdapter(upcomingBillsAdapter);

        overdueBillsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        overdueBillsAdapter = new HomeAdapter(overdueBillsList);
        overdueBillsRecyclerView.setAdapter(overdueBillsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set today's date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        String today = dateFormat.format(new Date());
        homeDateTxt.setText(today);

        fetchUserData();
        fetchAndDisplaySubscriptions();
    }

    private void fetchUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                greetingTxt.setText(String.format(" Hi, %s!", displayName));
            } else {
                String email = currentUser.getEmail();
                if (email != null) {
                    String username = email.split("@")[0];
                    greetingTxt.setText(String.format(" Hi, %s!", username));
                } else {
                    greetingTxt.setText(" Hi, User!");
                }
            }
        }
    }

    private void fetchAndDisplaySubscriptions() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getActivity(), "Please sign in to view subscriptions", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        totalExpected = 0.0;
        totalPaid = 0.0;
        upcomingBillsList.clear();
        overdueBillsList.clear();

        for (TextView view : rectangleViews) {
            view.setText("");
        }

        db.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int i = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            String status = doc.getString("status");
                            Double price = doc.getDouble("price");
                            String nextBillingDate = doc.getString("nextBillingDate");
                            String billingCycle = doc.getString("billingCycle");
                            String category = doc.getString("category");

                            Subscription subscription = new Subscription();
                            subscription.setName(name);
                            subscription.setStatus(status);
                            subscription.setPrice(price != null ? price : 0.0);
                            subscription.setNextBillingDate(nextBillingDate);
                            subscription.setBillingCycle(billingCycle);
                            subscription.setCategory(category);

                            if (price != null) {
                                if ("Paid".equalsIgnoreCase(status)) {
                                    totalPaid += price;
                                }
                                totalExpected += price;
                            }

                            if (isOverdue(status)) {
                                overdueBillsList.add(subscription);
                            } else if (isUpcoming(status)) {
                                upcomingBillsList.add(subscription);
                            }

                            if (i < rectangleViews.length && name != null) {
                                rectangleViews[i].setText(name);
                            }
                            i++;

                            Log.d(TAG, "Subscription: " + name +
                                    " | Price: " + price +
                                    " | Status: " + status);
                        }

                        upcomingBillsAdapter.updateList(upcomingBillsList);
                        overdueBillsAdapter.updateList(overdueBillsList);

                        updateUI();
                    } else {
                        Log.e(TAG, "Error getting subscriptions: ", task.getException());
                        Toast.makeText(getActivity(), "Failed to load subscriptions", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 🔄 Simplified status-based methods
    private boolean isOverdue(String status) {
        return status != null && status.equalsIgnoreCase("Overdue");
    }

    private boolean isUpcoming(String status) {
        return status != null && status.equalsIgnoreCase("Upcoming");
    }

    private void updateUI() {
        Locale locale = Locale.getDefault();
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);

        expectedMoneyTxt.setText(currencyFormat.format(totalExpected));
        spentMoneyTxt.setText(currencyFormat.format(totalPaid));

        double percent = (totalExpected == 0) ? 0 : (totalPaid / totalExpected) * 100;
        percentTxt.setText(String.format(Locale.getDefault(), "%.2f%%", percent));
    }
}
