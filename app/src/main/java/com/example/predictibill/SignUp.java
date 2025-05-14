package com.example.predictibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SignUp extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up); // Links to res/layout/sign_up.xml

        Button signUpButton = findViewById(R.id.signUp_button);
        TextView loginFooterTxt = findViewById(R.id.loginFooter_txt);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, AccountCreated.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Optional transition
            }
        });

        loginFooterTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Optional animation
            }
        });
    }
}
