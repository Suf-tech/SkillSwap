package com.example.skillswap;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.database.Cursor;

public class UserDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private String userEmail;
    private String userName;
    private int avatarId;

    private ImageView profileAvatar;
    private TextView profileName;
    private TextView profileEmail;
    private LinearLayout containerUserPosts;
    private LinearLayout containerUserRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        db = new DatabaseHelper(this);

        userEmail = getIntent().getStringExtra("user_email");
        userName = getIntent().getStringExtra("user_name");
        avatarId = getIntent().getIntExtra("avatar_id", 0);

        Toolbar toolbar = findViewById(R.id.userDetailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("User Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        profileAvatar = findViewById(R.id.userDetailAvatar);
        profileName = findViewById(R.id.userDetailName);
        profileEmail = findViewById(R.id.userDetailEmail);
        containerUserPosts = findViewById(R.id.containerUserPosts);
        containerUserRequests = findViewById(R.id.containerUserRequests);

        if (userName == null || userName.isEmpty()) {
            userName = db.getUserName(userEmail);
        }

        profileName.setText(userName);
        profileEmail.setText(userEmail);
        setAvatar(profileAvatar, avatarId);

        loadUserPosts();
        loadUserRequests();
    }

    private void loadUserPosts() {
        containerUserPosts.removeAllViews();
        Cursor cursor = db.getPostsByUser(userEmail);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int postId = cursor.getInt(0);
                String have = cursor.getString(1);
                String want = cursor.getString(2);
                String msg = cursor.getString(3);
                String status = cursor.getString(4);

                LinearLayout row = (LinearLayout) getLayoutInflater().inflate(R.layout.item_admin_user_post, containerUserPosts, false);
                TextView tvHave = row.findViewById(R.id.postHave);
                TextView tvWant = row.findViewById(R.id.postWant);
                TextView tvMsg = row.findViewById(R.id.postMsg);
                TextView tvStatus = row.findViewById(R.id.postStatus);
                Button btnDelete = row.findViewById(R.id.btnDeletePost);

                tvHave.setText("Offered: " + have);
                tvWant.setText("Wanted: " + want);
                tvMsg.setText(msg == null ? "" : msg);
                tvStatus.setText(status);

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                if (db.deletePost(postId)) {
                                    containerUserPosts.removeView(row);
                                    Toast.makeText(this, "Post deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                containerUserPosts.addView(row);
            }
            cursor.close();
        }
    }

    private void loadUserRequests() {
        containerUserRequests.removeAllViews();
        Cursor cursor = db.getRequestsByUser(userEmail);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int reqId = cursor.getInt(0);
                String sender = cursor.getString(2);
                String receiver = cursor.getString(3);
                String offered = cursor.getString(4);
                String required = cursor.getString(5);
                String msg = cursor.getString(6);
                String status = cursor.getString(7);

                LinearLayout row = (LinearLayout) getLayoutInflater().inflate(R.layout.item_admin_user_request, containerUserRequests, false);
                TextView tvDirection = row.findViewById(R.id.reqDirection);
                TextView tvSkills = row.findViewById(R.id.reqSkills);
                TextView tvMsg = row.findViewById(R.id.reqMsg);
                TextView tvStatus = row.findViewById(R.id.reqStatusAdmin);
                Button btnDelete = row.findViewById(R.id.btnDeleteRequest);

                tvDirection.setText(sender + " → " + receiver);
                tvSkills.setText("Offered: " + offered + "  |  Wanted: " + required);
                tvMsg.setText(msg == null ? "" : msg);
                tvStatus.setText(status);

                btnDelete.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Delete Request")
                            .setMessage("Are you sure you want to delete this request? This action cannot be undone.")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                if (db.deleteRequest(reqId)) {
                                    containerUserRequests.removeView(row);
                                    Toast.makeText(this, "Request deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });

                containerUserRequests.addView(row);
            }
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