package com.example.predictibill.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AnalyticsFragment extends Fragment {

    private PieChart pieChart;
    private LineChart lineChart;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private View view;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public AnalyticsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_analytics, container, false);

        initializeViews();
        initializeFirebase();
        setupCharts();

        if (auth.getCurrentUser() != null) {
            fetchUserSubscriptions();
        }

        return view;
    }

    private void initializeViews() {
        pieChart = view.findViewById(R.id.pieChart);
        lineChart = view.findViewById(R.id.lineChart);
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupCharts() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(65f);
        pieChart.setTransparentCircleRadius(70f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawCenterText(false);
        pieChart.setTouchEnabled(false);
        pieChart.getLegend().setEnabled(false);

        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawLabels(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(Color.DKGRAY);
        legend.setTextSize(12f);
        legend.setFormSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.DKGRAY);
    }

    private void fetchUserSubscriptions() {
        String userId = auth.getCurrentUser().getUid();
        Log.d("AnalyticsDebug", "Fetching subscriptions for userId: " + userId);

        db.collection("subscriptions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        processSubscriptionData(task.getResult());
                    } else {
                        Log.e("AnalyticsDebug", "Failed to fetch subscriptions.");
                    }
                });
    }

    private void processSubscriptionData(QuerySnapshot querySnapshot) {
        Map<String, Float> categoryAmountMap = new HashMap<>();
        List<Subscription> subscriptions = new ArrayList<>();
        float totalSpending = 0;

        Log.d("AnalyticsDebug", "Document count: " + querySnapshot.size());

        for (QueryDocumentSnapshot document : querySnapshot) {
            Subscription subscription = document.toObject(Subscription.class);

            Log.d("AnalyticsDebug", "Subscription: " + subscription.getName() +
                    ", Price: " + subscription.getPrice() +
                    ", Category: " + subscription.getCategory());

            String category = subscription.getCategory() != null ? subscription.getCategory() : "Unknown";
            double price = subscription.getPrice();

            float currentAmount = categoryAmountMap.containsKey(category) ?
                    categoryAmountMap.get(category) : 0f;
            categoryAmountMap.put(category, currentAmount + (float) price);
            totalSpending += price;

            subscriptions.add(subscription);
        }

        Log.d("AnalyticsDebug", "Total spending: " + totalSpending);

        // Remove this check temporarily for testing (you can add it back later)
        // if (totalSpending > 0)
        updatePieChart(categoryAmountMap);
        updateLineChart(subscriptions);
        updateSmartInsights(subscriptions, categoryAmountMap);
    }

    private void updatePieChart(Map<String, Float> categoryAmountMap) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        for (Map.Entry<String, Float> entry : categoryAmountMap.entrySet()) {
            pieEntries.add(new PieEntry(entry.getValue(), entry.getKey()));
            colors.add(getColorForCategory(entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(pieEntries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.animateY(1000);
        pieChart.invalidate();
    }

    private void updateLineChart(List<Subscription> subscriptions) {
        ArrayList<Entry> entries = calculateMonthlySpending(subscriptions);

        LineDataSet dataSet = new LineDataSet(entries, "Monthly Spend");
        dataSet.setColor(Color.parseColor("#90CAF9"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        final String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < months.length) {
                    return months[index];
                }
                return "";
            }
        });

        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    private ArrayList<Entry> calculateMonthlySpending(List<Subscription> subscriptions) {
        float[] monthlySpending = new float[12];
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        for (Subscription subscription : subscriptions) {
            try {
                Date startDate = dateFormat.parse(subscription.getStartDate());
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startDate);

                String billingCycle = subscription.getBillingCycle();
                float price = (float) subscription.getPrice();
                float monthlyCost = calculateMonthlyCost(billingCycle, price);

                for (int i = 0; i < 12; i++) {
                    Calendar monthCal = (Calendar) startCal.clone();
                    monthCal.add(Calendar.MONTH, i);

                    if (monthCal.get(Calendar.YEAR) == currentYear) {
                        int monthIndex = monthCal.get(Calendar.MONTH);
                        monthlySpending[monthIndex] += monthlyCost;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            entries.add(new Entry(i, monthlySpending[i]));
        }

        return entries;
    }

    private float calculateMonthlyCost(String billingCycle, float price) {
        if (billingCycle == null) return price;

        switch (billingCycle) {
            case "Monthly": return price;
            case "Quarterly": return price / 3;
            case "Yearly": return price / 12;
            case "Weekly": return price * 4;
            default: return price;
        }
    }

    private void updateSmartInsights(List<Subscription> subscriptions, Map<String, Float> categoryAmountMap) {
        TextView insightText = view.findViewById(R.id.insight_text);
        List<String> insightsPool = new ArrayList<>();

        float total = 0f;
        float minPrice = Float.MAX_VALUE;
        float average = 0f;
        Subscription mostExpensive = null;
        Subscription cheapest = null;
        String topCategory = null;
        float maxCategorySpend = 0f;
        int overdueCount = 0;
        float overdueTotal = 0f;
        int upcomingCount = 0;
        int paidCount = 0;
        Date earliestUpcoming = null;
        Date latestUpcoming = null;
        Map<String, Integer> paymentMethods = new HashMap<>();

        for (Subscription sub : subscriptions) {
            float price = (float) sub.getPrice();
            total += price;

            if (mostExpensive == null || price > mostExpensive.getPrice()) {
                mostExpensive = sub;
            }

            if (price < minPrice) {
                minPrice = price;
                cheapest = sub;
            }

            if ("Overdue".equals(sub.getStatus())) {
                overdueCount++;
                overdueTotal += price;
            } else if ("Upcoming".equals(sub.getStatus())) {
                upcomingCount++;
                try {
                    Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).parse(sub.getDueDate());
                    if (date != null) {
                        if (earliestUpcoming == null || date.before(earliestUpcoming)) {
                            earliestUpcoming = date;
                        }
                        if (latestUpcoming == null || date.after(latestUpcoming)) {
                            latestUpcoming = date;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("Paid".equals(sub.getStatus())) {
                paidCount++;
            }

            String method = sub.getPaymentMethod();
            if (method != null && !method.isEmpty()) {
                paymentMethods.put(method, paymentMethods.getOrDefault(method, 0) + 1);
            }
        }

        average = subscriptions.size() > 0 ? total / subscriptions.size() : 0;

        for (Map.Entry<String, Float> entry : categoryAmountMap.entrySet()) {
            if (entry.getValue() > maxCategorySpend) {
                maxCategorySpend = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        // Build the pool of smart insights
        insightsPool.add("You spend a total of ₱" + String.format("%.2f", total) + " monthly.");
        insightsPool.add("On average, each subscription costs you ₱" + String.format("%.2f", average) + ".");
        if (mostExpensive != null) {
            insightsPool.add("Your most expensive subscription is " + mostExpensive.getName() +
                    " (₱" + String.format("%.2f", mostExpensive.getPrice()) + ").");
        }
        if (cheapest != null) {
            insightsPool.add("Your cheapest subscription is " + cheapest.getName() +
                    " (₱" + String.format("%.2f", cheapest.getPrice()) + ").");
        }
        if (topCategory != null) {
            insightsPool.add("You spend the most on " + topCategory + " (₱" +
                    String.format("%.2f", maxCategorySpend) + ").");
        }
        for (Map.Entry<String, Float> entry : categoryAmountMap.entrySet()) {
            float percentage = (entry.getValue() / total) * 100;
            insightsPool.add(entry.getKey() + " accounts for " + String.format("%.0f", percentage) + "% of your spending.");
        }
        if (overdueCount > 0) {
            insightsPool.add("You have " + overdueCount + " overdue subscription" +
                    (overdueCount > 1 ? "s" : "") + " totaling ₱" + String.format("%.2f", overdueTotal) + ".");
        }
        if (paidCount > 0 || upcomingCount > 0) {
            insightsPool.add("Status summary: " + paidCount + " Paid, " + upcomingCount + " Upcoming, " + overdueCount + " Overdue.");
        }
        if (!paymentMethods.isEmpty()) {
            StringBuilder methodsUsed = new StringBuilder();
            for (Map.Entry<String, Integer> entry : paymentMethods.entrySet()) {
                methodsUsed.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
            }
            methodsUsed.setLength(methodsUsed.length() - 2); // Remove trailing comma
            insightsPool.add("Payment methods used: " + methodsUsed.toString());
        }
        if (earliestUpcoming != null && latestUpcoming != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
            insightsPool.add("Your earliest upcoming bill is due on " + sdf.format(earliestUpcoming) + ".");
            insightsPool.add("Your latest upcoming bill is on " + sdf.format(latestUpcoming) + ".");
        }

        // Shuffle and pick up to 3 random insights
        Collections.shuffle(insightsPool);
        int maxToShow = Math.min(3, insightsPool.size());
        StringBuilder finalInsights = new StringBuilder();
        for (int i = 0; i < maxToShow; i++) {
            finalInsights.append(insightsPool.get(i)).append("\n");
        }

        insightText.setText(finalInsights.toString().trim());
    }

    private void clearCharts() {
        pieChart.clear();
        lineChart.clear();
        TextView insightText = view.findViewById(R.id.insight_text);
        insightText.setText("No subscription data available");
    }

    private int getColorForCategory(String category) {
        if (category == null) return Color.LTGRAY;

        switch (category.toLowerCase()) {
            case "entertainment": return Color.parseColor("#FF7F7F");
            default: return Color.LTGRAY;
        }
    }
}
