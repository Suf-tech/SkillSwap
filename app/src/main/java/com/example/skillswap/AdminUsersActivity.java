package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter adapter;
    private List<AdminUser> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        Toolbar toolbar = findViewById(R.id.adminUsersToolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("User Management");
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        if (usersRecyclerView != null) {
            usersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

            adapter = new AdminUsersAdapter(this, userList, new AdminUsersAdapter.OnUserClickListener() {
                @Override
                public void onUserClick(AdminUser user) {
                    Intent intent = new Intent(AdminUsersActivity.this, UserDetailActivity.class);
                    intent.putExtra("user_id", user.uid); // Zaroori hai
                    intent.putExtra("user_email", user.email);
                    intent.putExtra("user_name", user.name);
                    intent.putExtra("avatar_id", user.avatarId);
                    startActivity(intent);
                }

                @Override
                public void onUserEditClick(AdminUser user) {
                    Intent intent = new Intent(AdminUsersActivity.this, AdminEditUserActivity.class);
                    intent.putExtra("user_id", user.uid); // Zaroori hai
                    intent.putExtra("user_email", user.email);
                    startActivity(intent);
                }
            });
            usersRecyclerView.setAdapter(adapter);
        }

        fetchUsersFromFirebase();
    }

    private void fetchUsersFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    try {
                        // FIX: UID yahan se fetch karein
                        String uid = userSnap.getKey();
                        String email = userSnap.child("email").getValue(String.class);
                        String name = userSnap.child("name").getValue(String.class);

                        int avatarId = 0;
                        if (userSnap.hasChild("avatarId")) {
                            Object avObj = userSnap.child("avatarId").getValue();
                            avatarId = (avObj instanceof Long) ? ((Long) avObj).intValue() : (avObj instanceof Integer ? (Integer) avObj : 0);
                        }

                        if (email != null && uid != null) {
                            // AB 4 ARGUMENTS PASS HONGE: (uid, email, name, avatarId)
                            userList.add(new AdminUser(uid, email, name != null ? name : "Unknown", avatarId));
                        }
                    } catch (Exception e) {
                        Log.e("AdminUsers", "Parsing error: " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}