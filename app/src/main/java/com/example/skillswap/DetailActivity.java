package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {
    TextView title, teacher;
    Button requestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        title = findViewById(R.id.title);
        teacher = findViewById(R.id.teacher);
        requestBtn = findViewById(R.id.requestBtn);

        Intent intent = getIntent();
        title.setText(intent.getStringExtra("title"));
        teacher.setText("By: " + intent.getStringExtra("teacher"));

        // YE WALI LINE ADD KARNI HAI 👇
        requestBtn.setOnClickListener(v -> {
            Intent i = new Intent(DetailActivity.this, RequestActivity.class);
            startActivity(i);
        });
    }
}