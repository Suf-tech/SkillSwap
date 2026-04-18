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

import java.util.Locale;

public class ProfileFragment extends Fragment {

    // XML IDs matching fragment_profile.xml
    private ImageView profileAvatar;
    private TextView profileName, profileEmail, profileRating;
    private LinearLayout btnPersonalInfo, btnIdentity, btnTheme;

    private DatabaseReference mUserRef;
    private DatabaseReference mRatingsRef;
    private ValueEventListener userListener;
    private ValueEventListener ratingsListener;
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
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
            mRatingsRef = FirebaseDatabase.getInstance().getReference().child("Ratings").child(currentUserId);
            loadUserDataFromFirebase();
            loadUserRatingsFromFirebase();
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
        if (mUserRef == null || userListener != null) return;

        // addValueEventListener use kiya hai taake PersonalInfo se change hote hi yahan khud update ho jaye
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    userName = snapshot.child("name").getValue(String.class);
                    userEmail = snapshot.child("email").getValue(String.class);

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
                    setAvatar(avatarId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mUserRef.addValueEventListener(userListener);
    }

    private void loadUserRatingsFromFirebase() {
        if (mRatingsRef == null || ratingsListener != null) return;

        ratingsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                if (!snapshot.exists()) {
                    profileRating.setText(getString(R.string.profile_no_ratings));
                    return;
                }

                double total = 0;
                int count = 0;
                for (DataSnapshot ratingSnap : snapshot.getChildren()) {
                    Double ratingValue = parseRatingValue(ratingSnap.child("rating").getValue());
                    if (ratingValue != null) {
                        total += ratingValue;
                        count++;
                    }
                }

                if (count == 0) {
                    profileRating.setText(getString(R.string.profile_no_ratings));
                    return;
                }

                double average = total / count;
                profileRating.setText(String.format(Locale.US, "%.1f ★", average));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Ratings Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRatingsRef.addValueEventListener(ratingsListener);
    }

    private Double parseRatingValue(Object rawValue) {
        if (rawValue == null) return null;

        double parsed;
        if (rawValue instanceof Long) {
            parsed = ((Long) rawValue).doubleValue();
        } else if (rawValue instanceof Integer) {
            parsed = ((Integer) rawValue).doubleValue();
        } else if (rawValue instanceof Double) {
            parsed = (Double) rawValue;
        } else if (rawValue instanceof Float) {
            parsed = ((Float) rawValue).doubleValue();
        } else if (rawValue instanceof String) {
            try {
                parsed = Double.parseDouble(((String) rawValue).trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        } else {
            return null;
        }

        if (Double.isNaN(parsed) || Double.isInfinite(parsed) || parsed < 0 || parsed > 5) {
            return null;
        }

        return parsed;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mUserRef != null && userListener != null) {
            mUserRef.removeEventListener(userListener);
            userListener = null;
        }

        if (mRatingsRef != null && ratingsListener != null) {
            mRatingsRef.removeEventListener(ratingsListener);
            ratingsListener = null;
        }
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