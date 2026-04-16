package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddPostFragment extends Fragment {

    // XML IDs: postHave, postWant, postMsg, submitPostBtn, myProfilePic
    private EditText have, want, msg;
    private ImageView profilePic;
    private Button postBtn;

    private DatabaseReference mDatabase;
    private String userId, currentUserEmail, currentUserName;
    private int myAvatarId = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Aapki XML ka naam 'activity_add_post' hai
        View view = inflater.inflate(R.layout.activity_add_post, container, false);

        // 1. Session Setup
        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userId = sp.getString("userId", "");
        currentUserEmail = sp.getString("userEmail", "");

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 2. Bind Views
        have = view.findViewById(R.id.postHave);
        want = view.findViewById(R.id.postWant);
        msg = view.findViewById(R.id.postMsg);
        profilePic = view.findViewById(R.id.myProfilePic);
        postBtn = view.findViewById(R.id.submitPostBtn);

        // 3. Load User Profile for Post Metadata
        if (!userId.isEmpty()) {
            mDatabase.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && isAdded()) {
                        currentUserName = snapshot.child("name").getValue(String.class);
                        if (snapshot.hasChild("avatarId")) {
                            Object avObj = snapshot.child("avatarId").getValue();
                            myAvatarId = (avObj instanceof Long) ? ((Long) avObj).intValue() : (Integer) avObj;
                            setAvatar(myAvatarId);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

        postBtn.setOnClickListener(v -> submitPost());

        return view;
    }

    private void submitPost() {
        String h = have.getText().toString().trim();
        String w = want.getText().toString().trim();
        String m = msg.getText().toString().trim();

        if (h.isEmpty() || w.isEmpty()) {
            Toast.makeText(requireContext(), "Skill 'Have' and 'Want' are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        postBtn.setEnabled(false); // Double click prevention
        postBtn.setText("Publishing...");

        String postId = mDatabase.child("Posts").push().getKey();

        // Syncing with all possible keys for Dashboard Adapters
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("id", postId);
        postMap.put("userId", userId);

        // Redundancy for SkillAdapter compatibility
        String finalName = (currentUserName != null) ? currentUserName : "SkillSwap User";
        postMap.put("teacher", finalName);
        postMap.put("userName", finalName);
        postMap.put("email", currentUserEmail);
        postMap.put("userEmail", currentUserEmail);

        postMap.put("title", h);
        postMap.put("have", h);
        postMap.put("want", w);
        postMap.put("message", m);
        postMap.put("avatarId", myAvatarId);
        postMap.put("isOpen", true);
        postMap.put("timestamp", System.currentTimeMillis());

        if (postId != null) {
            mDatabase.child("Posts").child(postId).setValue(postMap).addOnSuccessListener(aVoid -> {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Skill Post Live!", Toast.LENGTH_SHORT).show();

                    // Reset UI
                    have.setText(""); want.setText(""); msg.setText("");
                    postBtn.setEnabled(true);
                    postBtn.setText("Submit Post");

                    // Navigate back to Dashboard (Home Tab)
                    if (getActivity() instanceof HomeActivity) {
                        ViewPager2 vp = getActivity().findViewById(R.id.viewPager);
                        if (vp != null) vp.setCurrentItem(0, true);
                    }
                }
            }).addOnFailureListener(e -> {
                if (isAdded()) {
                    postBtn.setEnabled(true);
                    postBtn.setText("Submit Post");
                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        profilePic.setImageResource(res);
    }
}