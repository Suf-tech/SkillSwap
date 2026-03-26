package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    EditText name, email, password;
    Button signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(name.getText()) ||
                    TextUtils.isEmpty(email.getText()) ||
                    TextUtils.isEmpty(password.getText())) {

                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                // Signup ke baad wapas Login screen par bhejna
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }
}