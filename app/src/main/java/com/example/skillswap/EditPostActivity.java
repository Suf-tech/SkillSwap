package com.example.skillswap;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditPostActivity extends AppCompatActivity {

    EditText editHave, editWant, editMsg;
    Button updateBtn;
    DatabaseHelper db;
    int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add post wala layout hi use kar rahe hain
        setContentView(R.layout.activity_add_post);

        db = new DatabaseHelper(this);

        // Adapter se bheji gayi post id receive karein
        postId = getIntent().getIntExtra("post_id", -1);

        editHave = findViewById(R.id.postHave);
        editWant = findViewById(R.id.postWant);
        editMsg = findViewById(R.id.postMsg);
        updateBtn = findViewById(R.id.submitPostBtn);

        updateBtn.setText("Update Post");

        // Pehle se maujood data load karo fields mein
        loadPostData();

        updateBtn.setOnClickListener(v -> {
            String h = editHave.getText().toString().trim();
            String w = editWant.getText().toString().trim();
            String m = editMsg.getText().toString().trim();

            if (h.isEmpty() || w.isEmpty()) {
                Toast.makeText(this, "Skills are required!", Toast.LENGTH_SHORT).show();
            } else {
                // FIXED: updatePost method ab DatabaseHelper mein maujood hai
                boolean isUpdated = db.updatePost(postId, h, w, m);

                if (isUpdated) {
                    Toast.makeText(this, "Post Updated Successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Dashboard par wapas jao
                } else {
                    Toast.makeText(this, "Error: Could not update post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadPostData() {
        Cursor cursor = db.getPostById(postId);
        if (cursor != null && cursor.moveToFirst()) {
            // Index 3: skill_have, 4: skill_want, 5: message
            editHave.setText(cursor.getString(3));
            editWant.setText(cursor.getString(4));
            editMsg.setText(cursor.getString(5));
            cursor.close();
        }
    }
}