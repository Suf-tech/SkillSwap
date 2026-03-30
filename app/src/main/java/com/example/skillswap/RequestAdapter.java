package com.example.skillswap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class RequestAdapter extends BaseAdapter {
    Context context;
    ArrayList<RequestModel> list;
    String currentUser;
    DatabaseHelper db;

    public RequestAdapter(Context context, ArrayList<RequestModel> list, String currentUser) {
        this.context = context;
        this.list = list;
        this.currentUser = currentUser;
        this.db = new DatabaseHelper(context);
    }

    @Override
    public int getCount() { return list.size(); }
    @Override
    public Object getItem(int i) { return list.get(i); }
    @Override
    public long getItemId(int i) { return i; }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.request_item, viewGroup, false);
        }

        RequestModel req = list.get(i);
        TextView type = view.findViewById(R.id.reqType);
        TextView userEmail = view.findViewById(R.id.reqUserEmail);
        TextView details = view.findViewById(R.id.reqDetails);
        TextView message = view.findViewById(R.id.reqMessage);
        TextView status = view.findViewById(R.id.reqStatus);

        LinearLayout actionLayout = view.findViewById(R.id.actionLayout);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnReject = view.findViewById(R.id.btnReject);
        Button btnChat = view.findViewById(R.id.btnChat);
        Button btnComplete = view.findViewById(R.id.btnComplete);
        Button btnRate = view.findViewById(R.id.btnRate);

        boolean isSender = req.getSender().equalsIgnoreCase(currentUser);
        String otherUserEmail = isSender ? req.getReceiver() : req.getSender();
        boolean hasRated = isSender ? (req.getSenderRated() == 1) : (req.getReceiverRated() == 1);

        status.setText(req.getStatus());
        if(req.getStatus().equals("Accepted")) status.setTextColor(Color.parseColor("#4CAF50"));
        else if(req.getStatus().equals("Completed")) status.setTextColor(Color.parseColor("#9C27B0"));
        else if(req.getStatus().equals("Rejected")) status.setTextColor(Color.parseColor("#F44336"));
        else status.setTextColor(Color.parseColor("#FF9800"));

        if (isSender) {
            type.setText("OUTGOING");
            type.setTextColor(Color.parseColor("#4CAF50"));
            userEmail.setText("To: " + req.getReceiver());
        } else {
            type.setText("INCOMING");
            type.setTextColor(Color.parseColor("#2196F3"));
            userEmail.setText("From: " + req.getSender());
        }

        details.setText("Wants: " + req.getRequired() + "\nOffers: " + req.getOffered());
        message.setText("\"" + req.getMsg() + "\"");

        // Hide buttons for recycling
        btnAccept.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnChat.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnRate.setVisibility(View.GONE);
        actionLayout.setVisibility(View.GONE);

        // Lifecycle Visibility Logic
        if (req.getStatus().equals("Accepted")) {
            actionLayout.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
            btnComplete.setVisibility(View.VISIBLE);
        } else if (req.getStatus().equals("Completed")) {
            actionLayout.setVisibility(View.VISIBLE);
            btnChat.setVisibility(View.VISIBLE);
            // Only show rating button if they haven't rated yet!
            if (!hasRated) {
                btnRate.setVisibility(View.VISIBLE);
            }
        } else if (req.getStatus().equals("Pending") && !isSender) {
            actionLayout.setVisibility(View.VISIBLE);
            btnAccept.setVisibility(View.VISIBLE);
            btnReject.setVisibility(View.VISIBLE);
        }

        // --- Action Listeners ---
        btnAccept.setOnClickListener(v -> {
            if (db.updateRequestStatus(req.getId(), "Accepted")) {
                db.closePost(req.getPostId()); // FIX: Hides the post from the dashboard!
                req.setStatus("Accepted");
                notifyDataSetChanged();
            }
        });

        btnReject.setOnClickListener(v -> {
            if (db.updateRequestStatus(req.getId(), "Rejected")) {
                req.setStatus("Rejected");
                notifyDataSetChanged();
            }
        });

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chat_with_email", otherUserEmail);
            intent.putExtra("request_id", req.getId()); // FIX: Pass specific session ID
            intent.putExtra("isCompleted", req.getStatus().equals("Completed"));
            context.startActivity(intent);
        });

        btnComplete.setOnClickListener(v -> {
            if (db.updateRequestStatus(req.getId(), "Completed")) {
                req.setStatus("Completed");
                notifyDataSetChanged();
                Toast.makeText(context, "Swap Completed!", Toast.LENGTH_SHORT).show();
                if (!hasRated) btnRate.performClick();
            }
        });

        btnRate.setOnClickListener(v -> {
            CharSequence[] options = {"1 Star (Poor)", "2 Stars", "3 Stars", "4 Stars", "5 Stars (Excellent)"};
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Rate " + db.getUserName(otherUserEmail));
            builder.setItems(options, (dialog, which) -> {
                float ratingValue = which + 1.0f;
                if (db.rateUser(otherUserEmail, ratingValue)) {
                    // Lock the rating permanently
                    db.markAsRated(req.getId(), isSender);
                    if (isSender) req.setSenderRated(1);
                    else req.setReceiverRated(1);

                    notifyDataSetChanged();
                    Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error submitting rating.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.show();
        });

        return view;
    }
}