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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestAdapter extends BaseAdapter {
    public enum Mode {
        ACTIVE,
        HISTORY
    }

    Context context;
    ArrayList<RequestModel> list;
    String currentUserId;
    DatabaseReference mDatabase;
    Mode mode;

    public RequestAdapter(Context context, ArrayList<RequestModel> list, String currentUserId) {
        this(context, list, currentUserId, Mode.ACTIVE);
    }

    public RequestAdapter(Context context, ArrayList<RequestModel> list, String currentUserId, Mode mode) {
        this.context = context;
        this.list = list;
        this.currentUserId = currentUserId;
        this.mDatabase = FirebaseDatabase.getInstance().getReference();
        this.mode = mode;
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

        if (req == null) return view;

        boolean isSender = req.getSenderId() != null && req.getSenderId().equals(currentUserId);
        String otherUserId = isSender ? req.getReceiverId() : req.getSenderId();
        String currentStatus = req.getStatus() != null ? req.getStatus() : RequestModel.STATUS_PENDING;

        status.setText(currentStatus.toUpperCase());

        if (RequestModel.STATUS_ACCEPTED.equals(currentStatus)) status.setTextColor(Color.parseColor("#4CAF50"));
        else if (RequestModel.STATUS_COMPLETED.equals(currentStatus)) status.setTextColor(Color.parseColor("#9C27B0"));
        else if (RequestModel.STATUS_REJECTED.equals(currentStatus)) status.setTextColor(Color.parseColor("#F44336"));
        else status.setTextColor(Color.parseColor("#FF9800"));

        if (isSender) {
            type.setText("OUTGOING REQUEST");
            type.setTextColor(Color.parseColor("#4CAF50"));
            userEmail.setText("To: " + (req.getReceiverName() != null ? req.getReceiverName() : req.getReceiverEmail()));
        } else {
            type.setText("INCOMING REQUEST");
            type.setTextColor(Color.parseColor("#2196F3"));
            userEmail.setText("From: " + (req.getSenderName() != null ? req.getSenderName() : req.getSenderEmail()));
        }

        details.setText("Wants: " + req.getRequired() + "\nOffers: " + req.getOffered());
        message.setText("\"" + req.getMsg() + "\"");

        // --- VISIBILITY LOGIC ---
        actionLayout.setVisibility(View.GONE);
        btnAccept.setVisibility(View.GONE);
        btnReject.setVisibility(View.GONE);
        btnChat.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnRate.setVisibility(View.GONE);

        if (mode == Mode.HISTORY) {
            if (RequestModel.STATUS_COMPLETED.equals(currentStatus)) {
                actionLayout.setVisibility(View.VISIBLE);

                // Completed items stay read-only except rating access.
                mDatabase.child("Ratings").child(otherUserId).child(req.getId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                btnRate.setVisibility(snapshot.exists() ? View.GONE : View.VISIBLE);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });
            }
        } else {
            if (RequestModel.STATUS_ACCEPTED.equals(currentStatus)) {
                actionLayout.setVisibility(View.VISIBLE);
                btnChat.setVisibility(View.VISIBLE);
                btnComplete.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE); // Reject is allowed after accepted as a failure state.
            } else if (RequestModel.STATUS_PENDING.equals(currentStatus) && !isSender) {
                actionLayout.setVisibility(View.VISIBLE);
                btnAccept.setVisibility(View.VISIBLE);
                btnReject.setVisibility(View.VISIBLE);
            }
        }

        // --- LISTENERS ---
        btnAccept.setOnClickListener(v -> updateStatus(req, RequestModel.STATUS_ACCEPTED));
        btnReject.setOnClickListener(v -> updateStatus(req, RequestModel.STATUS_REJECTED));

        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("chat_with_id", otherUserId);
            intent.putExtra("request_id", req.getId());
            intent.putExtra("chat_with_name", isSender ? req.getReceiverName() : req.getSenderName());
            context.startActivity(intent);
        });

        btnComplete.setOnClickListener(v -> updateStatus(req, RequestModel.STATUS_COMPLETED));

        btnRate.setOnClickListener(v -> showRatingDialog(req));

        return view;
    }

    private void showRatingDialog(RequestModel req) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText commentInput = dialogView.findViewById(R.id.dialogComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitRating);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            float ratingValue = ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();

            if (ratingValue == 0) {
                Toast.makeText(context, "Please select stars", Toast.LENGTH_SHORT).show();
                return;
            }

            String targetUserId = currentUserId.equals(req.getSenderId()) ? req.getReceiverId() : req.getSenderId();
            String myName = currentUserId.equals(req.getSenderId()) ? req.getSenderName() : req.getReceiverName();
            String requestId = req.getId();

            if (targetUserId == null || targetUserId.trim().isEmpty() || requestId == null || requestId.trim().isEmpty()) {
                Toast.makeText(context, "Unable to submit rating for this request", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> ratingMap = new HashMap<>();
            ratingMap.put("rating", ratingValue);
            ratingMap.put("comment", comment);
            ratingMap.put("fromName", myName);
            ratingMap.put("fromId", currentUserId);
            ratingMap.put("timestamp", System.currentTimeMillis());

            mDatabase.child("Ratings").child(targetUserId).child(requestId).setValue(ratingMap)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Rating Submitted!", Toast.LENGTH_SHORT).show();
                        // Refresh the list to hide the rate button immediately
                        notifyDataSetChanged();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Rating failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
        dialog.show();
    }

    private void updateStatus(RequestModel req, String newStatus) {
        if (req == null) return;
        String requestId = req.getId();
        if (requestId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("timestamp", System.currentTimeMillis());

        mDatabase.child("Requests").child(requestId).updateChildren(updates).addOnSuccessListener(aVoid -> {
            if (RequestModel.STATUS_ACCEPTED.equals(newStatus)) {
                if (req.getPostId() != null) {
                    mDatabase.child("Posts").child(req.getPostId()).child("isOpen").setValue(false);
                }

                // Close all other posts for the current user too
                mDatabase.child("Posts").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String postUserId = ds.child("userId").getValue(String.class);
                            Boolean isOpen = ds.child("isOpen").getValue(Boolean.class);

                            if (postUserId != null && postUserId.equals(currentUserId) && isOpen != null && isOpen) {
                                ds.getRef().child("isOpen").setValue(false);
                            }
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
            }
            Toast.makeText(context, "Request " + newStatus, Toast.LENGTH_SHORT).show();
        });
    }
}