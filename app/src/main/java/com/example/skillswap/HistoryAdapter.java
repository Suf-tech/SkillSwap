package com.example.skillswap;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnHistoryActionListener {
        void onRateClick(RequestModel request);
    }

    private final List<RequestModel> historyList = new ArrayList<>();
    private final OnHistoryActionListener actionListener;

    public HistoryAdapter(OnHistoryActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void submitList(List<RequestModel> updatedList) {
        int oldSize = historyList.size();
        if (oldSize > 0) {
            historyList.clear();
            notifyItemRangeRemoved(0, oldSize);
        }
        historyList.addAll(updatedList);
        if (!historyList.isEmpty()) {
            notifyItemRangeInserted(0, historyList.size());
        }
    }

    public RequestModel getItemAt(int position) {
        if (position < 0 || position >= historyList.size()) return null;
        return historyList.get(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestModel req = historyList.get(position);
        String required = req.getRequired() != null ? req.getRequired() : "-";
        String offered = req.getOffered() != null ? req.getOffered() : "-";
        String message = req.getMsg() != null ? req.getMsg() : "";
        android.content.Context context = holder.itemView.getContext();

        String direction = (req.getSenderName() != null ? req.getSenderName() : req.getSenderEmail())
                + " → "
                + (req.getReceiverName() != null ? req.getReceiverName() : req.getReceiverEmail());
        holder.tvDirection.setText(direction);

        holder.tvSkills.setText(context.getString(R.string.history_skills_format, required, offered));
        holder.tvMessage.setText(context.getString(R.string.history_message_format, message));

        long timestamp = req.getTimestamp();
        if (timestamp > 0) {
            String formatted = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(new Date(timestamp));
            holder.tvTime.setText(context.getString(R.string.history_ended_on_format, formatted));
        } else {
            holder.tvTime.setText(context.getString(R.string.history_ended_unknown));
        }

        if (RequestModel.STATUS_COMPLETED.equals(req.getStatus())) {
            holder.tvStatus.setText(R.string.history_status_completed);
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
            holder.btnRate.setVisibility(View.VISIBLE);
            holder.btnRate.setOnClickListener(v -> {
                if (actionListener != null) {
                    actionListener.onRateClick(req);
                }
            });
        } else {
            holder.tvStatus.setText(R.string.history_status_rejected);
            holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
            holder.btnRate.setVisibility(View.GONE);
            holder.btnRate.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDirection, tvStatus, tvSkills, tvMessage, tvTime;
        Button btnRate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDirection = itemView.findViewById(R.id.tvHistoryDirection);
            tvStatus = itemView.findViewById(R.id.tvHistoryStatus);
            tvSkills = itemView.findViewById(R.id.tvHistorySkills);
            tvMessage = itemView.findViewById(R.id.tvHistoryMessage);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
            btnRate = itemView.findViewById(R.id.btnHistoryRate);
        }
    }
}

