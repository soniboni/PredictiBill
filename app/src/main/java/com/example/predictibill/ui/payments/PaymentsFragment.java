package com.example.predictibill.ui.payments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.predictibill.R;
import com.example.predictibill.ui.subscriptions.SubscriptionsFragment;
import java.util.List;

public class PaymentsFragment extends Fragment {

    // List for the "marked as paid" subscriptions.
    // Currently using dummy/static data from SubscriptionsFragment.
    private List<SubscriptionsFragment.Subscription> paidSubscriptions;

    private RecyclerView recyclerView;
    private PaymentsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for the PaymentsFragment (fragment_payments.xml)
        return inflater.inflate(R.layout.fragment_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // TODO for Backend:
        // Replace this dummy data reference with actual filtered paid subscriptions
        // once integration with the database is complete.
        // For now, using the shared static list from SubscriptionsFragment.
        paidSubscriptions = SubscriptionsFragment.subscriptionList;

        // Set up RecyclerView to display the list of paid subscriptions
        recyclerView = view.findViewById(R.id.payments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the adapter with the current subscription data
        adapter = new PaymentsAdapter(paidSubscriptions);
        recyclerView.setAdapter(adapter);
    }
}
