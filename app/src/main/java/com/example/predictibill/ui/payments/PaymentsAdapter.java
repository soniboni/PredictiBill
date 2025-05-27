package com.example.predictibill.ui.payments;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;

import java.util.List;

//Handles displaying subscription data in a list format for payments
public class PaymentsAdapter extends RecyclerView.Adapter<PaymentsAdapter.PaymentsViewHolder> {

    // List of subscriptions to be displayed
    private List<Subscription> subscriptionList;


    //subscriptionList List of Subscription objects to be displayed

    public PaymentsAdapter(List<Subscription> subscriptionList) {
        this.subscriptionList = subscriptionList;
    }
    //ViewHolder for each reference in the recycleview
    public static class PaymentsViewHolder extends RecyclerView.ViewHolder {
        // UI elements for each subscription item
        public TextView nameTextView;              // Displays subscription name
        public TextView billingCycleTextView;      // Displays billing cycle
        public TextView categoryTextView;          // Displays subscription category
        public TextView subscriptionIdTextView;    // Displays subscription ID
        public TextView priceTextView;             // Displays subscription price
        public TextView dueDateTextView;          // Displays payment due date


        public PaymentsViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize all TextView references
            nameTextView = itemView.findViewById(R.id.subscription_name);
            billingCycleTextView = itemView.findViewById(R.id.subscription_billing_cycle);
            categoryTextView = itemView.findViewById(R.id.subscription_category);
            subscriptionIdTextView = itemView.findViewById(R.id.subscription_id);
            priceTextView = itemView.findViewById(R.id.subscription_price_value);
            dueDateTextView = itemView.findViewById(R.id.subscription_due_date);
        }
    }

    // for new viewholders for each newly added sub
    @NonNull
    @Override
    public PaymentsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create a new ViewHolder
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payments_item, parent, false);
        return new PaymentsViewHolder(view);
    }

    //calls the specific data from the List
    @Override
    public void onBindViewHolder(@NonNull PaymentsViewHolder holder, int position) {
        // Get the subscription at the current position
        Subscription current = subscriptionList.get(position);

        // Set background for the item, it doesnt work if sa xml nung payments_item kasi ang cinacall nito as of now is from subscription so nakokopya niya yung
        //background kaya siya naka lagay dito:p
        holder.itemView.setBackgroundResource(R.drawable.payments_item_container);

        holder.nameTextView.setText(current.getName());
        holder.billingCycleTextView.setText(current.getBillingCycle());
        holder.categoryTextView.setText(current.getCategory());
        holder.subscriptionIdTextView.setText(current.getSubscriptionId());
        holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));
        holder.dueDateTextView.setText(String.format("Paid on: %s", current.getDueDate()));
    }


     //Returns the total number of items in the data set held by the adapter
    //@return The total number of items in the list
    @Override
    public int getItemCount() {
        // Handle null case and return 0 if list is null
        int count = subscriptionList == null ? 0 : subscriptionList.size();
        Log.d("PaymentsAdapter", "Item count: " + count);  // Debug log for item count
        return count;
    }
}