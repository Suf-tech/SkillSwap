package com.example.skillswap;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDetailActivity extends AppCompatActivity {

    // XML IDs: userDetailToolbar, userDetailAvatar, userDetailName, userDetailGender, userDetailEmail, containerUserPosts, containerUserRequests
    private DatabaseReference mDatabase;
    private String userEmail, userId, userName;
    private int avatarId;

    private ImageView profileAvatar;
    private TextView profileName, profileGender, profileEmail;
    private LinearLayout containerUserPosts, containerUserRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 1. Intent Data capture (Sync with Admin/Home activities)
        // Check both common keys to prevent null userId
        userId = getIntent().getStringExtra("user_id");
        if (userId == null) userId = getIntent().getStringExtra("uid");

        userEmail = getIntent().getStringExtra("user_email");
        userName = getIntent().getStringExtra("user_name");
        avatarId = getIntent().getIntExtra("avatar_id", 0);

        // 2. Toolbar Setup
        Toolbar toolbar = findViewById(R.id.userDetailToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("User Profile Audit");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // 3. Bind Views (Sync with XML IDs)
        profileAvatar = findViewById(R.id.userDetailAvatar);
        profileName = findViewById(R.id.userDetailName);
        profileGender = findViewById(R.id.userDetailGender);
        profileEmail = findViewById(R.id.userDetailEmail);
        containerUserPosts = findViewById(R.id.containerUserPosts);
        containerUserRequests = findViewById(R.id.containerUserRequests);

        // 4. Set Initial Data
        profileName.setText(userName != null ? userName : "SkillSwap Member");
        profileEmail.setText(userEmail != null ? userEmail : "No email found");
        setAvatar(profileAvatar, avatarId);

        // 5. Load Live Data from Firebase
        if (userId != null && !userId.isEmpty()) {
            loadUserBasicInfo();
            loadUserPostsFromFirebase();
            loadUserRequestsFromFirebase();
        } else {
            Toast.makeText(this, "Critical Error: User ID is missing!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadUserBasicInfo() {
        mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String gender = snapshot.child("gender").getValue(String.class);
                    profileGender.setText("Gender: " + (gender != null ? gender : "Not Specified"));

                    // Agar avatarId intent mein purana tha to yahan refresh karein
                    if (snapshot.hasChild("avatarId")) {
                        avatarId = snapshot.child("avatarId").getValue(Integer.class);
                        setAvatar(profileAvatar, avatarId);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUserPostsFromFirebase() {
        mDatabase.child("Posts").orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        containerUserPosts.removeAllViews();
                        for (DataSnapshot postSnap : snapshot.getChildren()) {
                            String postId = postSnap.getKey();
                            String have = postSnap.child("have").getValue(String.class);
                            String want = postSnap.child("want").getValue(String.class);

                            // Check if post is active
                            boolean isOpen = !postSnap.hasChild("isOpen") || Boolean.TRUE.equals(postSnap.child("isOpen").getValue(Boolean.class));
                            String status = isOpen ? "Active" : "Locked (Swap Accepted)";

                            View row = getLayoutInflater().inflate(R.layout.item_admin_user_post, containerUserPosts, false);
                            ((TextView) row.findViewById(R.id.postHave)).setText("Offered: " + have);
                            ((TextView) row.findViewById(R.id.postWant)).setText("Wanted: " + want);
                            ((TextView) row.findViewById(R.id.postStatus)).setText(status);

                            row.findViewById(R.id.btnDeletePost).setOnClickListener(v -> {
                                deleteItem("Posts", postId, "Post removed from database");
                            });

                            containerUserPosts.addView(row);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadUserRequestsFromFirebase() {
        mDatabase.child("Requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                containerUserRequests.removeAllViews();
                for (DataSnapshot reqSnap : snapshot.getChildren()) {
                    String senderId = reqSnap.child("senderId").getValue(String.class);
                    String receiverId = reqSnap.child("receiverId").getValue(String.class);

                    // User involve hai ya nahi (as sender or receiver)
                    if (userId.equals(senderId) || userId.equals(receiverId)) {
                        String reqId = reqSnap.getKey();
                        String senderName = reqSnap.child("senderName").getValue(String.class);
                        String receiverName = reqSnap.child("receiverName").getValue(String.class);
                        String status = reqSnap.child("status").getValue(String.class);

                        View row = getLayoutInflater().inflate(R.layout.item_admin_user_request, containerUserRequests, false);
                        ((TextView) row.findViewById(R.id.reqDirection)).setText(senderName + " → " + receiverName);
                        ((TextView) row.findViewById(R.id.reqStatusAdmin)).setText("Status: " + status);

                        row.findViewById(R.id.btnDeleteRequest).setOnClickListener(v -> {
                            deleteItem("Requests", reqId, "Swap request deleted");
                        });

                        containerUserRequests.addView(row);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void deleteItem(String node, String id, String successMsg) {
        new AlertDialog.Builder(this)
                .setTitle("Admin Control: Delete")
                .setMessage("Are you sure? This action cannot be undone.")
                .setPositiveButton("Confirm Delete", (dialog, which) -> {
                    mDatabase.child(node).child(id).removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(UserDetailActivity.this, successMsg, Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton("Keep It", null)
                .setIcon(android.R.drawable.ic_menu_delete)
                .show();
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