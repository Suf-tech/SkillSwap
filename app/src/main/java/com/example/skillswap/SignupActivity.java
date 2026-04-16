package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    // XML IDs match: name, email, password, confirmPassword, genderRadioGroup, passwordLayout, confirmPasswordLayout, signupBtn, loginText
    private EditText name, email;
    private TextInputEditText password, confirmPassword; // Material Components sync
    private TextInputLayout passwordLayout, confirmPasswordLayout;
    private RadioGroup genderRadioGroup;
    private Button signupBtn;
    private TextView loginText;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 1. Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Bind Views (Sync with XML)
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        signupBtn = findViewById(R.id.signupBtn);
        loginText = findViewById(R.id.loginText);

        // Redirect to Login
        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });

        // 3. Signup Button Logic
        signupBtn.setOnClickListener(v -> performSignup());
    }

    private void performSignup() {
        String n = name.getText().toString().trim();
        String e = email.getText().toString().trim();
        String p = password.getText().toString().trim();
        String cp = confirmPassword.getText().toString().trim();

        // Clear errors
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        // Gender selection logic
        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String gender = (selectedGenderId == R.id.radioFemale) ? "Female" : "Male";

        // Validations
        if (TextUtils.isEmpty(n) || TextUtils.isEmpty(e) || TextUtils.isEmpty(p) || TextUtils.isEmpty(cp)) {
            Toast.makeText(this, "All fields are mandatory!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (p.length() < 8) {
            passwordLayout.setError("Password should be at least 8 characters!");
            return;
        }

        if (!p.equals(cp)) {
            confirmPasswordLayout.setError("Passwords mismatch!");
            return;
        }

        // 4. Firebase Auth: Create User
        signupBtn.setEnabled(false);
        signupBtn.setText("Creating Account...");

        mAuth.createUserWithEmailAndPassword(e, p).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();

                // Store user data in Realtime Database for Custom Login & Identity
                HashMap<String, Object> userMap = new HashMap<>();
                userMap.put("name", n);
                userMap.put("email", e);
                userMap.put("password", p); // Alber's Custom Login Requirement
                userMap.put("gender", gender);
                userMap.put("uid", userId);
                userMap.put("avatarId", 0); // Default avatar
                userMap.put("needsReset", false); // Default flag

                mDatabase.child("Users").child(userId).setValue(userMap).addOnCompleteListener(dbTask -> {
                    if (dbTask.isSuccessful()) {
                        // 5. Manage Local Session
                        saveUserSession(userId, e);

                        Toast.makeText(SignupActivity.this, "Welcome to SkillSwap!", Toast.LENGTH_SHORT).show();

                        // Redirect to SkillSelection
                        Intent intent = new Intent(SignupActivity.this, SkillSelectionActivity.class);
                        intent.putExtra("user_name", n);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                signupBtn.setEnabled(true);
                signupBtn.setText("Sign Up");
                Toast.makeText(SignupActivity.this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveUserSession(String id, String email) {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit()
                .putBoolean("isLoggedIn", true)
                .putString("userId", id)
                .putString("userEmail", email)
                .apply();
    }
}