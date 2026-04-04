package com.example.skillswap; // Make sure this matches your project package

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    EditText name, email, password, confirmPassword;
    TextInputLayout passwordLayout, confirmPasswordLayout;
    RadioGroup genderRadioGroup;
    Button signupBtn;
    TextView loginText;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 1. Initialize Database
        db = new DatabaseHelper(this);

        // 2. Initialize Views
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);

        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);

        signupBtn = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);

        // 3. Navigation: Click "Log In" to go to LoginActivity
        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Closes Signup screen so back button behaves correctly
        });

        // 4. Registration Logic: Click "Sign Up" button
        signupBtn.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            String e = email.getText().toString().trim();
            String p = password.getText().toString();
            String cp = confirmPassword.getText().toString();

            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String gender = "Male";
            if (selectedGenderId == R.id.radioFemale) {
                gender = "Female";
            }

            // Reset Errors
            passwordLayout.setError(null);
            confirmPasswordLayout.setError(null);

            // Validation 1: Check for empty fields
            if (TextUtils.isEmpty(n) || TextUtils.isEmpty(e) || TextUtils.isEmpty(p) || TextUtils.isEmpty(cp)) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validation 2: Check if email already exists
            if (db.checkEmail(e)) {
                email.setError("Email is already registered!");
                return;
            }

            // Validation 3: Check password length
            if (p.length() < 8) {
                passwordLayout.setError("Password must be at least 8 characters!");
                return;
            }

            // Validation 4: Check if passwords match
            if (!p.equals(cp)) {
                confirmPasswordLayout.setError("Passwords do not match!");
                return;
            }

            // 5. Insert Data into SQLite Database
            Boolean insert = db.insertData(e, n, p, gender);

            if (insert) {
                // Session Management: Save email to SharedPreferences
                getSharedPreferences("UserSession", MODE_PRIVATE)
                        .edit()
                        .putString("user_email", e)
                        .apply();

                Toast.makeText(this, "Account Created! Let's set up your profile.", Toast.LENGTH_SHORT).show();

                // Redirect to Skill Selection Screen
                Intent intent = new Intent(SignupActivity.this, SkillSelectionActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Registration Failed! Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}