package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    ImageView profileAvatar;
    TextView profileName, profileEmail;
    LinearLayout btnPersonalInfo, btnIdentity, btnTheme;
    DatabaseHelper db;
    String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sp.getString("user_email", "");

        profileAvatar = findViewById(R.id.profileAvatar);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        btnTheme = findViewById(R.id.btnTheme);

        loadUserData();

        // THEME BUTTON DISABLED
        btnTheme.setOnClickListener(v -> {
            Toast.makeText(this, "Standard theme is active.", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnPersonalInfo).setOnClickListener(v ->
                startActivity(new Intent(this, PersonalInfoActivity.class)));

        findViewById(R.id.btnIdentity).setOnClickListener(v -> {
            Intent intent = new Intent(this, IdentityActivity.class);
            intent.putExtra("user_email", userEmail);
            startActivity(intent);
        });
    }

    private void loadUserData() {
        profileName.setText(db.getUserName(userEmail));
        profileEmail.setText(userEmail);
        setAvatar(db.getAvatarId(userEmail));
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        profileAvatar.setImageResource(res);
    }
}