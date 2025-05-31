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

    private static final String TAG = "HomeFragment";

    private FirebaseFirestore db;

    private TextView expectedMoneyTxt, spentMoneyTxt, percentTxt;
    private TextView[] rectangleViews;

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

        expectedMoneyTxt = view.findViewById(R.id.home_expectedmoney_txt);
        spentMoneyTxt = view.findViewById(R.id.home_spentmoney_txt);
        percentTxt = view.findViewById(R.id.home_percent_txt);

        rectangleViews = new TextView[]{
                view.findViewById(R.id.home_rectangle_1),
                view.findViewById(R.id.home_rectangle_2),
                view.findViewById(R.id.home_rectangle_3),
                view.findViewById(R.id.home_rectangle_4),
                view.findViewById(R.id.home_rectangle_5),
                view.findViewById(R.id.home_rectangle_6),
                view.findViewById(R.id.home_rectangle_7),
                view.findViewById(R.id.home_rectangle_8),
        };

        fetchAndDisplaySubscriptions();

        return view;
    }

    private void fetchAndDisplaySubscriptions() {
        db.collection("subscriptions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int i = 0;
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String name = doc.getString("name");
                            Double expected = doc.getDouble("expected");
                            Double paid = doc.getDouble("paid");

                            if (expected == null) expected = 0.0;
                            if (paid == null) paid = 0.0;

                            totalExpected += expected;
                            totalPaid += paid;

                            if (i < rectangleViews.length) {
                                rectangleViews[i].setText(name);
                            }
                            i++;
                        }
                        updateUI();
                    } else {
                        Log.e(TAG, "Error getting subscriptions: ", task.getException());
                    }
                });
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
