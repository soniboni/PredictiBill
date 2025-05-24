package com.example.predictibill.ui.subscriptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;

import java.util.List;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private List<SubscriptionsFragment.Subscription> subscriptionList;

    public SubscriptionAdapter(List<SubscriptionsFragment.Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView billingCycleTextView;
        public TextView categoryTextView;
        public TextView statusTextView;
        public TextView priceTextView;
        public TextView dueDateTextView;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subscription_name);
            billingCycleTextView = itemView.findViewById(R.id.subscription_billing_cycle);
            categoryTextView = itemView.findViewById(R.id.subscription_category);
            statusTextView = itemView.findViewById(R.id.subscription_status);
            priceTextView = itemView.findViewById(R.id.subscription_price_value);
            dueDateTextView = itemView.findViewById(R.id.subscription_due_date);
        }
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscription_item, parent, false);
        return new SubscriptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        SubscriptionsFragment.Subscription current = subscriptionList.get(position);

        holder.nameTextView.setText(current.getName());
        holder.billingCycleTextView.setText(current.getBillingCycle());
        holder.categoryTextView.setText(current.getCategory());
        holder.statusTextView.setText(current.getStatus());
        holder.priceTextView.setText("₱" + current.getPrice());
        holder.dueDateTextView.setText("Due date on "+current.getDueDate());
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }
}
