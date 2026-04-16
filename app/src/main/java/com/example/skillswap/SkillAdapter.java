package com.example.skillswap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SkillAdapter extends RecyclerView.Adapter<SkillAdapter.ViewHolder> {
    Context context;
    ArrayList<Skill> list;
    DatabaseReference mDatabase;
    String currentUserEmail = "";

    public SkillAdapter(Context context, ArrayList<Skill> list) {
        this.context = context;
        this.list = list;
        this.mDatabase = FirebaseDatabase.getInstance().getReference().child("Posts");

        SharedPreferences sp = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        this.currentUserEmail = sp.getString("userEmail", "");
    }

    public void setFilteredList(ArrayList<Skill> filteredList) {
        this.list = filteredList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML name: skill_item.xml
        View view = LayoutInflater.from(context).inflate(R.layout.skill_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Skill skill = list.get(position);
        if (skill == null) return;

        // Data Binding (Sync with Skill.java and new XML)
        holder.title.setText(skill.getTitle() != null ? skill.getTitle() : "No Skill Offered");
        holder.teacher.setText(skill.getTeacher() != null ? skill.getTeacher() : "User");
        holder.wantSkill.setText(skill.getWant() != null ? skill.getWant() : "Anything");

        // Avatar handling
        setAvatar(holder.userImage, skill.getAvatarId());

        String postEmail = skill.getEmail() != null ? skill.getEmail() : "";

        // Logic: Agar ye post meri apni hai toh Edit/Delete dikhayen
        if (!postEmail.isEmpty() && postEmail.equalsIgnoreCase(currentUserEmail)) {
            holder.viewDetails.setText("Manage Post");
            holder.viewDetails.setOnClickListener(v -> showOptionsDialog(skill));
        } else {
            // Doosre user ke liye View Details
            holder.viewDetails.setText("View Details");
            holder.viewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("postId", skill.getId());
                context.startActivity(intent);
            });
        }
    }

    private void showOptionsDialog(Skill skill) {
        CharSequence options[] = new CharSequence[] {"Edit Post", "Delete Post"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Post Options");
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(context, EditPostActivity.class);
                intent.putExtra("postId", skill.getId());
                context.startActivity(intent);
            } else {
                mDatabase.child(skill.getId()).removeValue().addOnSuccessListener(aVoid ->
                        Toast.makeText(context, "Post Deleted Successfully", Toast.LENGTH_SHORT).show()
                );
            }
        });
        builder.show();
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // New XML IDs: teacher, title, wantSkill, viewProfileBtn, userImage
        TextView title, teacher, wantSkill;
        Button viewDetails;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            teacher = itemView.findViewById(R.id.teacher);
            wantSkill = itemView.findViewById(R.id.wantSkill);
            viewDetails = itemView.findViewById(R.id.viewProfileBtn);
            userImage = itemView.findViewById(R.id.userImage);
        }
    }

    private void setAvatar(ImageView iv, int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        iv.setImageResource(res);
    }
}