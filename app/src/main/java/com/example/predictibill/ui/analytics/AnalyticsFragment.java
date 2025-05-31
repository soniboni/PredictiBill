package com.example.predictibill.ui.analytics;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.predictibill.R;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private PieChart pieChart;
    private LineChart lineChart;
    private FirebaseFirestore db;

    public AnalyticsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        pieChart = view.findViewById(R.id.pieChart);
        lineChart = view.findViewById(R.id.lineChart);
        db = FirebaseFirestore.getInstance();

        fetchCategoriesAndAmounts();
        setupLineChart();

        return view;
    }

    private void fetchCategoriesAndAmounts() {
        db.collection("subscriptions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Float> categoryAmountMap = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String category = document.getString("category");
                                Double amountDouble = document.getDouble("amount");

                                if (category != null && amountDouble != null) {
                                    float amount = amountDouble.floatValue();
                                    // Sum amounts per category
                                    if (categoryAmountMap.containsKey(category)) {
                                        categoryAmountMap.put(category, categoryAmountMap.get(category) + amount);
                                    } else {
                                        categoryAmountMap.put(category, amount);
                                    }
                                }
                            }

                            ArrayList<PieEntry> entries = new ArrayList<>();
                            ArrayList<Integer> colors = new ArrayList<>();

                            for (Map.Entry<String, Float> entry : categoryAmountMap.entrySet()) {
                                entries.add(new PieEntry(entry.getValue(), entry.getKey()));
                                colors.add(getColorForCategory(entry.getKey()));
                            }

                            if (entries.isEmpty()) {
                                entries = getDefaultEntries();
                                colors = getDefaultColors();
                            }

                            setupPieChart(entries, colors);

                        } else {
                            // Firestore fetch failed, use fallback data
                            setupPieChart(getDefaultEntries(), getDefaultColors());
                        }
                    }
                });
    }

    private void setupPieChart(ArrayList<PieEntry> entries, ArrayList<Integer> colors) {
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setDrawValues(false); // hide values inside pie

        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);

        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(65f);
        pieChart.setTransparentCircleRadius(70f);
        pieChart.setDrawEntryLabels(false);
        pieChart.setDrawCenterText(false);
        pieChart.setTouchEnabled(false);
        pieChart.animateY(1000);

        // Disable legend (we use a custom legend in layout)
        pieChart.getLegend().setEnabled(false);

        pieChart.invalidate();
    }

    private void setupLineChart() {
        ArrayList<Entry> entries = new ArrayList<>();

        // Sample data for last 12 weeks (index 0 to 11)
        float[] sampleValues = {5, 8, 6, 10, 7, 9, 11, 13, 12, 14, 15, 17};
        for (int i = 0; i < sampleValues.length; i++) {
            entries.add(new Entry(i, sampleValues[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Monthly Spend");
        dataSet.setColor(Color.parseColor("#90CAF9")); // Blue line
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(dataSet);

        lineChart.setData(lineData);
        lineChart.getDescription().setEnabled(false);

        // Enable legend and style it
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

        // Setup X-axis with month labels
        final String[] months = new String[] {
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < months.length) {
                    return months[index];
                } else {
                    return "";
                }
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.DKGRAY);

        lineChart.setTouchEnabled(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisLeft().setDrawLabels(false);
        lineChart.animateX(1000);
        lineChart.invalidate();
    }

    // Color mapping by category name
    private int getColorForCategory(String category) {
        switch (category.toLowerCase()) {
            case "entertainment":
                return Color.parseColor("#FF7F7F");  // red
            case "productivity & tools":
                return Color.parseColor("#FFC878");  // orange
            case "cloud storage":
                return Color.parseColor("#FFF176");  // yellow
            case "membership":
                return Color.parseColor("#A5D6A7");  // green
            case "food & delivery":
                return Color.parseColor("#90CAF9");  // blue
            default:
                return Color.LTGRAY; // fallback color
        }
    }

    // Fallback default data if Firestore fetch fails or no data
    private ArrayList<PieEntry> getDefaultEntries() {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(35f, "Entertainment"));
        entries.add(new PieEntry(20f, "Productivity & Tools"));
        entries.add(new PieEntry(15f, "Cloud Storage"));
        entries.add(new PieEntry(10f, "Membership"));
        entries.add(new PieEntry(20f, "Food & Delivery"));
        return entries;
    }

    private ArrayList<Integer> getDefaultColors() {
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF7F7F"));
        colors.add(Color.parseColor("#FFC878"));
        colors.add(Color.parseColor("#FFF176"));
        colors.add(Color.parseColor("#A5D6A7"));
        colors.add(Color.parseColor("#90CAF9"));
        return colors;
    }
}
