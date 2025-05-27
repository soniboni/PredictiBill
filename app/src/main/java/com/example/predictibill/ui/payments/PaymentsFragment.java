package com.example.predictibill.ui.payments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.predictibill.R;
import com.example.predictibill.models.Subscription;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PaymentsFragment extends Fragment {

    private List<Subscription> paidSubscriptions = new ArrayList<>();
    private RecyclerView recyclerView;
    private PaymentsAdapter adapter;

    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.payments_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PaymentsAdapter(paidSubscriptions);
        recyclerView.setAdapter(adapter);

        fetchPaidSubscriptionsFromFirestore();
    }

    private void fetchPaidSubscriptionsFromFirestore() {
        // Assuming you have a collection called "subscriptions"
        // and each document has a field "status" to indicate payment status
        CollectionReference subsRef = firestore.collection("subscriptions");

        subsRef.whereEqualTo("status", "paid") // filter to only paid subscriptions
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        paidSubscriptions.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            // Convert Firestore document to Subscription model
                            Subscription sub = doc.toObject(Subscription.class);
                            // Optionally set subscriptionId if not included in model mapping
                            sub.setSubscriptionId(doc.getId());

                            paidSubscriptions.add(sub);
                        }

                        adapter.notifyDataSetChanged();

                    } else {
                        Log.e("PaymentsFragment", "Error fetching paid subscriptions", task.getException());
                    }
                });
    }
}
