package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.predictibill.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SubscriptionsFragment extends Fragment {

    private RecyclerView subscriptionsRecyclerView;
    private TextView noSubscriptionsTextView;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subscriptions, container, false);

        subscriptionsRecyclerView = root.findViewById(R.id.subscription_recycler_view);
        noSubscriptionsTextView = root.findViewById(R.id.noSubscriptionsTextView);

        Button submitButton = root.findViewById(R.id.submit_button);
        // Updated listener to pass the view to onAddButtonClicked
        submitButton.setOnClickListener(this::onAddButtonClicked);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadSubscriptions();
    }

    private void loadSubscriptions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            fetchUserSubscriptions(uid);
        } else {
            showNoSubscriptionsMessage("No current subscriptions listed.");
        }
    }

    private void fetchUserSubscriptions(String uid) {
        databaseReference.child("subscriptions").child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            displaySubscriptions(snapshot);
                        } else {
                            showNoSubscriptionsMessage("No current subscriptions listed.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        showNoSubscriptionsMessage("Failed to load subscriptions.");
                    }
                });
    }

    private void displaySubscriptions(DataSnapshot snapshot) {
        noSubscriptionsTextView.setVisibility(View.GONE);
        subscriptionsRecyclerView.setVisibility(View.VISIBLE);

        // TODO: Parse snapshot and set adapter with subscription data
    }

    private void showNoSubscriptionsMessage(String message) {
        noSubscriptionsTextView.setText(message);
        noSubscriptionsTextView.setVisibility(View.VISIBLE);
        subscriptionsRecyclerView.setVisibility(View.GONE);
    }

    // Updated method signature to accept View for Navigation controller
    private void onAddButtonClicked(View view) {
        Navigation.findNavController(view)
                .navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
    }
}