package com.example.skillswap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditPostActivity extends AppCompatActivity {

    // XML IDs: editPostHave, editPostWant, editPostMsg, btnUpdatePost, btnDeletePost
    private EditText editHave, editWant, editMsg;
    private Button btnUpdatePost, btnDeletePost;

    private DatabaseReference mDatabase;
    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        // 1. Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        // 2. Bind Views (Sync with XML)
        editHave = findViewById(R.id.editPostHave);
        editWant = findViewById(R.id.editPostWant);
        editMsg = findViewById(R.id.editPostMsg);
        btnUpdatePost = findViewById(R.id.btnUpdatePost);
        btnDeletePost = findViewById(R.id.btnDeletePost);

        // 3. Get Post ID from Intent
        postId = getIntent().getStringExtra("postId");

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(this, "Error: Post ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4. Fetch Existing Data
        loadPostData();

        // 5. Button Listeners
        btnUpdatePost.setOnClickListener(v -> updatePostLogic());
        btnDeletePost.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void loadPostData() {
        mDatabase.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // SkillAdapter/SkillSelection keys match check
                    String have = snapshot.child("have").getValue(String.class);
                    String want = snapshot.child("want").getValue(String.class);
                    String message = snapshot.child("message").getValue(String.class);

                    editHave.setText(have != null ? have : "");
                    editWant.setText(want != null ? want : "");
                    editMsg.setText(message != null ? message : "");
                } else {
                    if (!isFinishing()) {
                        Toast.makeText(EditPostActivity.this, "Post no longer exists.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditPostActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePostLogic() {
        String h = editHave.getText().toString().trim();
        String w = editWant.getText().toString().trim();
        String m = editMsg.getText().toString().trim();

        if (h.isEmpty() || w.isEmpty()) {
            Toast.makeText(this, "Skills cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Updating multiple fields including title for the Dashboard
        HashMap<String, Object> updateMap = new HashMap<>();
        updateMap.put("have", h);
        updateMap.put("title", h);
        updateMap.put("want", w);
        updateMap.put("message", m);

        mDatabase.child(postId).updateChildren(updateMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Post Updated Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Update Failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to permanently remove this skill post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    mDatabase.child(postId).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Post Deleted Successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Delete operation failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}