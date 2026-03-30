package com.example.skillswap;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;

public class RequestsFragment extends Fragment {
    ListView requestListView;
    DatabaseHelper db;
    ArrayList<RequestModel> requestList;
    RequestAdapter adapter;
    String currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_requests, container, false);

        requestListView = view.findViewById(R.id.requestListView);
        db = new DatabaseHelper(requireContext());

        SharedPreferences sp = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        currentUser = sp.getString("user_email", "");

        loadRequests();

        return view;
    }

    public void loadRequests() {
        requestList = new ArrayList<>();
        Cursor cursor = db.getMyRequests(currentUser);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    requestList.add(new RequestModel(
                            cursor.getInt(0),      // Request ID
                            cursor.getInt(1),      // Post ID
                            cursor.getString(2),   // Sender
                            cursor.getString(3),   // Receiver
                            cursor.getString(4),   // Skill Offered
                            cursor.getString(5),   // Skill Required
                            cursor.getString(6),   // Message
                            cursor.getString(7),   // Status
                            cursor.getInt(8),      // Sender Rated Boolean
                            cursor.getInt(9)       // Receiver Rated Boolean
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        adapter = new RequestAdapter(requireContext(), requestList, currentUser);
        requestListView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRequests();
    }
}