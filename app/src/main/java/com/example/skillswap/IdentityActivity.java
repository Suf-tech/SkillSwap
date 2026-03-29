package com.example.skillswap;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class IdentityActivity extends AppCompatActivity {

    TextView idName, idEmail, taughtCount, learnedCount;
    ImageView idAvatar;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity); // Ab ye error nahi dega

        db = new DatabaseHelper(this);
        String email = getIntent().getStringExtra("user_email");

        idName = findViewById(R.id.idName);
        idEmail = findViewById(R.id.idEmail);
        idAvatar = findViewById(R.id.idAvatar);
        taughtCount = findViewById(R.id.taughtCount);
        learnedCount = findViewById(R.id.learnedCount);

        // Load Basic Info
        idName.setText(db.getUserName(email));
        idEmail.setText(email);

        int avatarId = db.getAvatarId(email);
        setAvatar(avatarId);

        // Calculate Stats (Taught vs Learned)
        calculateStats(email);
    }

    private void calculateStats(String email) {
        int taught = 0;
        int learned = 0;

        Cursor cursor = db.getMyRequests(email);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String status = cursor.getString(6); // Status Column
                String sender = cursor.getString(1); // Sender Column

                if (status.equalsIgnoreCase("Accepted")) {
                    if (sender.equalsIgnoreCase(email)) {
                        learned++; // Maine seekhne ki request bheji thi
                    } else {
                        taught++; // Mujhe sikhane ki request aayi thi
                    }
                }
            }
            cursor.close();
        }
        taughtCount.setText(String.valueOf(taught));
        learnedCount.setText(String.valueOf(learned));
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        idAvatar.setImageResource(res);
    }
}