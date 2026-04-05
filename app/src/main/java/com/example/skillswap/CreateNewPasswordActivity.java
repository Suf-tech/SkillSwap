package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateNewPasswordActivity extends AppCompatActivity {

    private EditText newPassword, confirmNewPassword;
    private Button btnSavePassword;
    private DatabaseHelper db;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_password);

        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("user_email");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Session error. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        btnSavePassword.setOnClickListener(v -> {
            String pass = newPassword.getText().toString().trim();
            String confirmPass = confirmNewPassword.getText().toString().trim();

            if (pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Please enter and confirm your new password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (pass.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update password and clear reset flag in DB
            if (db.updatePassword(userEmail, pass)) {
                // Save session now that password is reset
                getSharedPreferences("UserSession", MODE_PRIVATE)
                        .edit()
                        .putString("user_email", userEmail)
                        .apply();

                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                
                // Redirect to Home
                Intent intent = new Intent(CreateNewPasswordActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}