package com.example.skillswap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminSkillRequesterAdapter extends RecyclerView.Adapter<AdminSkillRequesterAdapter.ViewHolder> {

    private final List<AdminSkillRequester> requesterList;

    public AdminSkillRequesterAdapter(List<AdminSkillRequester> requesterList) {
        this.requesterList = requesterList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML ID: item_admin_skill_requester (Aapki di hui layout)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_skill_requester, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminSkillRequester requester = requesterList.get(position);

        // 1. Set Text Data (Sync with XML IDs)
        holder.name.setText(requester.name != null ? requester.name : "Unknown User");
        holder.email.setText(requester.email != null ? requester.email : "No Email");

        // 2. Set Avatar Logic
        setAvatar(holder.avatar, requester.avatarId);
    }

    @Override
    public int getItemCount() {
        return requesterList.size();
    }

    private void setAvatar(ImageView iv, int id) {
        int res = R.drawable.editbox_background; // Default background
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        iv.setImageResource(res);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name, email;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // In IDs ko aapki XML ke mutabiq sync kar diya gaya hai
            avatar = itemView.findViewById(R.id.requesterAvatar);
            name = itemView.findViewById(R.id.requesterName);
            email = itemView.findViewById(R.id.requesterEmail);
        }
    }
}