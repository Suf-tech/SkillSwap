package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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

public class AdminSkillsFragment extends Fragment {

    // XML IDs: adminSkillsRecyclerView, emptyState, skeletonLoading, adminSkillSearch, btnClearSearch, categoryChips
    private DatabaseReference mDatabase;
    private RecyclerView skillsRecyclerView;
    private AdminSkillsAdapter adapter;
    private List<AdminSkillItem> allSkills = new ArrayList<>();

    private View emptyState, skeletonLoading;
    private EditText searchEditText;
    private ImageView btnClearSearch;
    private ChipGroup categoryChips;

    private String currentSearchQuery = "";
    private String currentCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_admin_skills.xml inflate karein
        return inflater.inflate(R.layout.fragment_admin_skills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Bindings
        skillsRecyclerView = view.findViewById(R.id.adminSkillsRecyclerView);
        emptyState = view.findViewById(R.id.emptyState);
        skeletonLoading = view.findViewById(R.id.skeletonLoading);
        searchEditText = view.findViewById(R.id.adminSkillSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        categoryChips = view.findViewById(R.id.categoryChips);

        // RecyclerView Setup
        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminSkillsAdapter(allSkills, skill -> {
            Intent intent = new Intent(requireContext(), AdminSkillDetailActivity.class);
            intent.putExtra("skill_name", skill.getName());
            startActivity(intent);
        });
        skillsRecyclerView.setAdapter(adapter);

        // Load Data
        fetchSkillsFromFirebase();

        // 1. Search Logic
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                btnClearSearch.setVisibility(currentSearchQuery.isEmpty() ? View.GONE : View.VISIBLE);
                applyFilters();
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));

        // 2. Chip Filter Logic (Standard Selection)
        categoryChips.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == View.NO_ID) {
                currentCategory = "All";
            } else {
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    currentCategory = chip.getText().toString();
                }
            }
            applyFilters();
        });
    }

    private void fetchSkillsFromFirebase() {
        showLoading(true);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                Map<String, SkillAggregate> map = new HashMap<>();

                // Process Posts
                DataSnapshot posts = snapshot.child("Posts");
                for (DataSnapshot post : posts.getChildren()) {
                    String have = post.child("have").getValue(String.class);
                    String want = post.child("want").getValue(String.class);
                    if (have != null) addSkill(map, have, false, false, true);
                    if (want != null) addSkill(map, want, false, true, false);
                }

                // Process Requests
                DataSnapshot requests = snapshot.child("Requests");
                for (DataSnapshot req : requests.getChildren()) {
                    // Mapping current request skill field
                    String reqSkill = req.child("required").getValue(String.class);
                    if (reqSkill != null) addSkill(map, reqSkill, true, false, false);
                }

                allSkills.clear();
                for (SkillAggregate agg : map.values()) {
                    allSkills.add(new AdminSkillItem(agg.displayName, agg.count, agg.requested, agg.wanted, agg.offered));
                }

                // Stable Sort for all versions
                Collections.sort(allSkills, (a, b) -> Integer.compare(b.getCount(), a.getCount()));

                applyFilters();
                showLoading(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) showLoading(false);
            }
        });
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

    private void applyFilters() {
        List<AdminSkillItem> filtered = new ArrayList<>();
        for (AdminSkillItem item : allSkills) {
            boolean matchesSearch = item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase());
            boolean matchesCategory = currentCategory.equals("All") ||
                    item.getName().toLowerCase().contains(currentCategory.toLowerCase());

            if (matchesSearch && matchesCategory) {
                filtered.add(item);
            }
        }
        adapter.updateList(filtered);
        emptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        skillsRecyclerView.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean loading) {
        skeletonLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            skillsRecyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
        }
    }

    private static class SkillAggregate {
        final String displayName;
        int count;
        boolean requested, wanted, offered;
        SkillAggregate(String displayName) { this.displayName = displayName; }
    }
}