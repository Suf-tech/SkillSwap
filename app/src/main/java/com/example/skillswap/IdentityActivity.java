package com.example.skillswap;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IdentityActivity extends AppCompatActivity {

    // XML IDs: idName, idEmail, taughtCount, learnedCount, idAvatar
    private TextView idName, idEmail, taughtCount, learnedCount;
    private ImageView idAvatar;

    private DatabaseReference mDatabase;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identity);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // 1. Intent se Data lein
        userEmail = getIntent().getStringExtra("user_email");
        String name = getIntent().getStringExtra("user_name");
        int avatarId = getIntent().getIntExtra("avatar_id", 0);

        // 2. XML Views Initialize karein
        idName = findViewById(R.id.idName);
        idEmail = findViewById(R.id.idEmail);
        idAvatar = findViewById(R.id.idAvatar);
        taughtCount = findViewById(R.id.taughtCount);
        learnedCount = findViewById(R.id.learnedCount);

        // 3. UI Update karein (Initial Data)
        if (name != null) idName.setText(name);
        if (userEmail != null) idEmail.setText(userEmail);
        setAvatar(avatarId);

        // 4. Firebase se Real-time Stats Calculate karein
        if (userEmail != null && !userEmail.isEmpty()) {
            calculateStats();
        } else {
            Toast.makeText(this, "User email missing for stats", Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateStats() {
        // Requests node se count karein kitni swap requests mukammal huin
        mDatabase.child("Requests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int taught = 0;
                int learned = 0;

                for (DataSnapshot reqSnap : snapshot.getChildren()) {
                    String status = reqSnap.child("status").getValue(String.class);
                    String senderEmail = reqSnap.child("senderEmail").getValue(String.class);
                    String receiverEmail = reqSnap.child("receiverEmail").getValue(String.class);

                    // LOGIC: Sirf Accepted ya Completed requests count karein
                    if (status != null && (status.equalsIgnoreCase("Accepted") || status.equalsIgnoreCase("Completed"))) {

                        // Agar main sender tha, matlab maine doosre se kuch seekha (Learned)
                        if (userEmail.equalsIgnoreCase(senderEmail)) {
                            learned++;
                        }
                        // Agar main receiver tha, matlab maine kisi ko sikhaya (Taught)
                        else if (userEmail.equalsIgnoreCase(receiverEmail)) {
                            taught++;
                        }
                    }
                }

                // Final counts update karein UI par
                taughtCount.setText(String.valueOf(taught));
                learnedCount.setText(String.valueOf(learned));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("IdentityActivity", "Stats calculation failed: " + error.getMessage());
            }
        });
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background; // Default background
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        idAvatar.setImageResource(res);
    }
}