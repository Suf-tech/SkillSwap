package com.example.skillswap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private AdminDonutChartView chartRequested;
    private AdminDonutChartView chartOffered;

    private TextView tvTotalPosts;
    private TextView tvTotalRequests;
    private TextView tvTotalUsers;
    private LinearLayout containerTopRequested;
    private LinearLayout containerTopOffered;

    private final int[] SEGMENT_COLORS = new int[]{
            0xFF1976D2, // Blue
            0xFF388E3C, // Green
            0xFFFBC02D, // Amber
            0xFFE64A19, // Deep Orange
            0xFF7B1FA2, // Purple
            0xFF0097A7  // Teal
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = new DatabaseHelper(this);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        tvTotalPosts = findViewById(R.id.tvTotalPosts);
        tvTotalRequests = findViewById(R.id.tvTotalRequests);
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        containerTopRequested = findViewById(R.id.containerTopRequested);
        containerTopOffered = findViewById(R.id.containerTopOffered);

        chartRequested = findViewById(R.id.chartRequested);
        chartOffered = findViewById(R.id.chartOffered);

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigation);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_admin_overview) {
                    // Overview is current screen
                    return true;
                } else if (id == R.id.nav_admin_users) {
                    startActivity(new Intent(AdminDashboardActivity.this, AdminUsersActivity.class));
                    return true;
                } else if (id == R.id.nav_admin_reports) {
                    Toast.makeText(AdminDashboardActivity.this, "Reports (dummy)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_admin_settings) {
                    Toast.makeText(AdminDashboardActivity.this, "Settings (dummy)", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_admin_logout) {
                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        bottomNav.setSelectedItemId(R.id.nav_admin_overview);

        loadSummaryStats();
        loadRequestedChart();
        loadOfferedChart();
        loadTopSkills();
    }

    private void loadSummaryStats() {
        tvTotalPosts.setText(String.valueOf(db.getTotalOpenPosts()));
        tvTotalRequests.setText(String.valueOf(db.getTotalRequests()));
        tvTotalUsers.setText(String.valueOf(db.getTotalUsers()));
    }

    private void loadRequestedChart() {
        Cursor cursor = db.getSkillRequestedStats();
        setChartDataFromCursor(cursor, chartRequested, true);
    }

    private void loadOfferedChart() {
        Cursor cursor = db.getSkillOfferedStats();
        setChartDataFromCursor(cursor, chartOffered, false);
    }

    private void loadTopSkills() {
        containerTopRequested.removeAllViews();
        containerTopOffered.removeAllViews();

        Cursor cReq = db.getTopRequestedSkills(3);
        if (cReq != null) {
            while (cReq.moveToNext()) {
                String skill = cReq.getString(0);
                int count = cReq.getInt(1);
                TextView tv = new TextView(this);
                tv.setText("• " + skill + " (" + count + ")");
                tv.setTextSize(12f);
                tv.setTextColor(0xFF000000);
                containerTopRequested.addView(tv);
            }
            cReq.close();
        }

        Cursor cOff = db.getTopOfferedSkills(3);
        if (cOff != null) {
            while (cOff.moveToNext()) {
                String skill = cOff.getString(0);
                int count = cOff.getInt(1);
                TextView tv = new TextView(this);
                tv.setText("• " + skill + " (" + count + ")");
                tv.setTextSize(12f);
                tv.setTextColor(0xFF000000);
                containerTopOffered.addView(tv);
            }
            cOff.close();
        }
    }

    private void setChartDataFromCursor(Cursor cursor, AdminDonutChartView chartView, boolean requested) {
        if (cursor == null) {
            chartView.setSegments(null);
            return;
        }

        Map<String, Integer> counts = new HashMap<>();
        int total = 0;
        while (cursor.moveToNext()) {
            String skill = cursor.getString(0);
            int count = cursor.getInt(1);
            if (skill == null || skill.trim().isEmpty()) {
                skill = requested ? "Unknown Request" : "Unknown Skill";
            }
            skill = skill.trim();
            int existing = counts.containsKey(skill) ? counts.get(skill) : 0;
            counts.put(skill, existing + count);
            total += count;
        }
        cursor.close();

        if (total == 0 || counts.isEmpty()) {
            chartView.setSegments(null);
            return;
        }

        List<String> mainLabels = new ArrayList<>();
        List<Float> mainPercents = new ArrayList<>();
        float otherCount = 0f;

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            float pct = (float) entry.getValue() / (float) total;
            if (pct >= 0.10f) {
                mainLabels.add(entry.getKey());
                mainPercents.add(pct);
            } else {
                otherCount += entry.getValue();
            }
        }

        float otherPct = otherCount / (float) total;
        if (otherPct > 0f) {
            mainLabels.add("Other");
            mainPercents.add(otherPct);
        }

        float sum = 0f;
        for (float p : mainPercents) sum += p;
        if (sum <= 0f) {
            chartView.setSegments(null);
            return;
        }

        List<AdminDonutChartView.DonutSegment> segments = new ArrayList<>();
        for (int i = 0; i < mainLabels.size(); i++) {
            String label = mainLabels.get(i);
            float normalized = mainPercents.get(i) / sum;
            int color = SEGMENT_COLORS[i % SEGMENT_COLORS.length];
            segments.add(new AdminDonutChartView.DonutSegment(label, normalized, color));
        }

        chartView.setSegments(segments);
    }
}
