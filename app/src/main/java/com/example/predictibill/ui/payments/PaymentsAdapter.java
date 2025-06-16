package com.example.predictibill.ui.payments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentsViewHolder> {

    private List<Subscription> subscriptionList;
    private final OnPaymentClickListener listener;

    public interface OnPaymentClickListener {
        void onPaymentClick(Subscription subscription);
    }

    public PaymentsAdapter(List<Subscription> subscriptionList, OnPaymentClickListener listener) {
        this.subscriptionList = subscriptionList;
        this.listener = listener;
    }

    public void updateList(List<Subscription> newList) {
        this.subscriptionList = newList;
        notifyDataSetChanged();
    }

    public static class PaymentsViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView billingCycleTextView;
        public ImageView categoryImageView;
        public TextView priceTextView;
        public TextView dueDateTextView;
        public TextView paymentIdTextView; // 👈 Added this

        public PaymentsViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subscription_name);
            billingCycleTextView = itemView.findViewById(R.id.subscription_billing_cycle);
            categoryImageView = itemView.findViewById(R.id.subscription_category_image);
            priceTextView = itemView.findViewById(R.id.subscription_price_value);
            dueDateTextView = itemView.findViewById(R.id.subscription_due_date);
            paymentIdTextView = itemView.findViewById(R.id.subscription_id); // 👈 Add this in your layout
        }
    }

    @NonNull
    @Override
    public PaymentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payments_item, parent, false);
        return new PaymentsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentsViewHolder holder, int position) {
        Subscription current = subscriptionList.get(position);

        holder.itemView.setBackgroundResource(R.drawable.payments_item_container);

        holder.nameTextView.setText(current.getName());
        holder.billingCycleTextView.setText(current.getBillingCycle());
        holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));

        // ✅ Set actual subscription ID
        holder.paymentIdTextView.setText("#" + current.getId());

        // Format the payment date
        if (current.getLastPaidDue() != null && !current.getLastPaidDue().isEmpty()) {
            holder.dueDateTextView.setText("Paid on: " + formatPaymentDate(current.getLastPaidDue()));
        } else {
            holder.dueDateTextView.setText("Paid on: N/A");
        }

        // Set category image based on category name
        int categoryImageRes = getCategoryImageResource(current.getCategory());
        if (categoryImageRes != 0) {
            holder.categoryImageView.setImageResource(categoryImageRes);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onPaymentClick(current);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionList == null ? 0 : subscriptionList.size();
    }

    private String formatPaymentDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    private int getCategoryImageResource(String category) {
        if (category == null) return 0;

        switch (category.toLowerCase()) {
            case "entertainment":
                return R.drawable.category_name_entertainment;
            case "productivity & tools":
            case "productivity and tools":
                return R.drawable.category_name_productivityandtools;
            case "cloud storage":
                return R.drawable.category_name_cloudstorage;
            case "membership":
                return R.drawable.category_name_membership;
            case "food & delivery":
            case "food and delivery":
                return R.drawable.category_name_foodanddelivery;
            default:
                return 0;
        }
    }
}
