package com.example.skillswap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;

public class EditPostActivity extends AppCompatActivity {

    EditText editHave, editWant, editMsg;
    Button btnUpdatePost, btnDeletePost;
    DatabaseHelper db;
    int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        db = new DatabaseHelper(this);

        editHave = findViewById(R.id.editPostHave);
        editWant = findViewById(R.id.editPostWant);
        editMsg = findViewById(R.id.editPostMsg);
        btnUpdatePost = findViewById(R.id.btnUpdatePost);
        btnDeletePost = findViewById(R.id.btnDeletePost);

        // Intent se ID lein
        postId = getIntent().getIntExtra("postId", -1);

        if (postId != -1) {
            // Database se fresh data uthaein taake fields khali na rahen
            Cursor cursor = db.getPostById(postId);
            if (cursor != null && cursor.moveToFirst()) {
                editHave.setText(cursor.getString(3)); // skill_have
                editWant.setText(cursor.getString(4)); // skill_want
                editMsg.setText(cursor.getString(5));  // message
                cursor.close();
            }
        }

        // Update Logic
        btnUpdatePost.setOnClickListener(v -> {
            String h = editHave.getText().toString().trim();
            String w = editWant.getText().toString().trim();
            String m = editMsg.getText().toString().trim();

            if (h.isEmpty() || w.isEmpty()) {
                Toast.makeText(this, "Skills cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.updatePost(postId, h, w, m)) {
                Toast.makeText(this, "Changes Saved Successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Dashboard par wapas
            } else {
                Toast.makeText(this, "Error: Could not save changes", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Logic
        btnDeletePost.setOnClickListener(v -> {
            if (db.deletePost(postId)) {
                Toast.makeText(this, "Post Deleted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}