package com.example.predictibill.ui.subscriptions;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class SubscriptionDetailsOverdueFragment extends Fragment {

    private Dialog dialog;
    private Button mark_as_paid_cancel_btn, confirm_btn,
            cancel_subs_btn, keep_tracking_btn,
            delete_permanently_btn, delete_subs_cancel_btn;
    private FirebaseFirestore db;
    private String subscriptionId;

    public SubscriptionDetailsOverdueFragment() {
        super(R.layout.fragment_subscription_details_overdue);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription_details_overdue, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get subscription data from arguments
        Bundle args = getArguments();
        if (args != null) {
            subscriptionId = args.getString("subscriptionId");
            populateViews(view, args);
        }

        ImageButton backButton = view.findViewById(R.id.sub_detail_overdue_backBtn);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        Button markAsPaid = view.findViewById(R.id.overduesubs_details_mark_as_paid_btn);
        markAsPaid.setOnClickListener(v -> showMarkAsPaidDialog());

        Button cancelSubs = view.findViewById(R.id.overduesubs_details_cancel_subs_btn);
        cancelSubs.setOnClickListener(v -> showCancelSubsDialog());

        Button deleteSubs = view.findViewById(R.id.overduesubs_details_delete_subs_btn);
        deleteSubs.setOnClickListener(v -> showDeleteSubsDialog());

        Button editInfoBtn = view.findViewById(R.id.overduesubs_details_edit_info_btn);
        editInfoBtn.setOnClickListener(v -> {
            if (args != null) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_subscriptionDetailsOverdue_to_editSubscriptionFragment, args);
            }
        });
    }

    private void populateViews(View view, Bundle args) {
        // TextViews
        TextView nameTv = view.findViewById(R.id.overduesubs_details_name);
        TextView subsIdTv = view.findViewById(R.id.overduesubs_details_subsID);
        TextView noteTv = view.findViewById(R.id.overduesubs_details_note);
        TextView startDateTv = view.findViewById(R.id.overduesubs_details_startdate);
        TextView nextBillDateTv = view.findViewById(R.id.overduesubs_details_nextbilldate);
        TextView billingCycleTv = view.findViewById(R.id.overduesubs_details_billingcycle);
        TextView priceTv = view.findViewById(R.id.overduesubs_details_price);
        TextView paymentMethodTv = view.findViewById(R.id.overduesubs_details_paymentmethod);
        TextView lastPaidDueTv = view.findViewById(R.id.overduesubs_details_lastpaiddue);

        // Set values from arguments
        nameTv.setText(args.getString("name", ""));
        subsIdTv.setText(args.getString("subscriptionId", ""));
        noteTv.setText(args.getString("note", ""));
        startDateTv.setText(args.getString("startDate", ""));
        nextBillDateTv.setText(args.getString("nextBillingDate", ""));
        billingCycleTv.setText(args.getString("billingCycle", ""));
        priceTv.setText(String.format("₱%.2f", args.getDouble("price", 0.0)));
        paymentMethodTv.setText(args.getString("paymentMethod", ""));
        lastPaidDueTv.setText(args.getString("lastPaidDue", ""));

        // Set category image
        ImageView categoryIv = view.findViewById(R.id.overdue_category_name);
        String category = args.getString("category", "");
        int categoryRes = getCategoryImageRes(category);
        if (categoryRes != 0) {
            categoryIv.setImageResource(categoryRes);
        }

        // Set status image
        ImageView statusIv = view.findViewById(R.id.overdue_status);
        String status = args.getString("status", "");
        int statusRes = getStatusImageRes(status);
        if (statusRes != 0) {
            statusIv.setImageResource(statusRes);
        }
    }

    private int getCategoryImageRes(String category) {
        if (category == null) return 0;
        String formatted = category.toLowerCase()
                .replace("&", "and")
                .replaceAll("[^a-z0-9]", "");
        String resourceName = "category_name_" + formatted;
        return getResources().getIdentifier(resourceName, "drawable", requireContext().getPackageName());
    }

    private int getStatusImageRes(String status) {
        if (status == null) return 0;
        String formatted = "status_" + status.toLowerCase().trim();
        return getResources().getIdentifier(formatted, "drawable", requireContext().getPackageName());
    }

    private void showMarkAsPaidDialog() {
        if (getContext() == null) return;

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.mark_as_paid_dialog_box);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Drawable dialog_box_bg = ContextCompat.getDrawable(getContext(), R.drawable.dialog_box_bg);
            dialog.getWindow().setBackgroundDrawable(dialog_box_bg);
        }

        dialog.setCancelable(false);

        mark_as_paid_cancel_btn = dialog.findViewById(R.id.mark_as_paid_cancel_btn);
        confirm_btn = dialog.findViewById(R.id.confirm_btn);

        mark_as_paid_cancel_btn.setOnClickListener(v -> dialog.dismiss());

        confirm_btn.setOnClickListener(v -> {
            dialog.dismiss();
            updateSubscriptionStatus("Paid", R.id.action_subscriptionDetailsOverdue_to_subscriptionDetailsPaid);
        });

        dialog.show();
    }

    private void showCancelSubsDialog() {
        if (getContext() == null) return;

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.cancel_subscription_dialog_box);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Drawable dialog_box_bg = ContextCompat.getDrawable(getContext(), R.drawable.dialog_box_bg);
            dialog.getWindow().setBackgroundDrawable(dialog_box_bg);
        }

        dialog.setCancelable(false);

        cancel_subs_btn = dialog.findViewById(R.id.cancel_subs_btn);
        keep_tracking_btn = dialog.findViewById(R.id.keep_tracking_btn);

        cancel_subs_btn.setOnClickListener(v -> {
            dialog.dismiss();
            updateSubscriptionStatus("Cancelled", R.id.action_subscriptionDetailsUpcoming_to_subscriptionsFragment);
        });

        keep_tracking_btn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showDeleteSubsDialog() {
        if (getContext() == null) return;

        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.delete_subscription_dialog_box);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            Drawable dialog_box_bg = ContextCompat.getDrawable(getContext(), R.drawable.dialog_box_bg);
            dialog.getWindow().setBackgroundDrawable(dialog_box_bg);
        }

        dialog.setCancelable(false);

        delete_permanently_btn = dialog.findViewById(R.id.delete_permanently_btn);
        delete_subs_cancel_btn = dialog.findViewById(R.id.delete_subs_cancel_btn);

        delete_permanently_btn.setOnClickListener(v -> {
            dialog.dismiss();
            deleteSubscription();
        });

        delete_subs_cancel_btn.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateSubscriptionStatus(String newStatus, int navigationAction) {
        if (subscriptionId == null) return;

        db.collection("subscriptions")
                .document(subscriptionId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    // Update the arguments with new status
                    Bundle args = new Bundle();
                    if (getArguments() != null) {
                        args.putAll(getArguments());
                    }
                    args.putString("status", newStatus);

                    // Navigate to the new fragment
                    NavHostFragment.findNavController(this)
                            .navigate(navigationAction, args);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to update subscription", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteSubscription() {
        if (subscriptionId == null) return;

        db.collection("subscriptions")
                .document(subscriptionId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Subscription deleted", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_subscriptionDetailsOverdue_to_subscriptionsFragment);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete subscription", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}