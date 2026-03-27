package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RequestActivity extends AppCompatActivity {

    EditText name, skillOffered, message;
    Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);

        name = findViewById(R.id.reqName);
        skillOffered = findViewById(R.id.reqSkillOffered);
        message = findViewById(R.id.reqMessage);
        submitBtn = findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(v -> {
            String n = name.getText().toString();
            String s = skillOffered.getText().toString();
            String m = message.getText().toString();

            if (TextUtils.isEmpty(n) || TextUtils.isEmpty(s) || TextUtils.isEmpty(m)) {
                Toast.makeText(this, "Please fill all form fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Request Sent Successfully!", Toast.LENGTH_LONG).show();

                Intent intent = new Intent(RequestActivity.this, MyRequestsActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}