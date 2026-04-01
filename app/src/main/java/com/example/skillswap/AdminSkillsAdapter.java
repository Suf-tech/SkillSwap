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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_skill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminSkillItem item = skills.get(position);
        holder.skillName.setText(item.getName());
        holder.skillCount.setText(item.getCount() + " uses");

        // ...existing tag visibility logic...
        if (item.isRequested()) {
            holder.tagRequested.setVisibility(View.VISIBLE);
        } else {
            holder.tagRequested.setVisibility(View.GONE);
        }

        if (item.isWanted()) {
            holder.tagWanted.setVisibility(View.VISIBLE);
        } else {
            holder.tagWanted.setVisibility(View.GONE);
        }

        if (item.isOffered()) {
            holder.tagOffered.setVisibility(View.VISIBLE);
        } else {
            holder.tagOffered.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSkillClick(item);
            }
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
        TextView skillName;
        TextView skillCount;
        TextView tagRequested;
        TextView tagWanted;
        TextView tagOffered;

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

