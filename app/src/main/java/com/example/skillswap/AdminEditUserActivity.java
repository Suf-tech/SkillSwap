package com.example.skillswap;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AdminEditUserActivity extends AppCompatActivity {

    // Variable declaration as per XML IDs
    private EditText editName, editEmail;
    private RadioGroup genderRadioGroup;
    private ImageView currentAv;
    private SwitchMaterial switchForceReset;
    private Button btnUpdate;

    private DatabaseReference mDatabase;
    private String targetUserId = "";
    private String userEmailFromIntent;
    private int selectedAvatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        // 1. Firebase & Intent Setup
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userEmailFromIntent = getIntent().getStringExtra("user_email");

        // 2. Toolbar Setup (ID: userDetailToolbar)
        Toolbar toolbar = findViewById(R.id.userDetailToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit User Profile");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // 3. Initialize XML Views
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        switchForceReset = findViewById(R.id.switchForceReset);
        btnUpdate = findViewById(R.id.btnUpdateInfo);
        currentAv = findViewById(R.id.currentSelectedAv);

        if (userEmailFromIntent == null || userEmailFromIntent.isEmpty()) {
            Toast.makeText(this, "Error: User email not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editEmail.setText(userEmailFromIntent);
        editEmail.setEnabled(false); // Admin email change nahi kar sakta, sirf view karega

        // 4. Load User Data from Firebase
        loadUserData();

        // 5. Setup Avatar Click Listeners (av1 to av6)
        setupAvatarClicks();

        // 6. Update Button Logic
        btnUpdate.setOnClickListener(v -> performUpdate());
    }

    private void loadUserData() {
        mDatabase.child("Users").orderByChild("email").equalTo(userEmailFromIntent)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnap : snapshot.getChildren()) {
                                targetUserId = userSnap.getKey(); // UID capture

                                editName.setText(userSnap.child("name").getValue(String.class));

                                String gender = userSnap.child("gender").getValue(String.class);
                                if ("Female".equalsIgnoreCase(gender)) {
                                    genderRadioGroup.check(R.id.radioFemale);
                                } else {
                                    genderRadioGroup.check(R.id.radioMale);
                                }

                                if (userSnap.hasChild("avatarId")) {
                                    selectedAvatarId = userSnap.child("avatarId").getValue(Integer.class);
                                    updatePreview(selectedAvatarId);
                                }

                                if (userSnap.hasChild("needsReset")) {
                                    switchForceReset.setChecked(Boolean.TRUE.equals(userSnap.child("needsReset").getValue(Boolean.class)));
                                }
                            }
                        } else {
                            Toast.makeText(AdminEditUserActivity.this, "User details not found in database", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("AdminEditError", error.getMessage());
                    }
                });
    }

    private void performUpdate() {
        String newName = editName.getText().toString().trim();
        boolean forceReset = switchForceReset.isChecked();

        int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
        String newGender = (selectedGenderId == R.id.radioFemale) ? "Female" : "Male";

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name field cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (targetUserId == null || targetUserId.isEmpty()) {
            Toast.makeText(this, "Cannot update: User ID missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // HashMap for partial update
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", newName);
        updateMap.put("gender", newGender);
        updateMap.put("avatarId", selectedAvatarId);
        updateMap.put("needsReset", forceReset);

        mDatabase.child("Users").child(targetUserId).updateChildren(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupAvatarClicks() {
        findViewById(R.id.av1).setOnClickListener(v -> { selectedAvatarId = 1; updatePreview(1); });
        findViewById(R.id.av2).setOnClickListener(v -> { selectedAvatarId = 2; updatePreview(2); });
        findViewById(R.id.av3).setOnClickListener(v -> { selectedAvatarId = 3; updatePreview(3); });
        findViewById(R.id.av4).setOnClickListener(v -> { selectedAvatarId = 4; updatePreview(4); });
        findViewById(R.id.av5).setOnClickListener(v -> { selectedAvatarId = 5; updatePreview(5); });
        findViewById(R.id.av6).setOnClickListener(v -> { selectedAvatarId = 6; updatePreview(6); });
    }

    private void updatePreview(int id) {
        int res = R.drawable.editbox_background; // Default
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        currentAv.setImageResource(res);
    }
}