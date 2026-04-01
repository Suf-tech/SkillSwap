package com.example.skillswap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOverviewFragment extends Fragment {

    private DatabaseHelper db;
    private AdminDonutChartView chartRequested;
    private AdminDonutChartView chartOffered;

    private TextView tvTotalPosts;
    private TextView tvTotalRequests;
    private TextView tvTotalUsers;
    private LinearLayout containerTopRequested;
    private LinearLayout containerTopOffered;
    private LinearLayout containerTopActiveUsers;

    private final int[] SEGMENT_COLORS = new int[]{
            0xFF1976D2,
            0xFF388E3C,
            0xFFFBC02D,
            0xFFE64A19,
            0xFF7B1FA2,
            0xFF0097A7
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        tvTotalPosts = view.findViewById(R.id.tvTotalPosts);
        tvTotalRequests = view.findViewById(R.id.tvTotalRequests);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        containerTopRequested = view.findViewById(R.id.containerTopRequested);
        containerTopOffered = view.findViewById(R.id.containerTopOffered);
        containerTopActiveUsers = view.findViewById(R.id.containerTopActiveUsers);

        chartRequested = view.findViewById(R.id.chartRequested);
        chartOffered = view.findViewById(R.id.chartOffered);

        loadSummaryStats();
        loadRequestedChart();
        loadOfferedChart();
        loadTopSkills();
        loadTopActiveUsers();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Admin Dashboard");
            }
        }
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
                TextView tv = new TextView(requireContext());
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
                TextView tv = new TextView(requireContext());
                tv.setText("• " + skill + " (" + count + ")");
                tv.setTextSize(12f);
                tv.setTextColor(0xFF000000);
                containerTopOffered.addView(tv);
            }
            cOff.close();
        }
    }

    private void loadTopActiveUsers() {
        if (containerTopActiveUsers == null) return;
        containerTopActiveUsers.removeAllViews();

        Cursor c = db.getTopActiveUsers(5);
        if (c == null) return;

        int rank = 1;
        while (c.moveToNext()) {
            String email = c.getString(0);
            String name = c.getString(1);
            int posts = c.getInt(2);
            int requests = c.getInt(3);
            int total = c.getInt(4);

            LinearLayout row = new LinearLayout(requireContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            int padding = (int) (requireContext().getResources().getDisplayMetrics().density * 8);
            row.setPadding(0, padding, 0, padding);

            TextView badge = new TextView(requireContext());
            badge.setText(String.valueOf(rank));
            badge.setTextSize(12f);
            badge.setTextColor(0xFFFFFFFF);
            badge.setBackgroundResource(R.drawable.editbox_background);
            badge.setPadding(padding, padding / 2, padding, padding / 2);

            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            badgeParams.rightMargin = padding;
            row.addView(badge, badgeParams);

            ImageView avatar = new ImageView(requireContext());
            int avatarId = db.getAvatarId(email);
            setAvatar(avatar, avatarId);
            int size = (int) (requireContext().getResources().getDisplayMetrics().density * 40);
            LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(size, size);
            avatarParams.rightMargin = padding;
            row.addView(avatar, avatarParams);

            LinearLayout texts = new LinearLayout(requireContext());
            texts.setOrientation(LinearLayout.VERTICAL);

            TextView tvName = new TextView(requireContext());
            tvName.setText(name != null && !name.isEmpty() ? name : email);
            tvName.setTextSize(14f);
            tvName.setTextColor(0xFF000000);

            TextView tvStats = new TextView(requireContext());
            tvStats.setText("Posts: " + posts + " · Requests: " + requests + " · Total: " + total);
            tvStats.setTextSize(12f);
            tvStats.setTextColor(0xFF757575);

            texts.addView(tvName);
            texts.addView(tvStats);

            row.addView(texts, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f));

            row.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), UserDetailActivity.class);
                intent.putExtra("user_email", email);
                intent.putExtra("user_name", name);
                intent.putExtra("avatar_id", avatarId);
                startActivity(intent);
            });

            containerTopActiveUsers.addView(row);
            rank++;
        }
        c.close();
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

