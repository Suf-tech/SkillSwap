package com.example.skillswap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdminUsersAdapter extends RecyclerView.Adapter<AdminUsersAdapter.ViewHolder> {

    public interface OnUserClickListener {
        void onUserClick(AdminUser user);
        void onUserEditClick(AdminUser user);
    }

    private final Context context;
    private final List<AdminUser> users;
    private final OnUserClickListener listener;

    public AdminUsersAdapter(Context context, List<AdminUser> users, OnUserClickListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML name: item_admin_user.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminUser user = users.get(position);

        // Data binding with XML IDs
        holder.name.setText(user.name != null ? user.name : "Unknown");
        holder.email.setText(user.email != null ? user.email : "No Email");

        // Avatar setup
        setAvatar(holder.avatar, user.avatarId);

        // Click on entire card for details
        holder.itemView.setOnClickListener(v -> listener.onUserClick(user));

        // Click on edit icon specifically (ID: adminUserEdit)
        holder.edit.setOnClickListener(v -> listener.onUserEditClick(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateList(List<AdminUser> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        ImageView avatar, edit;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Sync with your XML IDs exactly
            name = itemView.findViewById(R.id.adminUserName);
            email = itemView.findViewById(R.id.adminUserEmail);
            avatar = itemView.findViewById(R.id.adminUserAvatar);
            edit = itemView.findViewById(R.id.adminUserEdit);
        }
    }

    private void setAvatar(ImageView iv, int id) {
        int res = R.drawable.editbox_background; // Default
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        iv.setImageResource(res);
    }
}