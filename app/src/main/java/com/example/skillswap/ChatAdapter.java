package com.example.skillswap;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageModel> list;
    private String currentUserId;

    public ChatAdapter(Context context, ArrayList<MessageModel> list, String currentUserId) {
        this.context = context;
        this.list = list;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // XML ID: item_chat_message
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = list.get(position);

        if (message == null) return;

        // Sync with MessageModel.java
        holder.messageText.setText(message.getMessageText());

        // --- BUBBLE ALIGNMENT & COLOR LOGIC ---
        if (message.getSender() != null && message.getSender().equals(currentUserId)) {
            // My Message: Right Side (Blue)
            holder.messageContainer.setGravity(Gravity.END);
            holder.messageText.setBackgroundResource(R.drawable.editbox_background);

            // Background Tint fix for better stability
            holder.messageText.getBackground().setTint(Color.parseColor("#2196F3"));
            holder.messageText.setTextColor(Color.WHITE);

            // Margin adjustment for right side
            holder.setMargins(holder.messageContainer, 50, 0);
        } else {
            // Partner Message: Left Side (Grey)
            holder.messageContainer.setGravity(Gravity.START);
            holder.messageText.setBackgroundResource(R.drawable.editbox_background);

            holder.messageText.getBackground().setTint(Color.parseColor("#E0E0E0"));
            holder.messageText.setTextColor(Color.BLACK);

            holder.setMargins(holder.messageContainer, 0, 50);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView messageText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageText = itemView.findViewById(R.id.messageText);
        }

        // Helper to set margins dynamically
        public void setMargins(View v, int left, int right) {
            if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                p.setMargins(left, 0, right, 0);
                v.requestLayout();
            }
        }
    }
}