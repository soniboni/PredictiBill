package com.example.predictibill.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.predictibill.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FirebaseFirestore db;
    private TextView expectedMoneyTxt, spentMoneyTxt, percentTxt;
    private double totalExpected = 0.0;
    private double totalPaid = 0.0;

    private static final String TAG = "HomeFragment";

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews
        expectedMoneyTxt = view.findViewById(R.id.home_expectedmoney_txt);
        spentMoneyTxt = view.findViewById(R.id.home_spentmoney_txt);
        percentTxt = view.findViewById(R.id.home_percent_txt);

        // Fetch data from Firestore
        fetchBillingData();

        return view;
    }

    private void fetchBillingData() {
        // Reset totals
        totalExpected = 0.0;
        totalPaid = 0.0;

        // Get all subscriptions instead of bills
        db.collection("subscriptions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            Log.d(TAG, "No subscriptions found in Firestore.");
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, "Document data: " + document.getData());

                            // Get price and status
                            Double price = document.getDouble("price");
                            String status = document.getString("status");

                            if (price == null) {
                                Log.w(TAG, "Missing or invalid 'price' in document: " + document.getId());
                                continue;
                            }

                            totalExpected += price;

                            if (status != null && status.equalsIgnoreCase("Paid")) {
                                totalPaid += price;
                            }
                        }

                        // Update UI with formatted values
                        updateUI();

                    } else {
                        Log.e(TAG, "Task failed: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore query failed: ", e));
    }

    private void updateUI() {
        // Format currency (Philippine Peso)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        currencyFormat.setMaximumFractionDigits(2);

        // Set formatted values to TextViews
        expectedMoneyTxt.setText(currencyFormat.format(totalExpected));
        spentMoneyTxt.setText(currencyFormat.format(totalPaid));

        // Calculate and display percentage
        if (totalExpected > 0) {
            double percentage = (totalPaid / totalExpected) * 100;
            percentTxt.setText(String.format(Locale.getDefault(), "%.1f%% of total", percentage));
        } else {
            percentTxt.setText("0% of total");
        }

        Log.d(TAG, "Expected: " + totalExpected + ", Paid: " + totalPaid);
    }
}
