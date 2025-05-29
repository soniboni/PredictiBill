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

    // Rectangle TextViews
    private TextView[] rectangleViews;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseFirestore.getInstance();

        expectedMoneyTxt = view.findViewById(R.id.home_expectedmoney_txt);
        spentMoneyTxt = view.findViewById(R.id.home_spentmoney_txt);
        percentTxt = view.findViewById(R.id.home_percent_txt);

        // Initialize rectangle array
        rectangleViews = new TextView[]{
                view.findViewById(R.id.home_rectangle_2),
                view.findViewById(R.id.home_rectangle_3),
                view.findViewById(R.id.home_rectangle_4),
                view.findViewById(R.id.home_rectangle_5),
                view.findViewById(R.id.home_rectangle_6),
                view.findViewById(R.id.home_rectangle_7),
                view.findViewById(R.id.home_rectangle_8),
                view.findViewById(R.id.home_rectangle_9),
                view.findViewById(R.id.home_rectangle_10),
                view.findViewById(R.id.home_rectangle_11)
        };

        fetchBillingData();

        return view;
    }

    private void fetchBillingData() {
        totalExpected = 0.0;
        totalPaid = 0.0;

        db.collection("subscriptions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double price = document.getDouble("price");
                            String status = document.getString("status");

                            if (price != null) {
                                totalExpected += price;
                                if ("Paid".equalsIgnoreCase(status)) {
                                    totalPaid += price;
                                }
                            }
                        }
                        updateUI();
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Firestore query failed: ", e));
    }

    private void updateUI() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
        currencyFormat.setMaximumFractionDigits(2);

        expectedMoneyTxt.setText(currencyFormat.format(totalExpected));
        spentMoneyTxt.setText(currencyFormat.format(totalPaid));

        double percentage = totalExpected > 0 ? (totalPaid / totalExpected) * 100 : 0;
        percentTxt.setText(String.format(Locale.getDefault(), "%.1f%% of total", percentage));

        shadeRectanglesByPercentage(percentage);
    }

    private void shadeRectanglesByPercentage(double percentage) {
        int totalRectangles = rectangleViews.length;
        int shadedCount = (int) Math.round((percentage / 100) * totalRectangles);

        for (int i = 0; i < totalRectangles; i++) {
            if (i < shadedCount) {
                rectangleViews[i].setBackgroundResource(R.drawable.home_rectangle_2); // shaded
            } else {
                rectangleViews[i].setBackgroundResource(R.drawable.home_rectangle_3); // unshaded
            }
        }
    }
}
