package com.example.skillswap;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    // XML IDs matching fragment_profile.xml
    private ImageView profileAvatar;
    private TextView profileName, profileEmail, profileRating;
    private LinearLayout btnPersonalInfo, btnIdentity, btnTheme;

    private DatabaseReference mDatabase;
    private String currentUserId, userName, userEmail;
    private int avatarId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // XML name check: fragment_profile
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 1. Session Retrieval
        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");

        // 2. View Bindings
        profileAvatar = view.findViewById(R.id.profileAvatar);
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileRating = view.findViewById(R.id.profileRating);
        btnPersonalInfo = view.findViewById(R.id.btnPersonalInfo);
        btnIdentity = view.findViewById(R.id.btnIdentity);
        btnTheme = view.findViewById(R.id.btnTheme);

        if (!currentUserId.isEmpty()) {
            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            loadUserDataFromFirebase();
        }

        // 3. Listeners
        btnTheme.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Standard Blue theme is active.", Toast.LENGTH_SHORT).show());

        btnPersonalInfo.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PersonalInfoActivity.class)));

        btnIdentity.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), IdentityActivity.class);
            intent.putExtra("user_id", currentUserId);
            intent.putExtra("user_email", userEmail);
            intent.putExtra("user_name", userName);
            intent.putExtra("avatar_id", avatarId);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserDataFromFirebase() {
        // addValueEventListener use kiya hai taake PersonalInfo se change hote hi yahan khud update ho jaye
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    userName = snapshot.child("name").getValue(String.class);
                    userEmail = snapshot.child("email").getValue(String.class);

                    // Rating default 5.0 set ki hai
                    String rating = snapshot.hasChild("rating") ? snapshot.child("rating").getValue(String.class) : "5.0 ★";

                    // Safe Integer Casting (Long to Int handled)
                    if (snapshot.hasChild("avatarId")) {
                        Object avObj = snapshot.child("avatarId").getValue();
                        if (avObj instanceof Long) {
                            avatarId = ((Long) avObj).intValue();
                        } else if (avObj instanceof Integer) {
                            avatarId = (Integer) avObj;
                        }
                    }

                    // Set UI
                    profileName.setText(userName != null ? userName : "SkillSwap User");
                    profileEmail.setText(userEmail != null ? userEmail : "");
                    profileRating.setText(rating);
                    setAvatar(avatarId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background; // Fallback background
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        profileAvatar.setImageResource(res);
    }
}