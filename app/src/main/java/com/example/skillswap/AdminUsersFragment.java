package com.example.skillswap;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private DatabaseHelper db;
    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter adapter;
    private List<AdminUser> allUsers;
    private View emptyStateLayout;
    private TextView emptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = new DatabaseHelper(requireContext());

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        
        usersRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        allUsers = loadUsers();
        adapter = new AdminUsersAdapter(requireContext(), new ArrayList<>(allUsers), new AdminUsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(AdminUser user) {
                Intent intent = new Intent(requireContext(), UserDetailActivity.class);
                intent.putExtra("user_email", user.email);
                intent.putExtra("user_name", user.name);
                intent.putExtra("avatar_id", user.avatarId);
                startActivity(intent);
            }

            @Override
            public void onUserEditClick(AdminUser user) {
                Intent intent = new Intent(requireContext(), AdminEditUserActivity.class);
                intent.putExtra("user_email", user.email);
                startActivity(intent);
            }
        });
        usersRecyclerView.setAdapter(adapter);

        EditText search = view.findViewById(R.id.adminUserSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
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
                activity.getSupportActionBar().setTitle("Users");
            }
        }
    }

    private List<AdminUser> loadUsers() {
        List<AdminUser> list = new ArrayList<>();
        Cursor cursor = db.getAllUsers();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String email = cursor.getString(0);
                String name = cursor.getString(1);
                int avatarId = cursor.getInt(2);
                list.add(new AdminUser(email, name, avatarId));
            }
            cursor.close();
        }
        return list;
    }

    private void filterUsers(String query) {
        String lower = query.trim().toLowerCase();
        if (lower.isEmpty()) {
            adapter.updateList(new ArrayList<>(allUsers));
            usersRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
            return;
        }

        List<AdminUser> filtered = new ArrayList<>();
        for (AdminUser user : allUsers) {
            String name = user.name != null ? user.name.toLowerCase() : "";
            String email = user.email != null ? user.email.toLowerCase() : "";
            if (name.contains(lower) || email.contains(lower)) {
                filtered.add(user);
            }
        }
        
        adapter.updateList(filtered);

        if (filtered.isEmpty()) {
            usersRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            emptyStateText.setText(getString(R.string.admin_no_users_found, query));
        } else {
            usersRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
}
