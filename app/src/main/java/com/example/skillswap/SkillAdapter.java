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
import java.util.ArrayList;

public class SkillAdapter extends RecyclerView.Adapter<SkillAdapter.ViewHolder> {
    Context context;
    ArrayList<Skill> list;
    String currentUserEmail;
    DatabaseHelper db;

    public SkillAdapter(Context context, ArrayList<Skill> list) {
        this.context = context;
        this.list = list;
        this.db = new DatabaseHelper(context);
        SharedPreferences sp = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        this.currentUserEmail = sp.getString("user_email", "");
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Skill skill = list.get(position);
        holder.title.setText(skill.getTitle());
        holder.teacher.setText(skill.getTeacher());
        setAvatar(holder.userImage, skill.getAvatarId());

        if (skill.getEmail().equalsIgnoreCase(currentUserEmail)) {
            holder.viewDetails.setText("Edit / Delete");
            holder.viewDetails.setOnClickListener(v -> {
                CharSequence options[] = new CharSequence[] {"Edit Post", "Delete Post"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Post Options");
                builder.setItems(options, (dialog, which) -> {
                    if (which == 0) { // Edit Post Logic
                        Intent intent = new Intent(context, EditPostActivity.class);
                        // FIXED: Pass correct data to Edit screen
                        intent.putExtra("postId", skill.getId());
                        intent.putExtra("have", skill.getTitle());
                        // Note: If your Skill model has want/msg, pass them here
                        context.startActivity(intent);
                    } else { // Delete Post Logic
                        if (db.deletePost(skill.getId())) {
                            list.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, list.size());
                            Toast.makeText(context, "Post Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.show();
            });
        } else {
            holder.viewDetails.setText("View Details");
            holder.viewDetails.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("email", skill.getEmail());
                intent.putExtra("title", skill.getTitle());
                intent.putExtra("teacher", skill.getTeacher());
                intent.putExtra("avatarId", skill.getAvatarId());
                context.startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.skill_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, teacher;
        Button viewDetails;
        ImageView userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            teacher = itemView.findViewById(R.id.teacher);
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