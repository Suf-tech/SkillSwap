package com.example.skillswap;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // Standard import add kiya
import com.google.android.material.textfield.TextInputLayout;

// FIXED: BaseActivity ki jagah AppCompatActivity use karein
public class ForgetPasswordActivity extends AppCompatActivity {

    EditText resetEmail, newPassword, confirmNewPassword;
    TextInputLayout newPasswordLayout, confirmNewPasswordLayout;
    Button updateBtn;
    TextView backToLogin;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        db = new DatabaseHelper(this);

        // Views Mapping
        resetEmail = findViewById(R.id.resetEmail);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmNewPasswordLayout = findViewById(R.id.confirmNewPasswordLayout);
        updateBtn = findViewById(R.id.updateBtn);
        backToLogin = findViewById(R.id.backToLogin);

        updateBtn.setOnClickListener(v -> {
            String emailStr = resetEmail.getText().toString().trim();
            String nPass = newPassword.getText().toString();
            String cPass = confirmNewPassword.getText().toString();

            newPasswordLayout.setError(null);
            confirmNewPasswordLayout.setError(null);

            if (TextUtils.isEmpty(emailStr)) {
                resetEmail.setError("Email required");
                return;
            }

            if (!db.checkEmail(emailStr)) {
                Toast.makeText(this, "Email not registered!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (nPass.length() < 8) {
                newPasswordLayout.setError("Min 8 characters required");
                return;
            }

            if (!nPass.equals(cPass)) {
                confirmNewPasswordLayout.setError("Passwords do not match");
                return;
            }

            if (db.updatePassword(emailStr, nPass)) {
                Toast.makeText(this, "Password Updated!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show();
            }
        });

        backToLogin.setOnClickListener(v -> finish());
    }
}