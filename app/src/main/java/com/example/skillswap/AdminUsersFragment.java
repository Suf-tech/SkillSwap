package com.example.skillswap;

import android.content.Intent;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersFragment extends Fragment {

    private DatabaseReference mDatabase;
    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter adapter;
    private List<AdminUser> allUsers = new ArrayList<>();
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

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        EditText searchEdit = view.findViewById(R.id.adminUserSearch);

        usersRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        adapter = new AdminUsersAdapter(requireContext(), new ArrayList<>(), new AdminUsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(AdminUser user) {
                Intent intent = new Intent(requireContext(), UserDetailActivity.class);
                intent.putExtra("user_id", user.uid); // Ab 'uid' error nahi dega
                intent.putExtra("user_email", user.email);
                intent.putExtra("user_name", user.name);
                intent.putExtra("avatar_id", user.avatarId);
                startActivity(intent);
            }

            @Override
            public void onUserEditClick(AdminUser user) {
                Intent intent = new Intent(requireContext(), AdminEditUserActivity.class);
                intent.putExtra("user_id", user.uid); // Ab 'uid' error nahi dega
                intent.putExtra("user_email", user.email);
                startActivity(intent);
            }
        });
        usersRecyclerView.setAdapter(adapter);

        fetchUsersFromFirebase();

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void fetchUsersFromFirebase() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                allUsers.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    String uid = userSnap.getKey(); // Firebase key as UID
                    String email = userSnap.child("email").getValue(String.class);
                    String name = userSnap.child("name").getValue(String.class);

                    Object avObj = userSnap.child("avatarId").getValue();
                    int avatarId = (avObj instanceof Long) ? ((Long) avObj).intValue() : (avObj instanceof Integer ? (Integer) avObj : 0);

                    if (email != null) {
                        // Matching with updated 4-argument constructor
                        allUsers.add(new AdminUser(uid, email, name, avatarId));
                    }
                }
                adapter.updateList(new ArrayList<>(allUsers));
                updateUIState(allUsers.isEmpty(), "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void filterUsers(String query) {
        String lower = query.trim().toLowerCase();
        List<AdminUser> filtered = new ArrayList<>();
        for (AdminUser user : allUsers) {
            if ((user.name != null && user.name.toLowerCase().contains(lower)) ||
                    (user.email != null && user.email.toLowerCase().contains(lower))) {
                filtered.add(user);
            }
        }
        adapter.updateList(filtered);
        updateUIState(filtered.isEmpty(), query);
    }

    private void updateUIState(boolean isEmpty, String query) {
        if (isEmpty) {
            usersRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
            emptyStateText.setText(query.isEmpty() ? "No users registered yet." : "No users matching '" + query + "'");
        } else {
            usersRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("User Management");
            }
        }
    }
}