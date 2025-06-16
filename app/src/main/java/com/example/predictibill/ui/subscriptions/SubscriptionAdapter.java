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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> {

    private List<Subscription> subscriptionList;
    private final OnSubscriptionClickListener listener;
    private Context context;

    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

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

        if (holder.nameTextView != null && current.getName() != null) {
            holder.nameTextView.setText(current.getName());
        }
        if (holder.billingCycleTextView != null && current.getBillingCycle() != null) {
            holder.billingCycleTextView.setText(current.getBillingCycle());
        }
        if (holder.priceTextView != null) {
            holder.priceTextView.setText(String.format("₱%.2f", current.getPrice()));
        }

        if (holder.dueDateTextView != null) {
            String formattedDueDate = formatDateString(current.getNextBillingDate());
            holder.dueDateTextView.setText(String.format("Due: %s", formattedDueDate));
        }

        if (holder.categoryImageView != null && holder.categoryTextView != null) {
            String category = current.getCategory();
            if (category != null) {
                Integer categoryRes = categoryImageMap.get(category);
                if (categoryRes != null) {
                    holder.categoryImageView.setVisibility(View.VISIBLE);
                    holder.categoryImageView.setImageResource(categoryRes);
                    holder.categoryTextView.setVisibility(View.GONE);
                } else {
                    holder.categoryImageView.setVisibility(View.GONE);
                    holder.categoryTextView.setVisibility(View.VISIBLE);
                    holder.categoryTextView.setText(category);
                }
            } else {
                holder.categoryImageView.setVisibility(View.GONE);
                holder.categoryTextView.setVisibility(View.GONE);
            }
        }

        if (holder.statusImageView != null) {
            String status = current.getStatus();
            if (status != null) {
                int statusRes = getStatusImageRes(status);
                if (statusRes != 0) {
                    holder.statusImageView.setVisibility(View.VISIBLE);
                    holder.statusImageView.setImageResource(statusRes);
                } else {
                    holder.statusImageView.setVisibility(View.GONE);
                }
            } else {
                holder.statusImageView.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                listener.onSubscriptionClick(current);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subscriptionList.size();
    }

    static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView billingCycleTextView;
        ImageView categoryImageView;
        TextView categoryTextView;
        ImageView statusImageView;
        TextView priceTextView;
        TextView dueDateTextView;

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

    // Fixed category names to exactly match filtering keys
    private static final Map<String, Integer> categoryImageMap = new HashMap<>();
    static {
        categoryImageMap.put("Cloud Storage", R.drawable.category_name_cloudstorage);
        categoryImageMap.put("Entertainment", R.drawable.category_name_entertainment);
        categoryImageMap.put("Food & Delivery", R.drawable.category_name_foodanddelivery); // Changed key to "Food Delivery"
        categoryImageMap.put("Membership", R.drawable.category_name_membership);
        categoryImageMap.put("Productivity & Tools", R.drawable.category_name_productivityandtools); // Changed key to "Productivity & Tools"
    }

    private int getStatusImageRes(String status) {
        if (status == null) return 0;
        String formatted = "status_" + status.toLowerCase().trim();
        return context.getResources().getIdentifier(formatted, "drawable", context.getPackageName());
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
        return dateStr;
    }
}
