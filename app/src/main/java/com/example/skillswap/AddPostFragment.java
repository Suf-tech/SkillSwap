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

public class AddPostFragment extends Fragment {

    EditText have, want, msg;
    ImageView profilePic;
    Button postBtn;
    DatabaseHelper db;
    String userEmail;
    int myAvatarId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        db = new DatabaseHelper(requireContext());
        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        userEmail = sp.getString("user_email", "");

        have = view.findViewById(R.id.postHave);
        want = view.findViewById(R.id.postWant);
        msg = view.findViewById(R.id.postMsg);
        profilePic = view.findViewById(R.id.myProfilePic);
        postBtn = view.findViewById(R.id.submitPostBtn);

        myAvatarId = db.getAvatarId(userEmail);
        setAvatar(myAvatarId);

        postBtn.setOnClickListener(v -> {
            String h = have.getText().toString().trim();
            String w = want.getText().toString().trim();
            String m = msg.getText().toString().trim();
            String name = db.getUserName(userEmail);

            if (h.isEmpty() || w.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in skills!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.addPost(userEmail, name, h, w, m, myAvatarId)) {
                Toast.makeText(requireContext(), "Skill Swap Post Created!", Toast.LENGTH_SHORT).show();

                // Clear the form
                have.setText("");
                want.setText("");
                msg.setText("");

                // Navigate back to the Home Tab automatically
                if (getActivity() instanceof HomeActivity) {
                    ViewPager2 viewPager = getActivity().findViewById(R.id.viewPager);
                    viewPager.setCurrentItem(0, true);
                }
            } else {
                Toast.makeText(requireContext(), "Error: Could not create post", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
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