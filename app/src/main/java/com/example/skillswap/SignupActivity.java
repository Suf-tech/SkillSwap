package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    EditText name, email, password, confirmPassword;
    TextInputLayout passwordLayout, confirmPasswordLayout;
    Button signupBtn;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Database and Views Initialization
        db = new DatabaseHelper(this);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        signupBtn = findViewById(R.id.signupBtn);

        signupBtn.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            String e = email.getText().toString().trim();
            String p = password.getText().toString();
            String cp = confirmPassword.getText().toString();

            // Reset Errors
            passwordLayout.setError(null);
            confirmPasswordLayout.setError(null);

            // 1. Basic Validation
            if (TextUtils.isEmpty(n) || TextUtils.isEmpty(e) || TextUtils.isEmpty(p) || TextUtils.isEmpty(cp)) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Email Already Exists Check
            if (db.checkEmail(e)) {
                email.setError("Email is already registered!");
                return;
            }

            // 3. Password Length Check
            if (p.length() < 8) {
                passwordLayout.setError("Password must be at least 8 characters!");
                return;
            }

            // 4. Confirm Password Match Check
            if (!p.equals(cp)) {
                confirmPasswordLayout.setError("Passwords do not match!");
                return;
            }

            // 5. Insert Data into SQLite
            Boolean insert = db.insertData(e, n, p);

            if (insert) {
                // --- PROFESSIONAL SESSION MANAGEMENT ---
                // Signup hote hi email save kar rahe hain taake SkillSelectionActivity ko user ka pata ho
                getSharedPreferences("UserSession", MODE_PRIVATE)
                        .edit()
                        .putString("user_email", e)
                        .apply();

                Toast.makeText(this, "Account Created! Let's set up your profile.", Toast.LENGTH_SHORT).show();

                // Redirect to Skill & Avatar Selection Screen
                Intent intent = new Intent(SignupActivity.this, SkillSelectionActivity.class);
                startActivity(intent);
                finish(); // Signup screen ko stack se khatam kar rahe hain
            } else {
                Toast.makeText(this, "Registration Failed! Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}