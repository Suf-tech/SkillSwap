package com.example.skillswap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.topToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SkillSwap Dashboard");
        }

        viewPager = findViewById(R.id.viewPager);
        bottomNav = findViewById(R.id.bottomNavigation);

        // 1. Attach the Adapter to ViewPager
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // 2. Handle Bottom Nav Taps
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    viewPager.setCurrentItem(0, true);
                    return true;
                } else if (id == R.id.nav_requests) {
                    viewPager.setCurrentItem(1, true);
                    return true;
                } else if (id == R.id.nav_add) {
                    viewPager.setCurrentItem(2, true);
                    return true;
                } else if (id == R.id.nav_profile) {
                    viewPager.setCurrentItem(3, true);
                    return true;
                } else if (id == R.id.nav_logout) {
                    handleLogout();
                    return false; // Don't highlight the logout icon
                }
                return false;
            }
        });

        // 3. Handle Screen Swipes to update Bottom Nav icon
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                        break;
                    case 1:
                        bottomNav.getMenu().findItem(R.id.nav_requests).setChecked(true);
                        break;
                    case 2:
                        bottomNav.getMenu().findItem(R.id.nav_add).setChecked(true);
                        break;
                    case 3:
                        bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
                        break;
                }
            }
        });
    }

    private void handleLogout() {
        SharedPreferences sp = getSharedPreferences("UserSession", MODE_PRIVATE);
        sp.edit().clear().apply();
        Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}