package com.example.skillswap;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class RequestActivity extends AppCompatActivity {
    ListView requestListView;
    DatabaseHelper db;
    ArrayList<RequestModel> requestList;
    RequestAdapter adapter;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        // Mapping ID from XML
        requestListView = findViewById(R.id.requestListView);
        db = new DatabaseHelper(this);

        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUser = sp.getString("user_email", "");

        loadRequests();
    }

    public void loadRequests() {
        requestList = new ArrayList<>();
        // Make sure 'getMyRequests' method exists in your DatabaseHelper
        Cursor cursor = db.getMyRequests(currentUser);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    requestList.add(new RequestModel(
                            cursor.getInt(0),      // ID
                            cursor.getString(1),   // Sender
                            cursor.getString(2),   // Receiver
                            cursor.getString(3),   // Skill Offered
                            cursor.getString(4),   // Skill Required
                            cursor.getString(5),   // Message
                            cursor.getString(6)    // Status
                    ));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        // Initializing Adapter
        adapter = new RequestAdapter(this, requestList, currentUser);
        requestListView.setAdapter(adapter);
    }
}