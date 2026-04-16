package com.example.skillswap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AdminSkillsAdapter extends RecyclerView.Adapter<AdminSkillsAdapter.ViewHolder> {

    // Interface: Ab ye AdminSkillItem pass karega (Audit View ke liye)
    public interface OnSkillClickListener {
        void onSkillClick(AdminSkillItem skill);
    }

    private final List<AdminSkillItem> skills;
    private final OnSkillClickListener listener;

    public AdminSkillsAdapter(List<AdminSkillItem> skills, OnSkillClickListener listener) {
        this.skills = new ArrayList<>(skills);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML ID: item_admin_skill (Jo audit tags wali XML thi)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_skill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminSkillItem item = skills.get(position);

        // 1. Set Main Data
        holder.skillName.setText(item.getName());
        holder.skillCount.setText(item.getCount() + " uses");

        // 2. Tags visibility (Audit Tags)
        holder.tagRequested.setVisibility(item.isRequested() ? View.VISIBLE : View.GONE);
        holder.tagWanted.setVisibility(item.isWanted() ? View.VISIBLE : View.GONE);
        holder.tagOffered.setVisibility(item.isOffered() ? View.VISIBLE : View.GONE);

        // 3. Item Click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onSkillClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    public void updateList(List<AdminSkillItem> newSkills) {
        skills.clear();
        skills.addAll(newSkills);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView skillName, skillCount, tagRequested, tagWanted, tagOffered;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            skillName = itemView.findViewById(R.id.adminSkillName);
            skillCount = itemView.findViewById(R.id.adminSkillCount);
            tagRequested = itemView.findViewById(R.id.tagRequested);
            tagWanted = itemView.findViewById(R.id.tagWanted);
            tagOffered = itemView.findViewById(R.id.tagOffered);
        }
    }
}