package com.example.skillswap;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    ImageView detailAvatar;
    TextView detailName, detailEmail, detailHave, detailWant, detailMessage;
    EditText requestInputMsg;
    Button sendRequestBtn;
    DatabaseHelper db;
    String postOwnerEmail, postTitle, postTeacher, currentUserEmail;
    int postAvatarId, postId;
    String fullWant = "", fullMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = new DatabaseHelper(this);
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserEmail = sp.getString("user_email", "");

        // Fetch the extra ID
        postId = getIntent().getIntExtra("postId", -1);
        postOwnerEmail = getIntent().getStringExtra("email");
        postTitle = getIntent().getStringExtra("title");
        postTeacher = getIntent().getStringExtra("teacher");
        postAvatarId = getIntent().getIntExtra("avatarId", 0);

        detailAvatar = findViewById(R.id.detailAvatar);
        detailName = findViewById(R.id.detailName);
        detailEmail = findViewById(R.id.detailEmail);
        detailHave = findViewById(R.id.detailHave);
        detailWant = findViewById(R.id.detailWant);
        detailMessage = findViewById(R.id.detailMessage);
        requestInputMsg = findViewById(R.id.requestInputMsg);
        sendRequestBtn = findViewById(R.id.sendRequestBtn);

        fetchFullPostDetails();

        detailName.setText(postTeacher);
        detailEmail.setText(postOwnerEmail);
        detailHave.setText(postTitle);
        detailWant.setText(fullWant);
        setAvatar(detailAvatar, postAvatarId);

        if (!fullMsg.isEmpty()) {
            detailMessage.setText("\"" + fullMsg + "\"");
        }

        sendRequestBtn.setOnClickListener(v -> {
            if (currentUserEmail.equals(postOwnerEmail)) {
                Toast.makeText(this, "You cannot send a request to yourself.", Toast.LENGTH_SHORT).show();
                return;
            }

            String customMsg = requestInputMsg.getText().toString().trim();
            if (customMsg.isEmpty()) customMsg = "I am interested in your swap offer!";

            // FIX: Sending the exact postId to the database
            if (db.sendRequest(postId, currentUserEmail, postOwnerEmail, fullWant, postTitle, customMsg)) {
                Toast.makeText(this, "Swap Request Sent Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to send request.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchFullPostDetails() {
        Cursor cursor = db.getReadableDatabase().rawQuery("SELECT skill_want, message FROM posts WHERE id = ?", new String[]{String.valueOf(postId)});
        if (cursor != null && cursor.moveToFirst()) {
            fullWant = cursor.getString(0);
            fullMsg = cursor.getString(1);
            cursor.close();
        }
    }

    private void setAvatar(ImageView iv, int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        iv.setImageResource(res);
    }
}