package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PersonalInfoActivity extends AppCompatActivity {

    // XML IDs match: editName, editEmail, editPass, genderRadioGroup, btnUpdateInfo, currentSelectedAv
    private EditText editName, editEmail;
    private TextInputEditText editPass; // Sync with XML Material Component
    private RadioGroup genderRadioGroup;
    private ImageView currentAv;
    private Button btnUpdate;

    private DatabaseReference mDatabase;
    private String userId;
    private int selectedAvatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        // Session se UID lein
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = sp.getString("userId", "");

        if (userId.isEmpty()) {
            Toast.makeText(this, "Session error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        // Bindings
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPass = findViewById(R.id.editPass);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        btnUpdate = findViewById(R.id.btnUpdateInfo);
        currentAv = findViewById(R.id.currentSelectedAv);

        loadUserProfile();
        setupAvatarClicks();

        btnUpdate.setOnClickListener(v -> updateProfile());
    }

    private void loadUserProfile() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editName.setText(snapshot.child("name").getValue(String.class));
                    editEmail.setText(snapshot.child("email").getValue(String.class));
                    editEmail.setEnabled(false); // Email locked (Primary Key)

                    String gender = snapshot.child("gender").getValue(String.class);
                    if ("Female".equalsIgnoreCase(gender)) {
                        genderRadioGroup.check(R.id.radioFemale);
                    } else {
                        genderRadioGroup.check(R.id.radioMale);
                    }

                    if (snapshot.hasChild("avatarId")) {
                        Object avObj = snapshot.child("avatarId").getValue();
                        selectedAvatarId = (avObj instanceof Long) ? ((Long) avObj).intValue() : (Integer) avObj;
                        updatePreview(selectedAvatarId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateProfile() {
        String newName = editName.getText().toString().trim();
        String newPass = editPass.getText().toString().trim();
        String newGender = (genderRadioGroup.getCheckedRadioButtonId() == R.id.radioFemale) ? "Female" : "Male";

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("gender", newGender);
        updates.put("avatarId", selectedAvatarId);

        if (!newPass.isEmpty()) {
            if (newPass.length() < 8) {
                Toast.makeText(this, "Password must be 8+ characters", Toast.LENGTH_SHORT).show();
                return;
            }
            updates.put("password", newPass);
        }

        mDatabase.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Update Firebase Auth password in background
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && !newPass.isEmpty()) {
                    user.updatePassword(newPass);
                }

                // --- SESSION UPDATE (Optional but good) ---
                // Agar aap kahi'n UserName use kar rahe hain to usey bhi refresh karein

                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
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
        // av1 to av6 are standard IDs from your XML
        findViewById(R.id.av1).setOnClickListener(v -> { selectedAvatarId = 1; updatePreview(1); });
        findViewById(R.id.av2).setOnClickListener(v -> { selectedAvatarId = 2; updatePreview(2); });
        findViewById(R.id.av3).setOnClickListener(v -> { selectedAvatarId = 3; updatePreview(3); });
        findViewById(R.id.av4).setOnClickListener(v -> { selectedAvatarId = 4; updatePreview(4); });
        findViewById(R.id.av5).setOnClickListener(v -> { selectedAvatarId = 5; updatePreview(5); });
        findViewById(R.id.av6).setOnClickListener(v -> { selectedAvatarId = 6; updatePreview(6); });
    }
}