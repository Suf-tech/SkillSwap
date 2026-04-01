package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView signupText, forgetPass;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DatabaseHelper(this);

        // Mapping IDs (Ab XML mein ye IDs maujood hain)
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        signupText = findViewById(R.id.signupText);
        forgetPass = findViewById(R.id.forgetPass);

        loginBtn.setOnClickListener(v -> {
            String inputEmail = email.getText().toString().trim();
            String inputPass = password.getText().toString().trim();

            if (TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputPass)) {
                Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show();
            } else {
                // First: hardcoded admin login (no session)
                if (inputEmail.equals("admin") && inputPass.equals("adminpass")) {
                    Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                // Normal user login via database
                boolean checkUserPass = db.checkEmailPassword(inputEmail, inputPass);

                if (checkUserPass) {
                    // Save Session
                    getSharedPreferences("UserSession", MODE_PRIVATE)
                            .edit()
                            .putString("user_email", inputEmail)
                            .apply();

                    Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Click listeners for navigation
        signupText.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        forgetPass.setOnClickListener(v -> startActivity(new Intent(this, ForgetPasswordActivity.class)));
    }
}