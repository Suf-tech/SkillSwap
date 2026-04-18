package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    // XML IDs: detailAvatar, detailName, detailEmail, detailHave, detailWant, detailMessage, requestInputMsg, sendRequestBtn
    private ImageView detailAvatar;
    private TextView detailName, detailEmail, detailHave, detailWant, detailMessage;
    private EditText requestInputMsg;
    private Button sendRequestBtn;

    private DatabaseReference mDatabase;
    private String currentUserId, currentUserName, currentUserEmail;

    private String postOwnerId, postOwnerEmail, postTitle, postOwnerName;
    private String postId;
    private int postAvatarId;
    private String fullWant = "", fullMsg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 1. Firebase & Session Initialization
        mDatabase = FirebaseDatabase.getInstance().getReference();
        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");
        currentUserEmail = sp.getString("userEmail", "");

        // 2. Bind Views (Sync with XML IDs)
        detailAvatar = findViewById(R.id.detailAvatar);
        detailName = findViewById(R.id.detailName);
        detailEmail = findViewById(R.id.detailEmail);
        detailHave = findViewById(R.id.detailHave);
        detailWant = findViewById(R.id.detailWant);
        detailMessage = findViewById(R.id.detailMessage);
        requestInputMsg = findViewById(R.id.requestInputMsg);
        sendRequestBtn = findViewById(R.id.sendRequestBtn);

        // 3. Get Intent Data
        postId = getIntent().getStringExtra("postId");

        // Fetch current user name for the request
        if (!currentUserId.isEmpty()) {
            mDatabase.child("Users").child(currentUserId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    currentUserName = snapshot.getValue(String.class);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        if (postId != null) {
            fetchPostDetailsFromFirebase();
        } else {
            Toast.makeText(this, "Error: Post ID not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 4. Send Request Logic
        sendRequestBtn.setOnClickListener(v -> {
            if (currentUserId.equals(postOwnerId)) {
                Toast.makeText(this, "You cannot send a request to your own post.", Toast.LENGTH_SHORT).show();
                return;
            }

            String customMsg = requestInputMsg.getText().toString().trim();
            // Default message if empty
            if (customMsg.isEmpty()) customMsg = "I am interested in your skill swap offer!";

            sendSwapRequest(customMsg);
        });
    }

    private void fetchPostDetailsFromFirebase() {
        mDatabase.child("Posts").child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    postOwnerId = snapshot.child("userId").getValue(String.class);

                    // Fallback keys check (Compatibility for both post types)
                    postOwnerName = snapshot.hasChild("teacher") ? snapshot.child("teacher").getValue(String.class) : snapshot.child("userName").getValue(String.class);
                    postOwnerEmail = snapshot.hasChild("email") ? snapshot.child("email").getValue(String.class) : snapshot.child("userEmail").getValue(String.class);

                    postTitle = snapshot.hasChild("title") ? snapshot.child("title").getValue(String.class) : snapshot.child("have").getValue(String.class);
                    fullWant = snapshot.child("want").getValue(String.class);
                    fullMsg = snapshot.child("message").getValue(String.class);
                    postAvatarId = snapshot.hasChild("avatarId") ? snapshot.child("avatarId").getValue(Integer.class) : 0;

                    // Update UI
                    detailName.setText(postOwnerName != null ? postOwnerName : "SkillSwap Member");
                    detailEmail.setText(postOwnerEmail != null ? postOwnerEmail : "");
                    detailHave.setText(postTitle != null ? postTitle : "No Skill Specified");
                    detailWant.setText(fullWant != null ? fullWant : "Anything");

                    // Message UI logic (Sync with italic style in XML)
                    if (fullMsg == null || fullMsg.isEmpty()) {
                        detailMessage.setText("\"No additional details provided by the user.\"");
                    } else {
                        detailMessage.setText("\"" + fullMsg + "\"");
                    }

                    setAvatar(detailAvatar, postAvatarId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendSwapRequest(String customMsg) {
        String requestId = mDatabase.child("Requests").push().getKey();

        if (requestId != null) {
            HashMap<String, Object> requestMap = new HashMap<>();
            requestMap.put("id", requestId);
            requestMap.put("postId", postId);
            requestMap.put("senderId", currentUserId);
            requestMap.put("senderName", currentUserName != null ? currentUserName : "New User");
            requestMap.put("senderEmail", currentUserEmail);
            requestMap.put("receiverId", postOwnerId);
            requestMap.put("receiverName", postOwnerName);
            requestMap.put("receiverEmail", postOwnerEmail);
            requestMap.put("offered", postTitle); // Model Sync
            requestMap.put("required", fullWant); // Model Sync
            requestMap.put("msg", customMsg);      // Model Sync
            requestMap.put("status", RequestModel.STATUS_PENDING);
            requestMap.put("timestamp", System.currentTimeMillis());

            mDatabase.child("Requests").child(requestId).setValue(requestMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DetailActivity.this, "Swap Request Sent Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DetailActivity.this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void setAvatar(ImageView iv, int id) {
        int res = R.drawable.editbox_background; // Default
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        iv.setImageResource(res);
    }
}