package com.example.skillswap;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

public class MyRequestsActivity extends AppCompatActivity {

    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        listView = findViewById(R.id.requestsListView);

        // Static Dummy Data
        String[] requests = {
                "Learn Java - Status: Pending",
                "Learn Photoshop - Status: Accepted",
                "Graphic Design - Status: Rejected"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, requests);

        listView.setAdapter(adapter);
    }
}