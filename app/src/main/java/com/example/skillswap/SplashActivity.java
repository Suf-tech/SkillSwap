package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private View centerContent, progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        centerContent = findViewById(R.id.centerContent);
        progressBar = findViewById(R.id.progressBar);

        // Animate center content
        centerContent.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        // Animate progress bar
        progressBar.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(700)
                .start();

        // Navigate after delay
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        }, 2000);
    }
}