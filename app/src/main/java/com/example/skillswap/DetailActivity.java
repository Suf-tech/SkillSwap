package com.example.skillswap;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // Standard import add kiya

// FIXED: BaseActivity ki jagah AppCompatActivity use karein
public class DetailActivity extends AppCompatActivity {

    EditText reqSkill, offerSkill, swapMsg;
    TextView teacherName;
    ImageView teacherImg;
    Button requestBtn;
    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        db = new DatabaseHelper(this);

        teacherName = findViewById(R.id.teacher);
        teacherImg = findViewById(R.id.teacherImage);
        reqSkill = findViewById(R.id.reqSkill);
        offerSkill = findViewById(R.id.offerSkill);
        swapMsg = findViewById(R.id.swapMsg);
        requestBtn = findViewById(R.id.requestBtn);

        String name = getIntent().getStringExtra("teacher");
        String receiverEmail = getIntent().getStringExtra("email");
        String skillTitle = getIntent().getStringExtra("title");
        int avatarId = getIntent().getIntExtra("avatarId", 0);

        teacherName.setText(name);
        reqSkill.setText(skillTitle);

        setAvatar(avatarId);

        requestBtn.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
            String senderEmail = sp.getString("user_email", "");

            String offered = offerSkill.getText().toString().trim();
            String required = reqSkill.getText().toString().trim();
            String msg = swapMsg.getText().toString().trim();

            if (offered.isEmpty() || required.isEmpty() || msg.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Database method call
            boolean isSent = db.sendRequest(senderEmail, receiverEmail, offered, required, msg);

            if (isSent) {
                Toast.makeText(this, "Request Sent Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to send request", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAvatar(int id) {
        int res = R.drawable.editbox_background;
        if (id == 1) res = R.drawable.avatar_m1;
        else if (id == 2) res = R.drawable.avatar_m2;
        else if (id == 3) res = R.drawable.avatar_m3;
        else if (id == 4) res = R.drawable.avatar_f1;
        else if (id == 5) res = R.drawable.avatar_f2;
        else if (id == 6) res = R.drawable.avatar_f3;
        teacherImg.setImageResource(res);
    }
}