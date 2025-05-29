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

public class SubscriptionDetailsCancelledFragment extends Fragment {

    private Dialog dialog;
    private Button restore_subs_btn, delete_permanently_btn, delete_subs_cancel_btn;
    private FirebaseFirestore db;
    private String subscriptionId;

    public SubscriptionDetailsCancelledFragment() {
        super(R.layout.fragment_subscription_details_cancelled);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription_details_cancelled, container, false);
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

        ImageButton backButton = view.findViewById(R.id.sub_detail_cancelled_backBtn);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        Button restoreSubs = view.findViewById(R.id.cancelledsubs_details_restore_subs_btn);
        restoreSubs.setOnClickListener(v -> restoreSubscription());

        Button deleteSubs = view.findViewById(R.id.cancelledsubs_details_delete_subs_btn);
        deleteSubs.setOnClickListener(v -> showDeleteSubsDialog());

        Button editInfoBtn = view.findViewById(R.id.cancelledsubs_details_edit_info_btn);
        editInfoBtn.setOnClickListener(v -> {
            if (args != null) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_subscriptionDetailsCancelled_to_editSubscriptionFragment, args);
            }
        });
    }

    private void populateViews(View view, Bundle args) {
        // TextViews
        TextView nameTv = view.findViewById(R.id.cancelledsubs_details_name);
        TextView subsIdTv = view.findViewById(R.id.cancelledsubs_details_subsID);
        TextView noteTv = view.findViewById(R.id.cancelledsubs_details_note);
        TextView startDateTv = view.findViewById(R.id.cancelledsubs_details_startdate);
        TextView nextBillDateTv = view.findViewById(R.id.cancelledsubs_details_nextbilldate);
        TextView billingCycleTv = view.findViewById(R.id.cancelledsubs_details_billingcycle);
        TextView priceTv = view.findViewById(R.id.cancelledsubs_details_price);
        TextView paymentMethodTv = view.findViewById(R.id.cancelledsubs_details_paymentmethod);
        TextView lastPaidDueTv = view.findViewById(R.id.cancelledsubs_details_lastpaiddue);

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
        ImageView categoryIv = view.findViewById(R.id.cancelled_category_name);
        String category = args.getString("category", "");
        int categoryRes = getCategoryImageRes(category);
        if (categoryRes != 0) {
            categoryIv.setImageResource(categoryRes);
        }

        // Set status image
        ImageView statusIv = view.findViewById(R.id.cancelled_status);
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

    private void restoreSubscription() {
        if (subscriptionId == null) return;

        db.collection("subscriptions")
                .document(subscriptionId)
                .update("status", "Upcoming")
                .addOnSuccessListener(aVoid -> {
                    // Update the arguments with new status
                    Bundle args = new Bundle();
                    if (getArguments() != null) {
                        args.putAll(getArguments());
                    }
                    args.putString("status", "Upcoming");

                    // Navigate to upcoming fragment
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_subscriptionDetailsCancelled_to_subscriptionDetailsUpcoming, args);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to restore subscription", Toast.LENGTH_SHORT).show();
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
                            .navigate(R.id.action_subscriptionDetailsCancelled_to_subscriptionsFragment);
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