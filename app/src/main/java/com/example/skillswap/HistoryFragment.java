package com.example.skillswap;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HistoryFragment extends Fragment {

    private RecyclerView historyRecyclerView;
    private TextView tvCompletedCount;
    private TextView tvRejectedCount;
    private TextView tvHistoryEmpty;

    private ArrayList<RequestModel> historyList;
    private HistoryAdapter adapter;

    private DatabaseReference mRequestsRef;
    private DatabaseReference mRootRef;
    private ValueEventListener historyListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");

        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        tvCompletedCount = view.findViewById(R.id.tvCompletedCount);
        tvRejectedCount = view.findViewById(R.id.tvRejectedCount);
        tvHistoryEmpty = view.findViewById(R.id.tvHistoryEmpty);

        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(this::onRateClick);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyRecyclerView.setAdapter(adapter);
        attachSwipeToDelete();

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRequestsRef = mRootRef.child("Requests");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!currentUserId.isEmpty()) {
            attachHistoryListener();
        } else if (isAdded()) {
            Toast.makeText(requireContext(), "Session Error: Please Login", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachHistoryListener() {
        if (historyListener != null) return;

        historyListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                historyList.clear();
                int completedCount = 0;
                int rejectedCount = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        RequestModel req = data.getValue(RequestModel.class);
                        if (req != null) {
                            String senderId = req.getSenderId();
                            String receiverId = req.getReceiverId();
                            String status = req.getStatus();

                            boolean isInvolvedUser = currentUserId.equals(senderId) || currentUserId.equals(receiverId);
                            boolean isHistoryStatus = RequestModel.STATUS_COMPLETED.equals(status)
                                    || RequestModel.STATUS_REJECTED.equals(status);

                            if (isInvolvedUser && isHistoryStatus) {
                                historyList.add(req);
                                if (RequestModel.STATUS_COMPLETED.equals(status)) {
                                    completedCount++;
                                } else {
                                    rejectedCount++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("HistoryFragment", "Error parsing request: " + e.getMessage());
                    }
                }

                historyList.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                adapter.submitList(historyList);

                tvCompletedCount.setText(String.valueOf(completedCount));
                tvRejectedCount.setText(String.valueOf(rejectedCount));
                tvHistoryEmpty.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mRequestsRef.addValueEventListener(historyListener);
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                RequestModel req = adapter.getItemAt(position);

                if (req == null || req.getId() == null) {
                    adapter.notifyItemChanged(position);
                    return;
                }

                String status = req.getStatus();
                boolean isHistoryStatus = RequestModel.STATUS_COMPLETED.equals(status)
                        || RequestModel.STATUS_REJECTED.equals(status);

                if (!isHistoryStatus) {
                    adapter.notifyItemChanged(position);
                    return;
                }

                mRequestsRef.child(req.getId()).removeValue()
                        .addOnSuccessListener(unused -> Toast.makeText(requireContext(), "History item deleted", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> {
                            adapter.notifyItemChanged(position);
                            Toast.makeText(requireContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(historyRecyclerView);
    }

    private void onRateClick(RequestModel req) {
        if (req == null) return;

        String status = req.getStatus();
        if (!RequestModel.STATUS_COMPLETED.equals(status)) {
            Toast.makeText(requireContext(), "Only completed requests can be rated", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null);
        builder.setView(dialogView);

        RatingBar ratingBar = dialogView.findViewById(R.id.dialogRatingBar);
        EditText commentInput = dialogView.findViewById(R.id.dialogComment);
        Button btnSubmit = dialogView.findViewById(R.id.btnSubmitRating);

        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            float ratingValue = ratingBar.getRating();
            if (ratingValue == 0) {
                Toast.makeText(requireContext(), "Please select stars", Toast.LENGTH_SHORT).show();
                return;
            }

            String targetUserId = currentUserId.equals(req.getSenderId()) ? req.getReceiverId() : req.getSenderId();
            String myName = currentUserId.equals(req.getSenderId()) ? req.getSenderName() : req.getReceiverName();
            String requestId = req.getId();

            if (targetUserId == null || targetUserId.trim().isEmpty() || requestId == null || requestId.trim().isEmpty()) {
                Toast.makeText(requireContext(), "Unable to submit rating for this request", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> ratingMap = new HashMap<>();
            ratingMap.put("rating", ratingValue);
            ratingMap.put("comment", commentInput.getText().toString().trim());
            ratingMap.put("fromName", myName);
            ratingMap.put("fromId", currentUserId);
            ratingMap.put("timestamp", System.currentTimeMillis());

            mRootRef.child("Ratings").child(targetUserId).child(requestId).setValue(ratingMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(requireContext(), "Rating submitted!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Rating failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mRequestsRef != null && historyListener != null) {
            mRequestsRef.removeEventListener(historyListener);
            historyListener = null;
        }
    }
}

