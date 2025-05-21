package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.predictibill.R;

public class SubscriptionsFragment extends Fragment {

    public SubscriptionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscriptions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button Add_subButton = view.findViewById(R.id.submit_button);
        Add_subButton.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_subscriptionsFragment_to_addSubscriptions);
        });
    }
}
