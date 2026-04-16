package com.example.skillswap;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.Random;

public class ForgetPasswordActivity extends AppCompatActivity {

    // XML IDs: layoutEmail, layoutOTP, layoutPassword, resetEmail, etOTP, newPassword, confirmNewPassword, btnSendOTP, btnVerifyOTP, updateBtn, btnResendOTP, backToLogin
    private LinearLayout layoutEmail, layoutOTP, layoutPassword;
    private EditText resetEmail, etOTP;
    private TextInputEditText newPassword, confirmNewPassword; // Matching XML types
    private Button btnSendOTP, btnVerifyOTP, updateBtn;
    private TextView btnResendOTP, backToLogin;

    private DatabaseReference mDatabase;
    private String generatedOTP;
    private String targetUserId = "";
    private String oldPasswordFromDB = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        // 1. UI Binding (Sync with XML)
        layoutEmail = findViewById(R.id.layoutEmail);
        layoutOTP = findViewById(R.id.layoutOTP);
        layoutPassword = findViewById(R.id.layoutPassword);

        resetEmail = findViewById(R.id.resetEmail);
        etOTP = findViewById(R.id.etOTP);

        // Match TextInputLayout in XML
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);

        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        updateBtn = findViewById(R.id.updateBtn);
        btnResendOTP = findViewById(R.id.btnResendOTP);
        backToLogin = findViewById(R.id.backToLogin);

        // Step 1: Send OTP Logic
        btnSendOTP.setOnClickListener(v -> {
            String email = resetEmail.getText().toString().trim();
            if (email.isEmpty()) {
                resetEmail.setError("Please enter your registered email");
                return;
            }
            checkEmailAndSendOTP(email);
        });

        // Step 2: Verify OTP Logic
        btnVerifyOTP.setOnClickListener(v -> {
            String enteredOTP = etOTP.getText().toString().trim();
            if (enteredOTP.equals(generatedOTP)) {
                layoutOTP.setVisibility(View.GONE);
                layoutPassword.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Verification failed: Invalid OTP!", Toast.LENGTH_SHORT).show();
            }
        });

        // Resend OTP
        btnResendOTP.setOnClickListener(v -> generateAndSendOTP());

        // Step 3: Final Password Update
        updateBtn.setOnClickListener(v -> {
            String nPass = newPassword.getText().toString().trim();
            String cPass = confirmNewPassword.getText().toString().trim();

            if (nPass.length() < 8) {
                newPassword.setError("Password must be at least 8 characters");
                return;
            }
            if (!nPass.equals(cPass)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nPass.equals(oldPasswordFromDB)) {
                Toast.makeText(this, "Security Alert: Choose a different password than old one!", Toast.LENGTH_SHORT).show();
                return;
            }

            updatePasswordInFirebase(nPass);
        });

        backToLogin.setOnClickListener(v -> finish());
    }

    private void checkEmailAndSendOTP(String email) {
        mDatabase.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        targetUserId = userSnap.getKey();
                        oldPasswordFromDB = userSnap.child("password").getValue(String.class);
                    }
                    generateAndSendOTP();
                    layoutEmail.setVisibility(View.GONE);
                    layoutOTP.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ForgetPasswordActivity.this, "Email address not found!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void generateAndSendOTP() {
        // Generating a 4-digit numeric OTP
        generatedOTP = String.valueOf(new Random().nextInt(8999) + 1000);

        // Simulation Toast (Since we don't have an SMS/Email Gateway yet)
        Toast.makeText(this, "DEBUG: OTP SENT TO " + resetEmail.getText().toString() + " is " + generatedOTP, Toast.LENGTH_LONG).show();
    }

    private void updatePasswordInFirebase(String pass) {
        if (targetUserId == null || targetUserId.isEmpty()) return;

        // Syncing with Alber bhai's Custom Login System (Database side)
        mDatabase.child(targetUserId).child("password").setValue(pass).addOnSuccessListener(aVoid -> {

            // Note: Firebase Auth Password reset should ideally happen via email link for security,
            // but since we are using a custom login system for Alber, this database update is priority.

            Toast.makeText(this, "Password updated! You can now login.", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "System error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}