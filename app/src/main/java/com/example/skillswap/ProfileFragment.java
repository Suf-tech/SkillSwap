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

public class ProfileFragment extends Fragment {

    ImageView profileAvatar;
    TextView profileName, profileEmail, profileRating;
    LinearLayout btnPersonalInfo, btnIdentity, btnTheme;
    DatabaseHelper db;
    String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = new DatabaseHelper(requireContext());
        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userEmail = sp.getString("user_email", "");

        profileAvatar = view.findViewById(R.id.profileAvatar);
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileRating = view.findViewById(R.id.profileRating);
        btnTheme = view.findViewById(R.id.btnTheme);

        loadUserData();

        btnTheme.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Standard theme is active.", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnPersonalInfo).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), PersonalInfoActivity.class)));

        view.findViewById(R.id.btnIdentity).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), IdentityActivity.class);
            intent.putExtra("user_email", userEmail);
            startActivity(intent);
        });

        return view;
    }

    private void loadUserData() {
        profileName.setText(db.getUserName(userEmail));
        profileEmail.setText(userEmail);
        setAvatar(db.getAvatarId(userEmail));

        // FIXED: The missing line to actually fetch and display the rating!
        profileRating.setText(db.getUserRating(userEmail));
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        profileAvatar.setImageResource(res);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}