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
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_requests.xml inflate karein
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        // 1. Session Initialization
        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");

        // 2. View Bindings
        requestListView = view.findViewById(R.id.requestListView);
        requestList = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Requests");

        // 3. Adapter Setup
        if (isAdded()) {
            adapter = new RequestAdapter(requireContext(), requestList, currentUserId);
            requestListView.setAdapter(adapter);

            if (!currentUserId.isEmpty()) {
                loadRequestsFromFirebase();
            } else {
                Toast.makeText(requireContext(), "Session Error: Please Login", Toast.LENGTH_SHORT).show();
            }
        }

        return view;
    }

    private void loadRequestsFromFirebase() {
        // Real-time listener: Jab status change hogi (Accept/Reject), list khud refresh ho jayegi
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return; // Fragment safety: Crash prevention

                requestList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    try {
                        RequestModel req = data.getValue(RequestModel.class);
                        if (req != null) {
                            String senderId = req.getSenderId();
                            String receiverId = req.getReceiverId();

                            // Logic: Sirf wahi requests dikhayen jahan current user sender ya receiver ho
                            if (currentUserId.equals(senderId) || currentUserId.equals(receiverId)) {
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
        });
    }
}