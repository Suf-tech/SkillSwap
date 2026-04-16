package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SkillSelectionActivity extends AppCompatActivity {

    // XML IDs match: skillHave, skillWant, selectedAvatarPreview, finishBtn, imgM1...imgF3
    private EditText skillHave, skillWant;
    private ImageView preview, m1, m2, m3, f1, f2, f3;
    private Button finishBtn;

    private DatabaseReference mDatabase;
    private String currentUserId, userEmail, userName;
    private int selectedAvatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_selection);

        // 1. Session & Intent Data
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");
        userEmail = sp.getString("userEmail", "");

        // SignupActivity se bhej gaya naam capture karein
        userName = getIntent().getStringExtra("user_name");

        if (currentUserId.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Bind Views (Sync with XML)
        skillHave = findViewById(R.id.skillHave);
        skillWant = findViewById(R.id.skillWant);
        preview = findViewById(R.id.selectedAvatarPreview);
        finishBtn = findViewById(R.id.finishBtn);

        m1 = findViewById(R.id.imgM1); m2 = findViewById(R.id.imgM2); m3 = findViewById(R.id.imgM3);
        f1 = findViewById(R.id.imgF1); f2 = findViewById(R.id.imgF2); f3 = findViewById(R.id.imgF3);

        // 3. Avatar Click Listeners
        m1.setOnClickListener(v -> setAvatar(1, R.drawable.avatar_m1));
        m2.setOnClickListener(v -> setAvatar(2, R.drawable.avatar_m2));
        m3.setOnClickListener(v -> setAvatar(3, R.drawable.avatar_m3));
        f1.setOnClickListener(v -> setAvatar(4, R.drawable.avatar_f1));
        f2.setOnClickListener(v -> setAvatar(5, R.drawable.avatar_f2));
        f3.setOnClickListener(v -> setAvatar(6, R.drawable.avatar_f3));

        // 4. Finish Setup Logic
        finishBtn.setOnClickListener(v -> {
            String have = skillHave.getText().toString().trim();
            String want = skillWant.getText().toString().trim();

            if (have.isEmpty() || want.isEmpty()) {
                Toast.makeText(this, "Please enter your skills!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedAvatarId == 0) {
                Toast.makeText(this, "Please select an avatar!", Toast.LENGTH_SHORT).show();
                return;
            }

            completeProfileSetup(have, want);
        });
    }

    private void completeProfileSetup(String have, String want) {
        // Ensure name is not null (Fallback mechanism)
        String finalName = (userName != null && !userName.isEmpty()) ? userName : "SkillSwap User";

        // Step A: Update User Master Node
        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("avatarId", selectedAvatarId);
        userUpdates.put("name", finalName);

        mDatabase.child("Users").child(currentUserId).updateChildren(userUpdates);

        // Step B: Create the First Public Post
        String postId = mDatabase.child("Posts").push().getKey();
        if (postId != null) {
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("id", postId);
            postMap.put("userId", currentUserId);

            // Redundant keys for cross-compatibility with different Adapters
            postMap.put("email", userEmail);
            postMap.put("userEmail", userEmail);
            postMap.put("teacher", finalName);
            postMap.put("userName", finalName);

            postMap.put("title", have);
            postMap.put("have", have);
            postMap.put("want", want);
            postMap.put("avatarId", selectedAvatarId);
            postMap.put("isOpen", true);
            postMap.put("timestamp", System.currentTimeMillis());

            mDatabase.child("Posts").child(postId).setValue(postMap).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Welcome to SkillSwap, " + finalName + "!", Toast.LENGTH_SHORT).show();

                // Redirect to Home with clean stack
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setAvatar(int id, int res) {
        selectedAvatarId = id;
        preview.setImageResource(res);
        // Optional: Selected avatar ke charon taraf highlight de sakte hain
    }
}