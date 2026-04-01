package com.example.skillswap;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_skills, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        skillsRecyclerView = view.findViewById(R.id.adminSkillsRecyclerView);
        skillsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        allSkills = loadSkills();
        adapter = new AdminSkillsAdapter(allSkills, skill -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(skill.getName());
            builder.setMessage("More details coming soon.");
            builder.setPositiveButton("OK", null);
            builder.show();
        });
        skillsRecyclerView.setAdapter(adapter);

        EditText search = view.findViewById(R.id.adminSkillSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSkills(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
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

        // Posts: skill_have (offered) and skill_want (wanted)
        Cursor postCursor = db.getReadableDatabase().rawQuery("SELECT skill_have, skill_want FROM posts", null);
        if (postCursor != null) {
            while (postCursor.moveToNext()) {
                String have = postCursor.getString(0);
                String want = postCursor.getString(1);

                if (have != null && !have.trim().isEmpty()) {
                    addSkill(map, have, false, false, true);
                }
                if (want != null && !want.trim().isEmpty()) {
                    addSkill(map, want, false, true, false);
                }
            }
            postCursor.close();
        }

        // Requests: skill_offered (offered) and skill_required (requested)
        Cursor reqCursor = db.getReadableDatabase().rawQuery("SELECT skill_offered, skill_required FROM requests", null);
        if (reqCursor != null) {
            while (reqCursor.moveToNext()) {
                String offered = reqCursor.getString(0);
                String required = reqCursor.getString(1);

                if (offered != null && !offered.trim().isEmpty()) {
                    addSkill(map, offered, false, false, true);
                }
                if (required != null && !required.trim().isEmpty()) {
                    addSkill(map, required, true, false, false);
                }
            }
            reqCursor.close();
        }

        List<AdminSkillItem> list = new ArrayList<>();
        for (SkillAggregate agg : map.values()) {
            list.add(new AdminSkillItem(agg.displayName, agg.count, agg.requested, agg.wanted, agg.offered));
        }

        list.sort((a, b) -> {
            if (b.getCount() != a.getCount()) {
                return Integer.compare(b.getCount(), a.getCount());
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });

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

    private void filterSkills(String query) {
        String lower = query.trim().toLowerCase(Locale.ROOT);
        if (lower.isEmpty()) {
            adapter.updateList(new ArrayList<>(allSkills));
            return;
        }

        List<AdminSkillItem> filtered = new ArrayList<>();
        for (AdminSkillItem item : allSkills) {
            if (item.getName() != null && item.getName().toLowerCase(Locale.ROOT).contains(lower)) {
                filtered.add(item);
            }
        }
        adapter.updateList(filtered);
    }

    private static class SkillAggregate {
        final String displayName;
        int count;
        boolean requested;
        boolean wanted;
        boolean offered;

        SkillAggregate(String displayName) {
            this.displayName = displayName;
        }
    }
}

