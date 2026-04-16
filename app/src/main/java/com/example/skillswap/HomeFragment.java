package com.example.skillswap;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText searchBar;
    private LinearLayout emptyStateLayout;
    private TextView emptyStateText;
    private ArrayList<Skill> skillList;
    private SkillAdapter adapter;
    private DatabaseReference mDatabase;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        searchBar = view.findViewById(R.id.searchBar);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        skillList = new ArrayList<>();
        adapter = new SkillAdapter(requireContext(), skillList);
        recyclerView.setAdapter(adapter);

        loadPostsFromFirebase();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void loadPostsFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                skillList.clear();
                for (DataSnapshot postSnap : snapshot.getChildren()) {
                    try {
                        Skill skill = postSnap.getValue(Skill.class);

                        if (skill != null) {
                            // --- CLEAN FILTER LOGIC ---
                            boolean isOpen = true;
                            if (postSnap.hasChild("isOpen")) {
                                Object openObj = postSnap.child("isOpen").getValue();
                                if (openObj instanceof Boolean) {
                                    isOpen = (Boolean) openObj;
                                }
                            }

                            // Only add if post is OPEN and teacher name is valid
                            if (isOpen && skill.getTeacher() != null && !skill.getTeacher().equals("Unknown User")) {
                                skillList.add(skill);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseError", "Data parsing error: " + e.getMessage());
                    }
                }
                adapter.notifyDataSetChanged();
                updateUIState(skillList.isEmpty(), "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }

    private void filterList(String text) {
        ArrayList<Skill> filteredList = new ArrayList<>();
        String query = text.toLowerCase().trim();

        for (Skill item : skillList) {
            String have = item.getHave() != null ? item.getHave().toLowerCase() : "";
            String want = item.getWant() != null ? item.getWant().toLowerCase() : "";
            String name = item.getTeacher() != null ? item.getTeacher().toLowerCase() : "";

            if (have.contains(query) || want.contains(query) || name.contains(query)) {
                filteredList.add(item);
            }
        }

        adapter.setFilteredList(filteredList);
        updateUIState(filteredList.isEmpty(), query);
    }

    private void updateUIState(boolean isEmpty, String query) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            if (!query.isEmpty()) {
                emptyStateText.setText("No results for '" + query + "'");
            } else {
                emptyStateText.setText("No active swaps at the moment.");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
}