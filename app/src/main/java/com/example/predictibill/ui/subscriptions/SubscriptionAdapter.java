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

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView priceTextView;

        public SubscriptionViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subscription_name);
            priceTextView = itemView.findViewById(R.id.subscription_price);
        }
    }

    public SubscriptionAdapter(List<SubscriptionsFragment.Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscription_item, parent, false);
        return new SubscriptionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        SubscriptionsFragment.Subscription current = subscriptionList.get(position);
        holder.nameTextView.setText(current.getName());
        holder.priceTextView.setText("₱" + current.getPrice() + " / " + current.getBillingCycle());
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }
}
