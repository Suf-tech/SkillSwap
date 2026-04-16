package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // System Bars Padding (Jo aapne pehle likha tha)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- SPLASH & REDIRECT LOGIC ---
        // 2 Second ka delay taake logo nazar aaye
        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);
            String role = sp.getString("role", "user");

            Intent intent;
            if (isLoggedIn) {
                // Agar login hai, to check karein Admin hai ya User
                if ("admin".equals(role)) {
                    intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, HomeActivity.class);
                }
            } else {
                // Agar login nahi hai, to Login screen
                intent = new Intent(MainActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish(); // Splash activity ko khatam kar dein taake back karne pe ye na aaye

        }, 2000); // 2000ms = 2 seconds
    }
}