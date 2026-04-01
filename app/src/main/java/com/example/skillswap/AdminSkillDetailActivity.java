package com.example.skillswap;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class AdminSkillDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_skill_detail);

        db = new DatabaseHelper(this);

        String skillName = getIntent().getStringExtra("skill_name");
        if (skillName == null) {
            skillName = "Unknown";
        }

        Toolbar toolbar = findViewById(R.id.skillDetailToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(skillName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView usageText = findViewById(R.id.skillUsageText);
        TextView requestCountText = findViewById(R.id.skillRequestCountText);

        int postCount = db.getSkillPostCount(skillName);
        int requestCount = db.getSkillRequestCount(skillName);

        usageText.setText("This skill has " + postCount + " appearances in posts.");
        requestCountText.setText("It has appeared in " + requestCount + " swap requests.");

        RecyclerView recentList = findViewById(R.id.recentRequestersRecyclerView);
        recentList.setLayoutManager(new LinearLayoutManager(this));

        List<AdminSkillRequester> requesters = new ArrayList<>();
        Cursor c = db.getRecentRequestUsersForSkill(skillName, 10);
        if (c != null) {
            while (c.moveToNext()) {
                String email = c.getString(0);
                String name = c.getString(1);
                int avatarId = c.getInt(2);
                if (name == null || name.isEmpty()) {
                    name = email != null ? email : "Unknown";
                }
                requesters.add(new AdminSkillRequester(name, email, avatarId));
            }
            c.close();
        }

        AdminSkillRequesterAdapter adapter = new AdminSkillRequesterAdapter(requesters);
        recentList.setAdapter(adapter);
    }
}

