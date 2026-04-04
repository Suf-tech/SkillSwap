package com.example.skillswap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AdminEditUserActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editEmail;
    private RadioGroup genderRadioGroup;
    private ImageView currentAv;
    private SwitchMaterial switchForceReset;
    private Button btnUpdate;
    private DatabaseHelper db;
    private String userEmail;
    private int selectedAvatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_edit_user);

        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("user_email");

        Toolbar toolbar = findViewById(R.id.userDetailToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit User");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        switchForceReset = findViewById(R.id.switchForceReset);
        btnUpdate = findViewById(R.id.btnUpdateInfo);
        currentAv = findViewById(R.id.currentSelectedAv);

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No user email provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(db.getUserName(userEmail));
        editEmail.setText(userEmail);
        editEmail.setEnabled(false); 
        
        String gender = db.getUserGender(userEmail);
        if ("Female".equalsIgnoreCase(gender)) {
            genderRadioGroup.check(R.id.radioFemale);
        } else {
            genderRadioGroup.check(R.id.radioMale);
        }

        switchForceReset.setChecked(db.needsPasswordReset(userEmail));

        selectedAvatarId = db.getAvatarId(userEmail);
        updatePreview(selectedAvatarId);

        setupAvatarClicks();

        btnUpdate.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            boolean forceReset = switchForceReset.isChecked();

            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String newGender = "Male";
            if (selectedGenderId == R.id.radioFemale) {
                newGender = "Female";
            }

            if (newName.isEmpty()) {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.adminUpdateUser(userEmail, newName, newGender, selectedAvatarId, forceReset)) {
                Toast.makeText(this, "User updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update user", Toast.LENGTH_SHORT).show();
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
        findViewById(R.id.av1).setOnClickListener(v -> { selectedAvatarId = 1; updatePreview(1); });
        findViewById(R.id.av2).setOnClickListener(v -> { selectedAvatarId = 2; updatePreview(2); });
        findViewById(R.id.av3).setOnClickListener(v -> { selectedAvatarId = 3; updatePreview(3); });
        findViewById(R.id.av4).setOnClickListener(v -> { selectedAvatarId = 4; updatePreview(4); });
        findViewById(R.id.av5).setOnClickListener(v -> { selectedAvatarId = 5; updatePreview(5); });
        findViewById(R.id.av6).setOnClickListener(v -> { selectedAvatarId = 6; updatePreview(6); });
    }
}