package com.example.skillswap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminSkillsFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView skillsRecyclerView;
    private AdminSkillsAdapter adapter;
    private List<AdminSkillItem> allSkills;
    
    private View emptyState;
    private View skeletonLoading;
    private EditText searchEditText;
    private ImageView btnClearSearch;
    private ChipGroup categoryChips;

    private String currentSearchQuery = "";
    private String currentCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_skills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        // Initialize Views
        skillsRecyclerView = view.findViewById(R.id.adminSkillsRecyclerView);
        emptyState = view.findViewById(R.id.emptyState);
        skeletonLoading = view.findViewById(R.id.skeletonLoading);
        searchEditText = view.findViewById(R.id.adminSkillSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        categoryChips = view.findViewById(R.id.categoryChips);
        ImageView btnFilter = view.findViewById(R.id.btnFilter);

        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initial Data Load with Skeleton Simulation
        showLoading(true);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            allSkills = loadSkills();
            adapter = new AdminSkillsAdapter(allSkills, skill -> {
                Intent intent = new Intent(requireContext(), AdminSkillDetailActivity.class);
                intent.putExtra("skill_name", skill.getName());
                startActivity(intent);
            });
            skillsRecyclerView.setAdapter(adapter);
            showLoading(false);
            updateUIState();
        }, 800);

        // Search Logic
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));

        // Category Chip Logic
        categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                Chip chip = group.findViewById(checkedIds.get(0));
                currentCategory = chip.getText().toString();
            }
            applyFilters();
        });

        // Simple Filter/Sort Action
        btnFilter.setOnClickListener(v -> {
            // Reverse current list as a simple "Sort" demonstration
            List<AdminSkillItem> currentList = new ArrayList<>(allSkills);
            java.util.Collections.reverse(currentList);
            allSkills = currentList;
            applyFilters();
            android.widget.Toast.makeText(getContext(), "List sorted", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void showLoading(boolean loading) {
        skeletonLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        skillsRecyclerView.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (loading) emptyState.setVisibility(View.GONE);
    }

    private void applyFilters() {
        if (allSkills == null) return;

        List<AdminSkillItem> filtered = new ArrayList<>();
        String queryLower = currentSearchQuery.toLowerCase(Locale.ROOT).trim();

        for (AdminSkillItem item : allSkills) {
            boolean matchesQuery = item.getName().toLowerCase(Locale.ROOT).contains(queryLower);
            boolean matchesCategory = currentCategory.equals("All") || 
                                     item.getName().toLowerCase(Locale.ROOT).contains(currentCategory.toLowerCase(Locale.ROOT));
            
            if (matchesQuery && matchesCategory) {
                filtered.add(item);
            }
        }

        adapter.updateList(filtered);
        updateUIState();
    }

    private void updateUIState() {
        if (adapter == null) return;
        
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            skillsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            skillsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Skills");
            }
        }
    }

    private List<AdminSkillItem> loadSkills() {
        Map<String, SkillAggregate> map = new HashMap<>();

        Cursor postCursor = db.getReadableDatabase().rawQuery("SELECT skill_have, skill_want FROM posts", null);
        if (postCursor != null) {
            while (postCursor.moveToNext()) {
                String have = postCursor.getString(0);
                String want = postCursor.getString(1);
                if (have != null && !have.trim().isEmpty()) addSkill(map, have, false, false, true);
                if (want != null && !want.trim().isEmpty()) addSkill(map, want, false, true, false);
            }
            postCursor.close();
        }

        Cursor reqCursor = db.getReadableDatabase().rawQuery("SELECT skill_offered, skill_required FROM requests", null);
        if (reqCursor != null) {
            while (reqCursor.moveToNext()) {
                String offered = reqCursor.getString(0);
                String required = reqCursor.getString(1);
                if (offered != null && !offered.trim().isEmpty()) addSkill(map, offered, false, false, true);
                if (required != null && !required.trim().isEmpty()) addSkill(map, required, true, false, false);
            }
            reqCursor.close();
        }

        List<AdminSkillItem> list = new ArrayList<>();
        for (SkillAggregate agg : map.values()) {
            list.add(new AdminSkillItem(agg.displayName, agg.count, agg.requested, agg.wanted, agg.offered));
        }

        list.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        return list;
    }

    private void addSkill(Map<String, SkillAggregate> map, String rawName, boolean requested, boolean wanted, boolean offered) {
        String key = rawName.trim().toLowerCase(Locale.ROOT);
        SkillAggregate agg = map.get(key);
        if (agg == null) {
            agg = new SkillAggregate(rawName.trim());
            map.put(key, agg);
        }
        agg.count++;
        if (requested) agg.requested = true;
        if (wanted) agg.wanted = true;
        if (offered) agg.offered = true;
    }

    private static class SkillAggregate {
        final String displayName;
        int count;
        boolean requested;
        boolean wanted;
        boolean offered;
        SkillAggregate(String displayName) { this.displayName = displayName; }
    }
}
