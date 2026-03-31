package com.example.skillswap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminEditUserActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editPass;
    private EditText editEmail;
    private ImageView currentAv;
    private Button btnUpdate;
    private DatabaseHelper db;
    private String userEmail;
    private int selectedAvatarId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        db = new DatabaseHelper(this);
        userEmail = getIntent().getStringExtra("user_email");

        Toolbar toolbar = findViewById(R.id.userDetailToolbar); // if you later add a toolbar id for this layout
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
        editPass = findViewById(R.id.editPass);
        btnUpdate = findViewById(R.id.btnUpdateInfo);
        currentAv = findViewById(R.id.currentSelectedAv);

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No user email provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editName.setText(db.getUserName(userEmail));
        editEmail.setText(userEmail);
        editEmail.setEnabled(false); // admin should not change email here
        String pass = db.getPassword(userEmail);
        if (pass != null) {
            editPass.setText(pass);
        }

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

            if (db.updateFullProfile(userEmail, newName, newPass, selectedAvatarId)) {
                Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show();
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

