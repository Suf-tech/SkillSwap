package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

// Firebase Import
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private View centerContent, progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        centerContent = findViewById(R.id.centerContent);
        progressBar = findViewById(R.id.progressBar);

        // Animations (Wahi purani makkhan jaisi)
        centerContent.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        progressBar.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(700)
                .start();

        // 2 Second ke delay ke baad decide karein ke kahan jana hai
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkUserSession();
        }, 2000);
    }

    private void checkUserSession() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User pehle se login hai -> Seedha HomeActivity (Dashboard)
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
        } else {
            // User login nahi hai -> LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }
}