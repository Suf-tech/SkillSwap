package com.example.skillswap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddPostActivity extends AppCompatActivity {

    EditText have, want, msg;
    ImageView profilePic;
    Button postBtn;
    DatabaseHelper db;
    String userEmail;
    int myAvatarId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        db = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        userEmail = sp.getString("user_email", "");

        // Mapping IDs from XML
        have = findViewById(R.id.postHave);
        want = findViewById(R.id.postWant);
        msg = findViewById(R.id.postMsg);
        profilePic = findViewById(R.id.myProfilePic);
        postBtn = findViewById(R.id.submitPostBtn);

        // Load current user avatar
        myAvatarId = db.getAvatarId(userEmail);
        setAvatar(myAvatarId);

        postBtn.setOnClickListener(v -> {
            String h = have.getText().toString().trim();
            String w = want.getText().toString().trim();
            String m = msg.getText().toString().trim();
            String name = db.getUserName(userEmail);

            if (h.isEmpty() || w.isEmpty()) {
                Toast.makeText(this, "Please fill in skills!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.addPost(userEmail, name, h, w, m, myAvatarId)) {
                Toast.makeText(this, "Skill Swap Post Created!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Error: Could not create post", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        profilePic.setImageResource(res);
    }
}