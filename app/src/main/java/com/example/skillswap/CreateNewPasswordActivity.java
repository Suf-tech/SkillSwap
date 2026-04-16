package com.example.skillswap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class CreateNewPasswordActivity extends AppCompatActivity {

    // XML IDs: newPassword, confirmNewPassword, btnSavePassword
    private EditText newPassword, confirmNewPassword;
    private Button btnSavePassword;

    private FirebaseUser user;
    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

        // 1. Initialize Firebase & Session
        user = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sp.getString("userId", "");

        // Safety check: Agar user authenticated nahi hai to login pe bhejain
        if (user == null || userId.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 2. Bind Views (Matching your XML IDs)
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> performPasswordUpdate());
    }

    private void performPasswordUpdate() {
        String pass = newPassword.getText().toString().trim();
        String confirmPass = confirmNewPassword.getText().toString().trim();

        // Validations
        if (pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill both password fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 8) {
            Toast.makeText(this, "Security Alert: Password must be 8+ characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Step A: Update Firebase Authentication Password
        user.updatePassword(pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // 4. Step B: Update Database (Password AND needsReset flag)
                // Ye boht zaroori hai taake naya password database mein bhi sync ho jaye
                Map<String, Object> updates = new HashMap<>();
                updates.put("password", pass);
                updates.put("needsReset", false);

                mDatabase.child("Users").child(userId).updateChildren(updates)
                        .addOnCompleteListener(dbTask -> {
                            if (dbTask.isSuccessful()) {
                                Toast.makeText(this, "Security Setup Complete!", Toast.LENGTH_SHORT).show();

                                // Direct HomeActivity par bhejain
                                Intent intent = new Intent(this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(this, "Database Sync Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Aksar ye tab fail hota hai jab login purana ho jaye
                String error = task.getException() != null ? task.getException().getMessage() : "Unknown Error";
                Toast.makeText(this, "Critical Error: " + error, Toast.LENGTH_LONG).show();

                // Agar session purana hai to logout karwa k dobara login karwaen
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });
    }
}