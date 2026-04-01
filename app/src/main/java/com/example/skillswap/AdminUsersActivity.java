package com.example.skillswap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        db = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.adminUsersToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Users");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<AdminUser> users = loadUsers();
        adapter = new AdminUsersAdapter(this, users, new AdminUsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(AdminUser user) {
                Intent intent = new Intent(AdminUsersActivity.this, UserDetailActivity.class);
                intent.putExtra("user_email", user.email);
                intent.putExtra("user_name", user.name);
                intent.putExtra("avatar_id", user.avatarId);
                startActivity(intent);
            }

            @Override
            public void onUserEditClick(AdminUser user) {
                Intent intent = new Intent(AdminUsersActivity.this, AdminEditUserActivity.class);
                intent.putExtra("user_email", user.email);
                startActivity(intent);
            }
        });
        usersRecyclerView.setAdapter(adapter);
    }

    private List<AdminUser> loadUsers() {
        List<AdminUser> list = new ArrayList<>();
        Cursor cursor = db.getAllUsers();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String email = cursor.getString(0);
                String name = cursor.getString(1);
                int avatarId = cursor.getInt(2);
                list.add(new AdminUser(email, name, avatarId));
            }
            cursor.close();
        }
        return list;
    }
}
