package com.example.predictibill.ui.subscriptions;

import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.R;

public class SubscriptionDetailsUpcomingFragment extends Fragment {

    Dialog dialog;
    ImageButton cancel_btn, confirm_btn;

    public SubscriptionDetailsUpcomingFragment() {
        // Required empty public constructor
        super(R.layout.fragment_subscription_details_upcoming);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subscription_details_upcoming, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.sub_detail_upcoming_backBtn);
        backButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        Button markAsPaid = view.findViewById(R.id.upcomingsubs_details_mark_as_paid_btn);
        markAsPaid.setOnClickListener(v -> showMarkAsPaidDialog());
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

        dialog.setCancelable(true);

        // Initialize buttons
        cancel_btn = dialog.findViewById(R.id.cancel_btn);
        confirm_btn = dialog.findViewById(R.id.confirm_btn);

        if (cancel_btn != null) {
            //Dismiss the dialog box when the cancel button is clicked
            cancel_btn.setOnClickListener(v -> dialog.dismiss());
        }

        if (confirm_btn != null) {
            confirm_btn.setOnClickListener(v -> { dialog.dismiss();
                // Navigate to the paid subscription details
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_subscriptionDetailsUpcoming_to_subscriptionDetailsPaid);
            });
        }

        // Show the dialog
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up dialog to prevent memory leaks
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}