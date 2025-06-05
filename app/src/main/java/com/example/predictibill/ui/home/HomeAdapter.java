package com.example.predictibill.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.BillViewHolder> {

    private List<Subscription> billList;
    private Context context;
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public HomeAdapter(List<Subscription> billList) {
        this.billList = billList;
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.subscriptions_bill_card, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Subscription current = billList.get(position);

        // Set basic information
        holder.billName.setText(current.getName());
        holder.billingCycle.setText(current.getBillingCycle());
        holder.billPrice.setText(String.format("$%.2f", current.getPrice()));

        // Format and set due date
        String formattedDueDate = formatDateString(current.getNextBillingDate());
        holder.billDueDate.setText(formattedDueDate);

        // Set category image
        String category = current.getCategory();
        if (category != null) {
            int categoryRes = getCategoryImageRes(category);
            if (categoryRes != 0) {
                holder.categoryName.setImageResource(categoryRes);
            }
        }
    }

    @Override
    public int getItemCount() {
        return billList.size();
    }

    public void updateList(List<Subscription> newList) {
        this.billList = newList;
        notifyDataSetChanged();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        TextView billName;
        TextView billingCycle;
        TextView billDueDate;
        TextView billPrice;
        ImageView categoryName;

        public BillViewHolder(@NonNull View itemView) {
            super(itemView);
            billName = itemView.findViewById(R.id.bill_name);
            billingCycle = itemView.findViewById(R.id.billing_cycle);
            billDueDate = itemView.findViewById(R.id.bill_duedate);
            billPrice = itemView.findViewById(R.id.bill_price);
            categoryName = itemView.findViewById(R.id.category_name);
        }
    }

    private int getCategoryImageRes(String category) {
        if (category == null) return 0;

        String formatted = category.toLowerCase()
                .replace("&", "and")
                .replaceAll("[^a-z0-9]", "");

        String resourceName = "category_" + formatted;
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
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
        return dateStr; // fallback if parsing fails
    }
}