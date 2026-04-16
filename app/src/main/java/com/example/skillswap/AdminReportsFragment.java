package com.example.skillswap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminReportsFragment extends Fragment {

    private DatabaseReference mDatabase;
    private TextView tvStatusSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // fragment_admin_reports.xml ko inflate karein
        View view = inflater.inflate(R.layout.fragment_admin_reports, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Agar aap XML mein koi extra TextView add karte hain to yahan bind kar sakte hain
        // Abhi ke liye hum sirf basic initialization kar rahe hain

        loadReportStats();

        return view;
    }

    private void loadReportStats() {
        // Example logic: Swap Success Rate Calculate karna
        mDatabase.child("Requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded()) return;

                long totalRequests = snapshot.getChildrenCount();
                long completedSwaps = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    String status = data.child("status").getValue(String.class);
                    if ("Completed".equalsIgnoreCase(status)) {
                        completedSwaps++;
                    }
                }

                // Yahan aap mazeed reports ka logic likh sakte hain
                // Jaise average response time ya user growth
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                // Toolbar title update
                activity.getSupportActionBar().setTitle("System Reports");
            }
        }
    }
}