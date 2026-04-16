package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRequestsActivity extends AppCompatActivity {

    // XML ID matching: requestListView (Aapki XML mein 's' nahi hai)
    private ListView listView;
    private ArrayList<RequestModel> requestList;
    private RequestAdapter adapter;

    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        // 1. Session Se UID Lein (For Stability as per Alber's requirement)
        SharedPreferences sp = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUserId = sp.getString("userId", "");

        if (currentUserId.isEmpty()) {
            Toast.makeText(this, "Session expired! Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Requests");

        // 2. Initialize Views (Sync with XML ID: requestListView)
        listView = findViewById(R.id.requestListView);
        requestList = new ArrayList<>();

        // 3. Adapter Initialization
        // Hum wahi professional RequestAdapter use kar rahe hain jo aapne upar provide kiya
        adapter = new RequestAdapter(this, requestList, currentUserId);
        listView.setAdapter(adapter);

        loadMyRequests();
    }

    private void loadMyRequests() {
        // Real-time listener taake status (Accept/Reject/Complete) foran update ho
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot reqSnap : snapshot.getChildren()) {
                    try {
                        RequestModel request = reqSnap.getValue(RequestModel.class);

                        if (request != null) {
                            // Logic: Check karein ke user is request mein involve hai (Sender ya Receiver)
                            String senderId = request.getSenderId();
                            String receiverId = request.getReceiverId();

                            if (currentUserId.equals(senderId) || currentUserId.equals(receiverId)) {
                                requestList.add(request);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MyRequests", "Error parsing request: " + e.getMessage());
                    }
                }

                adapter.notifyDataSetChanged();

                if (requestList.isEmpty()) {
                    Toast.makeText(MyRequestsActivity.this, "No swap requests found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MyRequestsActivity.this, "Database Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}