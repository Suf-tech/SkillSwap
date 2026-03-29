package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // Standard import add kiya
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;

// FIXED: BaseActivity ki jagah AppCompatActivity use karein
public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Skill> skillList;
    SkillAdapter adapter;
    DatabaseHelper db;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SkillSwap Dashboard");
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadPosts();

        // Bottom Navigation Logic
        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            }
            else if (id == R.id.nav_add) {
                startActivity(new Intent(HomeActivity.this, AddPostActivity.class));
                return true;
            }
            else if (id == R.id.nav_requests) {
                startActivity(new Intent(HomeActivity.this, RequestActivity.class));
                return true;
            }
            else if (id == R.id.nav_profile) {
                startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
                return true;
            }
            else if (id == R.id.nav_logout) {
                SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
                sp.edit().clear().apply();
                Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }

    private void loadPosts() {
        skillList = new ArrayList<>();
        Cursor cursor = db.getAllPosts();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String email = cursor.getString(1);
                String name = cursor.getString(2);
                String skillHave = cursor.getString(3);
                int avatar = cursor.getInt(6);

                skillList.add(new Skill(id, skillHave, name, email, avatar));
            }
            cursor.close();
        }

        adapter = new SkillAdapter(this, skillList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }
}