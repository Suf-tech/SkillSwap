package com.example.skillswap;

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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HomeFragment extends Fragment {

    RecyclerView recyclerView;
    EditText searchBar;
    ArrayList<Skill> skillList;
    SkillAdapter adapter;
    DatabaseHelper db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = new DatabaseHelper(requireContext());
        searchBar = view.findViewById(R.id.searchBar);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        loadPosts();

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void filterList(String text) {
        ArrayList<Skill> filteredList = new ArrayList<>();
        for (Skill item : skillList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            } else {
                Cursor cursor = db.getPostById(item.getId());
                if(cursor != null && cursor.moveToFirst()){
                    String want = cursor.getString(4);
                    if (want.toLowerCase().contains(text.toLowerCase())) {
                        filteredList.add(item);
                    }
                    cursor.close();
                }
            }
        }

        if (adapter != null) {
            adapter.setFilteredList(filteredList);
        }
    }

    private void loadPosts() {
        skillList = new ArrayList<>();
        // FIX: Only fetch posts that haven't been accepted yet
        Cursor cursor = db.getAllOpenPosts();

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

        adapter = new SkillAdapter(requireContext(), skillList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPosts();
        if(searchBar != null) searchBar.setText("");
    }
}