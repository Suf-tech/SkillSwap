package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    public ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Session Check
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);

        if (!isLoggedIn) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_home);

        try {
            // 2. Toolbar Setup
            toolbar = findViewById(R.id.topToolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("SkillSwap");
                }
            }

            // 3. ViewPager2 Setup
            viewPager = findViewById(R.id.viewPager);
            ViewPagerAdapter adapter = new ViewPagerAdapter(this);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(4);

            // 4. Bottom Navigation Setup
            bottomNav = findViewById(R.id.bottomNavigation);

            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    viewPager.setCurrentItem(0, true);
                    updateToolbarTitle("Dashboard");
                } else if (id == R.id.nav_requests) {
                    viewPager.setCurrentItem(1, true);
                    updateToolbarTitle("Swap Requests");
                } else if (id == R.id.nav_add) {
                    viewPager.setCurrentItem(2, true);
                    updateToolbarTitle("Create Post");
                } else if (id == R.id.nav_profile) {
                    viewPager.setCurrentItem(3, true);
                    updateToolbarTitle("My Profile");
                } else if (id == R.id.nav_history) {
                    viewPager.setCurrentItem(4, true);
                    updateToolbarTitle("History");
                } else if (id == R.id.nav_logout) {
                    handleLogout();
                    return false;
                }
                return true;
            });

            // 5. Swipe Callback
            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (position < 5) {
                        int selectedNavId;
                        switch (position) {
                            case 0:
                                selectedNavId = R.id.nav_home;
                                updateToolbarTitle("Dashboard");
                                break;
                            case 1:
                                selectedNavId = R.id.nav_requests;
                                updateToolbarTitle("Swap Requests");
                                break;
                            case 2:
                                selectedNavId = R.id.nav_add;
                                updateToolbarTitle("Create Post");
                                break;
                            case 3:
                                selectedNavId = R.id.nav_profile;
                                updateToolbarTitle("My Profile");
                                break;
                            case 4:
                            default:
                                selectedNavId = R.id.nav_history;
                                updateToolbarTitle("History");
                                break;
                        }
                        bottomNav.setSelectedItemId(selectedNavId);
                    }
                }
            });

            // --- NAYA BACK PRESS LOGIC (FIXED BRACKETS) ---
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (viewPager != null && viewPager.getCurrentItem() != 0) {
                        viewPager.setCurrentItem(0, true);
                    } else {
                        setEnabled(false);
                        finish();
                    }
                }
            });

        } catch (Exception e) {
            Log.e("HomeActivityError", "Error: " + e.getMessage());
        }
    }

    private void updateToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();
        Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}