package com.example.predictibill.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.predictibill.R;

public class AccountCreated extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_created);

        // Initialize views
        TextView fullNameText = findViewById(R.id.userFullName_txt);
        TextView emailText = findViewById(R.id.userEmail_txt);

        // Get data from intent
        Intent intent = getIntent();
        String firstName = intent.getStringExtra("firstName");
        String lastName = intent.getStringExtra("lastName");
        String email = intent.getStringExtra("email");

        // Display user information
        if (firstName != null && lastName != null) {
            fullNameText.setText(String.format("%s %s", firstName, lastName));
        }
        if (email != null) {
            emailText.setText(email);
        }

        // Set up continue button
        findViewById(R.id.continue_button).setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}