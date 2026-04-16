package com.example.skillswap;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminSkillDetailActivity extends AppCompatActivity {

    // XML IDs: skillDetailToolbar, skillUsageText, skillRequestCountText, recentRequestersRecyclerView
    private DatabaseReference mDatabase;
    private String skillName;
    private TextView usageText, requestCountText;
    private RecyclerView recentList;
    private List<AdminSkillRequester> requesters;
    private AdminSkillRequesterAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_skill_detail);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Intent se skill ka naam lein
        skillName = getIntent().getStringExtra("skill_name");
        if (skillName == null) skillName = "Unknown Skill";

        // 1. Toolbar Setup (ID: skillDetailToolbar)
        Toolbar toolbar = findViewById(R.id.skillDetailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(skillName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // 2. View Initialization
        usageText = findViewById(R.id.skillUsageText);
        requestCountText = findViewById(R.id.skillRequestCountText);
        recentList = findViewById(R.id.recentRequestersRecyclerView);

        // 3. RecyclerView Setup
        recentList.setLayoutManager(new LinearLayoutManager(this));
        requesters = new ArrayList<>();
        adapter = new AdminSkillRequesterAdapter(requesters);
        recentList.setAdapter(adapter);

        // 4. Fetch Statistics
        fetchSkillStats();
    }

    private void fetchSkillStats() {
        // --- STEP 1: Check occurrences in POSTS node ---
        mDatabase.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int postCount = 0;
                requesters.clear(); // List clear karein taake duplicates na ayein

                for (DataSnapshot post : snapshot.getChildren()) {
                    // SkillAdapter/Home ke mutabiq keys check karein
                    String have = post.child("have").getValue(String.class);
                    String want = post.child("want").getValue(String.class);

                    if (skillName.equalsIgnoreCase(have) || skillName.equalsIgnoreCase(want)) {
                        postCount++;

                        // Requesters list mein add karein
                        String name = post.hasChild("teacher") ? post.child("teacher").getValue(String.class) : post.child("userName").getValue(String.class);
                        String email = post.hasChild("email") ? post.child("email").getValue(String.class) : post.child("userEmail").getValue(String.class);
                        int avatarId = post.hasChild("avatarId") ? post.child("avatarId").getValue(Integer.class) : 0;

                        if (name != null) {
                            requesters.add(new AdminSkillRequester(name, email, avatarId));
                        }
                    }
                }
                usageText.setText("This skill has " + postCount + " active posts.");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminSkillDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // --- STEP 2: Check occurrences in REQUESTS node ---
        mDatabase.child("Requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int reqCount = 0;
                for (DataSnapshot req : snapshot.getChildren()) {
                    // RequestModel ke mutabiq keys check karein (offered aur required)
                    String offered = req.child("offered").getValue(String.class);
                    String required = req.child("required").getValue(String.class);

                    if (skillName.equalsIgnoreCase(offered) || skillName.equalsIgnoreCase(required)) {
                        reqCount++;
                    }
                }
                requestCountText.setText("It has appeared in " + reqCount + " swap requests.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}