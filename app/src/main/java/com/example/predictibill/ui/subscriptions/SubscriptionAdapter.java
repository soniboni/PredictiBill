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

    public SubscriptionAdapter(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView billingCycleTextView;
        public TextView categoryTextView;
        public ImageView statusImageView;
        public TextView priceTextView;
        public TextView dueDateTextView;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.subscription_name);
            billingCycleTextView = itemView.findViewById(R.id.subscription_billing_cycle);
            categoryTextView = itemView.findViewById(R.id.subscription_category);
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
        holder.categoryTextView.setText(current.getCategory());
        holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));
        holder.dueDateTextView.setText(String.format("Due: %s", current.getDueDate()));

        // Set status image based on status
        switch (current.getStatus().toLowerCase()) {
            case "upcoming":
                holder.statusImageView.setImageResource(R.drawable.status_upcoming);
                break;
            case "overdue":
                holder.statusImageView.setImageResource(R.drawable.status_overdue);
                break;
            case "paid":
                holder.statusImageView.setImageResource(R.drawable.status_paid);
                break;
            case "cancelled":
                holder.statusImageView.setImageResource(R.drawable.status_cancelled);
                break;
            default:
                holder.statusImageView.setImageResource(R.drawable.status_upcoming);
        }
    }

    @Override
    public int getItemCount() {
        return subscriptionList == null ? 0 : subscriptionList.size();
    }

    public void updateList(List<Subscription> newList) {
        subscriptionList = newList;
        notifyDataSetChanged();
    }
}
