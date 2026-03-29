package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // Standard activity import

// FIXED: BaseActivity ki jagah AppCompatActivity use karein
public class SkillSelectionActivity extends AppCompatActivity {

    EditText skillHave, skillWant;
    ImageView preview, m1, m2, m3, f1, f2, f3;
    Button finishBtn;
    DatabaseHelper db;
    int selectedAvatarId = 0;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_selection);

        db = new DatabaseHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sp.getString("user_email", "");

        // Initialize Views
        skillHave = findViewById(R.id.skillHave);
        skillWant = findViewById(R.id.skillWant);
        preview = findViewById(R.id.selectedAvatarPreview);
        finishBtn = findViewById(R.id.finishBtn);

        // Mapping ImageViews from XML
        m1 = findViewById(R.id.imgM1); m2 = findViewById(R.id.imgM2); m3 = findViewById(R.id.imgM3);
        f1 = findViewById(R.id.imgF1); f2 = findViewById(R.id.imgF2); f3 = findViewById(R.id.imgF3);

        // Click Listeners for Avatar Selection
        m1.setOnClickListener(v -> setAvatar(1, R.drawable.avatar_m1));
        m2.setOnClickListener(v -> setAvatar(2, R.drawable.avatar_m2));
        m3.setOnClickListener(v -> setAvatar(3, R.drawable.avatar_m3));
        f1.setOnClickListener(v -> setAvatar(4, R.drawable.avatar_f1));
        f2.setOnClickListener(v -> setAvatar(5, R.drawable.avatar_f2));
        f3.setOnClickListener(v -> setAvatar(6, R.drawable.avatar_f3));

        finishBtn.setOnClickListener(v -> {
            String have = skillHave.getText().toString().trim();
            String want = skillWant.getText().toString().trim();

            if (have.isEmpty() || want.isEmpty() || selectedAvatarId == 0) {
                Toast.makeText(this, "Select an avatar and fill both skills!", Toast.LENGTH_SHORT).show();
            } else {
                // Database update
                boolean updated = db.updateAvatar(userEmail, selectedAvatarId);

                if(updated) {
                    Toast.makeText(this, "Profile Setup Complete!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SkillSelectionActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Error updating profile!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setAvatar(int id, int res) {
        selectedAvatarId = id;
        preview.setImageResource(res);
    }
}