package com.example.predictibill.ui.payments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.predictibill.models.Subscription;

import com.example.predictibill.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentsViewHolder> {

    private List<Subscription> subscriptionList;

    public PaymentsAdapter(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public void updateList(List<Subscription> newList) {
        subscriptionList = newList;
        notifyDataSetChanged();
    }

    public static class PaymentsViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView billingCycleTextView;
        public ImageView categoryImageView;  // Changed from TextView to ImageView
        public TextView subscriptionIdTextView;
        public TextView priceTextView;
        public TextView dueDateTextView;

        public PaymentsViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subscription_name);
            billingCycleTextView = itemView.findViewById(R.id.subscription_billing_cycle);
            categoryImageView = itemView.findViewById(R.id.subscription_category_image);  // Use ImageView id
            subscriptionIdTextView = itemView.findViewById(R.id.subscription_id);
            priceTextView = itemView.findViewById(R.id.subscription_price_value);
            dueDateTextView = itemView.findViewById(R.id.subscription_due_date);
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
        holder.subscriptionIdTextView.setText(current.getSubscriptionId());
        holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));

        if (current.getUpdatedAt() != null) {
            Date updatedAtDate = current.getUpdatedAt().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(updatedAtDate);
            holder.dueDateTextView.setText("Paid on: " + formattedDate);
        } else {
            holder.dueDateTextView.setText("Paid on: N/A");
        }

        // Set category image based on category name
        int categoryImageRes = getCategoryImageResource(current.getCategory());
        holder.categoryImageView.setImageResource(categoryImageRes);
    }

    @Override
    public int getItemCount() {
        return subscriptionList == null ? 0 : subscriptionList.size();
    }

    // Helper method to return image resource id based on category name
    private int getCategoryImageResource(String category) {
        if (category == null) return 0;

        switch (category.toLowerCase()) {
            case "entertainment":
                return R.drawable.category_name_entertainment;
            case "productivity & tools":
                return R.drawable.category_name_productivityandtools;
            case "cloud storage":
                return R.drawable.category_name_cloudstorage;
            case "membership":
                return R.drawable.category_name_membership;
            case "food & delivery":
                return R.drawable.category_name_foodanddelivery;
            default:
                return 0;
        }
    }
}
