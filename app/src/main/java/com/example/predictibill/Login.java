package com.example.predictibill;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        TextView signUpFooterTxt = findViewById(R.id.signUpFooter_txt);
        TextView forgotPasswordTxt = findViewById(R.id.forgotPassword_txt);
        signUpFooterTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Optional animation
            }
        });
        forgotPasswordTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // Optional animation
            }
        });
    }

}