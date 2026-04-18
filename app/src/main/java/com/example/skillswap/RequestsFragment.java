package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RequestsFragment extends Fragment {

    // XML ID matching: requestListView
    private ListView requestListView;
    private ArrayList<RequestModel> requestList;
    private RequestAdapter adapter;

    private DatabaseReference mDatabase;
    private ValueEventListener requestsListener;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");

        requestListView = view.findViewById(R.id.requestListView);
        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requireContext(), requestList, currentUserId, RequestAdapter.Mode.ACTIVE);
        requestListView.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Requests");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!currentUserId.isEmpty()) {
            attachRequestsListener();
        } else if (isAdded()) {
            Toast.makeText(requireContext(), "Session Error: Please Login", Toast.LENGTH_SHORT).show();
        }
    }

    private void attachRequestsListener() {
        if (requestsListener != null) return;

        requestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                requestList.clear();
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

                            if (isInvolvedUser && !isHistoryStatus) {
                                requestList.add(req);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("RequestsFragment", "Error parsing request: " + e.getMessage());
                    }
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        };

        mDatabase.addValueEventListener(requestsListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mDatabase != null && requestsListener != null) {
            mDatabase.removeEventListener(requestsListener);
            requestsListener = null;
        }
    }
}