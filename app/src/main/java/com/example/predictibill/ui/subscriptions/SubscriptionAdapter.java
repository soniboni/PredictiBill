package com.example.predictibill.ui.subscriptions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.predictibill.R;
import java.util.List;

//RecyclerView Adapter to bind a list of subscriptions to the UI.
public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private List<SubscriptionsFragment.Subscription> subscriptionList;

    /**
     * Constructor that receives the list of subscriptions to display.
     *
     * @param subscriptionList List of subscriptions to bind to the RecyclerView.
     */
    public SubscriptionAdapter(List<SubscriptionsFragment.Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }

    /**
     * Holds references to each item's views
     */
    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView billingCycleTextView;
        public TextView categoryTextView;
        public TextView statusTextView;
        public TextView priceTextView;
        public TextView dueDateTextView;

        /**
         * Initializes view references for a single item.
         */
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

    /**
     * If recycleView needs a new holder, it calls this then inflates the layout for each items
     */
    @NonNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual subscription list item
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscription_item, parent, false);
        return new SubscriptionViewHolder(view);
    }

    /**
     * Binds the data from the subscription list to each ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        // Get the subscription at the current position
        SubscriptionsFragment.Subscription current = subscriptionList.get(position);

        // Set the text views with subscription details
        holder.nameTextView.setText(current.getName());
        holder.billingCycleTextView.setText(current.getBillingCycle());
        holder.categoryTextView.setText(current.getCategory());
        holder.statusTextView.setText(current.getStatus());
        holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));
        holder.dueDateTextView.setText(String.format("Due: %s", current.getDueDate()));
    }

    /**
     * Returns the total number of subscriptions in the list.
     */
    @Override
    public int getItemCount() {
        return subscriptionList == null ? 0 : subscriptionList.size();
    }
}
