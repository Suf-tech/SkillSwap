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
    private String currentUserEmail;

    public ChatAdapter(Context context, ArrayList<MessageModel> list, String currentUserEmail) {
        this.context = context;
        this.list = list;
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageModel message = list.get(position);
        holder.messageText.setText(message.getMessageText());

        if (message.getSenderEmail().equalsIgnoreCase(currentUserEmail)) {
            // Sent Message: Align Right, Blue Bubble
            holder.messageContainer.setGravity(Gravity.END);
            holder.messageText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2196F3")));
            holder.messageText.setTextColor(Color.WHITE);
        } else {
            // Received Message: Align Left, Grey Bubble
            holder.messageContainer.setGravity(Gravity.START);
            holder.messageText.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E0E0E0")));
            holder.messageText.setTextColor(Color.BLACK);
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
    }
}