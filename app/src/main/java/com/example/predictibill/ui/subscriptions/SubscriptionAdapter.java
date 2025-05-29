package com.example.predictibill.ui.subscriptions;

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

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private List<Subscription> subscriptionList;
    private Context context;
    private OnSubscriptionClickListener listener;

    public interface OnSubscriptionClickListener {
        void onSubscriptionClick(Subscription subscription);
    }

    public SubscriptionAdapter(List<Subscription> subscriptionList, OnSubscriptionClickListener listener) {
        this.subscriptionList = subscriptionList;
        this.listener = listener;
    }

    public void updateList(List<Subscription> newList) {
        this.subscriptionList = newList;
        notifyDataSetChanged();
    }

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView billingCycleTextView;
        public ImageView categoryImageView;
        public TextView categoryTextView;
        public ImageView statusImageView;
        public TextView priceTextView;
        public TextView dueDateTextView;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subscription_name);
            billingCycleTextView = itemView.findViewById(R.id.subscription_billing_cycle);
            categoryImageView = itemView.findViewById(R.id.subscription_category_image);
            categoryTextView = itemView.findViewById(R.id.subscription_category_text);
            statusImageView = itemView.findViewById(R.id.subscription_status);
            priceTextView = itemView.findViewById(R.id.subscription_price_value);
            dueDateTextView = itemView.findViewById(R.id.subscription_due_date);
        }
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.subscription_item, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        Subscription current = subscriptionList.get(position);

        holder.nameTextView.setText(current.getName());
        holder.billingCycleTextView.setText(current.getBillingCycle());
        holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));
        holder.dueDateTextView.setText(String.format("Due: %s", current.getNextBillingDate()));

        // Category image logic
        String category = current.getCategory();
        int categoryRes = getCategoryImageRes(category);

        if (categoryRes != 0) {
            holder.categoryImageView.setVisibility(View.VISIBLE);
            holder.categoryImageView.setImageResource(categoryRes);
            holder.categoryTextView.setVisibility(View.GONE);
        } else {
            holder.categoryImageView.setVisibility(View.GONE);
            holder.categoryTextView.setVisibility(View.VISIBLE);
            holder.categoryTextView.setText(category);
        }

        // Status icon logic
        String status = current.getStatus();
        int statusRes = getStatusImageRes(status);

        if (statusRes != 0) {
            holder.statusImageView.setVisibility(View.VISIBLE);
            holder.statusImageView.setImageResource(statusRes);
        } else {
            holder.statusImageView.setVisibility(View.GONE);
        }

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSubscriptionClick(current);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    private int getCategoryImageRes(String category) {
        if (category == null) return 0;

        String formatted = category.toLowerCase()
                .replace("&", "and")
                .replaceAll("[^a-z0-9]", "");

        String resourceName = "category_name_" + formatted;
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }

    private int getStatusImageRes(String status) {
        if (status == null) return 0;

        String formatted = "status_" + status.toLowerCase().trim();
        return context.getResources().getIdentifier(formatted, "drawable", context.getPackageName());
    }
}