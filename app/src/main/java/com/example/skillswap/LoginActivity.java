package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    // XML IDs: email, password, loginBtn, signupText, forgetPass
    private EditText email;
    private TextInputEditText password; // XML mein TextInputEditText hai
    private Button loginBtn;
    private TextView signupText, forgetPass;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Firebase Initialization
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Bind Views (Sync with XML)
        email = findViewById(R.id.email);
        password = findViewById(R.id.password); // ID matches TextInputEditText
        loginBtn = findViewById(R.id.loginBtn);
        signupText = findViewById(R.id.signupText);
        forgetPass = findViewById(R.id.forgetPass);

        // Note: ProgressBar XML mein nahi hai, isliye isay remove ya add karna hoga.
        // Crash se bachne ke liye maine progress logic handle kar liya hai.

        // 3. Login Button Click
        loginBtn.setOnClickListener(v -> {
            String inputEmail = email.getText().toString().trim();
            String inputPass = password.getText().toString().trim();

            if (TextUtils.isEmpty(inputEmail) || TextUtils.isEmpty(inputPass)) {
                Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show();
                return;
            }

            // --- ADMIN LOGIN CHECK ---
            if (inputEmail.equalsIgnoreCase("admin@skillswap.com") && inputPass.equals("admin123")) {
                handleAdminLogin();
                return;
            }

            // --- USER LOGIN VIA DATABASE ---
            loginViaDatabase(inputEmail, inputPass);
        });

        signupText.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        forgetPass.setOnClickListener(v -> startActivity(new Intent(this, ForgetPasswordActivity.class)));
    }

    private void loginViaDatabase(String inputEmail, String inputPass) {
        // XML mein progressBar nahi hai, isliye logic normal rakha hai
        loginBtn.setEnabled(false);
        loginBtn.setText("Logging in...");

        mDatabase.child("Users").orderByChild("email").equalTo(inputEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");

                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                String dbPassword = userSnap.child("password").getValue(String.class);
                                String userId = userSnap.getKey();
                                Boolean needsReset = userSnap.child("needsReset").getValue(Boolean.class);

                                if (dbPassword != null && dbPassword.equals(inputPass)) {

                                    // Step A: Session Save
                                    saveUserSession(userId, inputEmail);

                                    // Step B: Firebase Auth background sync
                                    mAuth.signInWithEmailAndPassword(inputEmail, inputPass);

                                    // Step C: Check if Admin forced a password reset
                                    if (Boolean.TRUE.equals(needsReset)) {
                                        Toast.makeText(LoginActivity.this, "Reset required by Admin", Toast.LENGTH_LONG).show();
                                        Intent resetIntent = new Intent(LoginActivity.this, CreateNewPasswordActivity.class);
                                        startActivity(resetIntent);
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                                        Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(homeIntent);
                                    }
                                    finish();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Invalid Password!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Email not registered!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        loginBtn.setEnabled(true);
                        loginBtn.setText("Login");
                        Toast.makeText(LoginActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserSession(String id, String email) {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userId", id);
        editor.putString("userEmail", email);
        editor.apply();
    }

    private void handleAdminLogin() {
        // Admin Session
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().putBoolean("isLoggedIn", true).putString("role", "admin").apply();

        Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}