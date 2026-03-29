package com.example.skillswap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalInfoActivity extends AppCompatActivity {

    EditText editName, editPass, editEmail;
    ImageView currentAv;
    Button btnUpdate;
    DatabaseHelper db;
    String userEmail;
    int selectedAvatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        db = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sp.getString("user_email", "");

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPass = findViewById(R.id.editPass);
        btnUpdate = findViewById(R.id.btnUpdateInfo);
        currentAv = findViewById(R.id.currentSelectedAv);

        // Auto-fill Data from Database
        editName.setText(db.getUserName(userEmail));
        editEmail.setText(userEmail);
        editEmail.setEnabled(false); // Email lock
        editPass.setText(db.getPassword(userEmail));

        selectedAvatarId = db.getAvatarId(userEmail);
        updatePreview(selectedAvatarId);

        setupAvatarClicks();

        btnUpdate.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            String newPass = editPass.getText().toString().trim();

            if (newName.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Database update call
            if (db.updateFullProfile(userEmail, newName, newPass, selectedAvatarId)) {
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Go back to Profile
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePreview(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        currentAv.setImageResource(res);
    }

    private void setupAvatarClicks() {
        // Avatars clicking logic
        findViewById(R.id.av1).setOnClickListener(v -> { selectedAvatarId = 1; updatePreview(1); });
        findViewById(R.id.av2).setOnClickListener(v -> { selectedAvatarId = 2; updatePreview(2); });
        findViewById(R.id.av3).setOnClickListener(v -> { selectedAvatarId = 3; updatePreview(3); });
        findViewById(R.id.av4).setOnClickListener(v -> { selectedAvatarId = 4; updatePreview(4); });
        findViewById(R.id.av5).setOnClickListener(v -> { selectedAvatarId = 5; updatePreview(5); });
        findViewById(R.id.av6).setOnClickListener(v -> { selectedAvatarId = 6; updatePreview(6); });
    }
}