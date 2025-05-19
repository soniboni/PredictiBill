package com.example.predictibill.ui.subscriptions;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.predictibill.R;

public class AddSubscriptionFragment extends Fragment {
    public AddSubscriptionFragment() {
        super(R.layout.fragments_add_subscriptions);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Use NavController if using Jetpack Navigation
            NavHostFragment.findNavController(this).navigateUp();

        });
    }
}
