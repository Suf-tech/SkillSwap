package com.example.skillswap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class AdminRequestsAdapter extends RecyclerView.Adapter<AdminRequestsAdapter.ViewHolder> {

    public interface OnRequestDeleteListener {
        void onDeleteClick(RequestModel request);
    }

    private final List<RequestModel> requestList;
    private final OnRequestDeleteListener deleteListener;

    public AdminRequestsAdapter(List<RequestModel> requestList, OnRequestDeleteListener deleteListener) {
        this.requestList = new ArrayList<>(requestList);
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // ZAROORI: Check karein ke layout file ka naam 'item_admin_request_manage.xml' hi ho
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_request_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RequestModel req = requestList.get(position);

        holder.reqDirection.setText(req.getSenderName() + " → " + req.getReceiverName());
        holder.reqSkills.setText("Offers: " + req.getOffered() + " | Wants: " + req.getRequired());
        holder.reqMsg.setText(req.getMsg());
        holder.reqStatus.setText("Status: " + req.getStatus());

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(req);
            }
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    // --- YE WALA HISSA AAPKE SCREENSHOT MEIN GHALAT THA ---
    static class ViewHolder extends RecyclerView.ViewHolder {
        // 1. Pehle variables declare karein (Ye missing thay)
        TextView reqDirection, reqSkills, reqMsg, reqStatus;
        MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // 2. Phir unhein bind karein
            reqDirection = itemView.findViewById(R.id.reqDirection);
            reqSkills = itemView.findViewById(R.id.reqSkills);
            reqMsg = itemView.findViewById(R.id.reqMsg);
            reqStatus = itemView.findViewById(R.id.reqStatusAdmin);
            btnDelete = itemView.findViewById(R.id.btnDeleteRequest);
        }
    }
}