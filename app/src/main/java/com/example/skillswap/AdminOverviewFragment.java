package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminOverviewFragment extends Fragment {

    // XML IDs: chartRequested, chartOffered, legendRequested, legendOffered, tvTotalPosts, tvTotalRequests, tvTotalUsers
    private DatabaseReference mDatabase;
    private AdminDonutChartView chartRequested, chartOffered;
    private LinearLayout legendRequested, legendOffered;

    private TextView tvTotalPosts, tvTotalRequests, tvTotalUsers;
    private LinearLayout containerTopRequested, containerTopOffered, containerTopActiveUsers;

    private final int[] SEGMENT_COLORS = new int[]{
            0xFF1976D2, 0xFF388E3C, 0xFFFBC02D, 0xFFE64A19, 0xFF7B1FA2, 0xFF0097A7
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_admin_overview.xml se inflate karein
        return inflater.inflate(R.layout.fragment_admin_overview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Bindings
        tvTotalPosts = view.findViewById(R.id.tvTotalPosts);
        tvTotalRequests = view.findViewById(R.id.tvTotalRequests);
        tvTotalUsers = view.findViewById(R.id.tvTotalUsers);
        containerTopRequested = view.findViewById(R.id.containerTopRequested);
        containerTopOffered = view.findViewById(R.id.containerTopOffered);
        containerTopActiveUsers = view.findViewById(R.id.containerTopActiveUsers);

        chartRequested = view.findViewById(R.id.chartRequested);
        chartOffered = view.findViewById(R.id.chartOffered);
        legendRequested = view.findViewById(R.id.legendRequested);
        legendOffered = view.findViewById(R.id.legendOffered);

        fetchData();
    }

    private void fetchData() {
        // 1. Users Count & List
        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                tvTotalUsers.setText(String.valueOf(snapshot.getChildrenCount()));
                loadTopActiveUsers(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 2. Posts Stats
        mDatabase.child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                tvTotalPosts.setText(String.valueOf(snapshot.getChildrenCount()));
                processSkillStats(snapshot);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        // 3. Requests Count
        mDatabase.child("Requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;
                tvTotalRequests.setText(String.valueOf(snapshot.getChildrenCount()));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void processSkillStats(DataSnapshot postsSnapshot) {
        Map<String, Integer> requestedMap = new HashMap<>();
        Map<String, Integer> offeredMap = new HashMap<>();

        for (DataSnapshot post : postsSnapshot.getChildren()) {
            String have = post.child("have").getValue(String.class);
            String want = post.child("want").getValue(String.class);

            if (have != null && !have.isEmpty()) offeredMap.put(have, offeredMap.getOrDefault(have, 0) + 1);
            if (want != null && !want.isEmpty()) requestedMap.put(want, requestedMap.getOrDefault(want, 0) + 1);
        }

        updateChart(requestedMap, chartRequested, legendRequested);
        updateChart(offeredMap, chartOffered, legendOffered);
        displayTopSkills(requestedMap, containerTopRequested);
        displayTopSkills(offeredMap, containerTopOffered);
    }

    private void updateChart(Map<String, Integer> dataMap, AdminDonutChartView chartView, LinearLayout legend) {
        int total = 0;
        for (int count : dataMap.values()) total += count;

        legend.removeAllViews();
        if (total == 0) return;

        List<AdminDonutChartView.DonutSegment> segments = new ArrayList<>();

        // Sorting and limiting to top 6 for chart clarity
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(dataMap.entrySet());
        Collections.sort(sortedList, (a, b) -> b.getValue().compareTo(a.getValue()));

        int i = 0;
        for (Map.Entry<String, Integer> entry : sortedList) {
            float pct = (float) entry.getValue() / total;
            int color = SEGMENT_COLORS[i % SEGMENT_COLORS.length];
            segments.add(new AdminDonutChartView.DonutSegment(entry.getKey(), pct, color));
            addLegendItem(legend, entry.getKey(), pct, color);
            i++;
            if (i >= 6) break;
        }
        chartView.setSegments(segments);
    }

    private void displayTopSkills(Map<String, Integer> dataMap, LinearLayout container) {
        container.removeAllViews();
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(dataMap.entrySet());
        Collections.sort(sortedList, (a, b) -> b.getValue().compareTo(a.getValue()));

        int limit = Math.min(sortedList.size(), 3);
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Integer> entry = sortedList.get(i);
            TextView tv = new TextView(requireContext());
            tv.setText("• " + entry.getKey() + " (" + entry.getValue() + ")");
            tv.setTextColor(0xFF000000);
            tv.setPadding(0, 4, 0, 4);
            container.addView(tv);
        }
    }

    private void loadTopActiveUsers(DataSnapshot usersSnapshot) {
        if (!isAdded()) return;
        containerTopActiveUsers.removeAllViews();

        int count = 0;
        for (DataSnapshot userSnap : usersSnapshot.getChildren()) {
            if (count >= 5) break;

            String name = userSnap.child("name").getValue(String.class);
            String email = userSnap.child("email").getValue(String.class);
            String userId = userSnap.getKey();

            // Safe unboxing for avatarId
            Object avObj = userSnap.child("avatarId").getValue();
            int avatarId = (avObj instanceof Long) ? ((Long) avObj).intValue() : (avObj instanceof Integer ? (Integer) avObj : 0);

            View row = LayoutInflater.from(requireContext()).inflate(R.layout.item_admin_user, containerTopActiveUsers, false);

            ((TextView) row.findViewById(R.id.adminUserName)).setText(name != null ? name : "SkillSwap User");
            ((TextView) row.findViewById(R.id.adminUserEmail)).setText(email != null ? email : "No Email");
            setAvatar((ImageView) row.findViewById(R.id.adminUserAvatar), avatarId);

            row.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), UserDetailActivity.class);
                intent.putExtra("user_id", userId);
                intent.putExtra("user_email", email);
                intent.putExtra("user_name", name);
                intent.putExtra("avatar_id", avatarId);
                startActivity(intent);
            });

            containerTopActiveUsers.addView(row);
            count++;
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

    private void addLegendItem(LinearLayout container, String label, float percentage, int color) {
        if (!isAdded()) return;
        TextView tv = new TextView(requireContext());
        tv.setText(String.format(Locale.getDefault(), "%s (%.0f%%)", label, percentage * 100));
        tv.setTextColor(color);
        tv.setTextSize(12);
        container.addView(tv);
    }
}