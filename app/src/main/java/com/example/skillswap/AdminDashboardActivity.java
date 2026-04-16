package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    // Variable declaration (Scannable for Android Studio)
    private Toolbar adminToolbar;
    private BottomNavigationView adminBottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        try {
            // 1. Toolbar Setup (ID: adminToolbar)
            adminToolbar = findViewById(R.id.adminToolbar);
            if (adminToolbar != null) {
                setSupportActionBar(adminToolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Admin Dashboard");
                }
            }

            // 2. Bottom Navigation Setup (ID: adminBottomNavigation)
            adminBottomNav = findViewById(R.id.adminBottomNavigation);

            if (adminBottomNav != null) {
                adminBottomNav.setOnItemSelectedListener(item -> {
                    Fragment selectedFragment = null;
                    int id = item.getItemId();

                    // --- ID Check Logic ---
                    // NOTE: Agar 'nav_admin_reports' error de, to check karein ke
                    // res/menu/bottom_nav_admin.xml mein yehi ID use hui hai ya nahi.

                    if (id == R.id.nav_admin_overview) {
                        selectedFragment = new AdminOverviewFragment();
                    } else if (id == R.id.nav_admin_users) {
                        selectedFragment = new AdminUsersFragment();
                    } else if (id == R.id.nav_admin_logout) {
                        handleLogout();
                        return true;
                    } else {
                        // Agar koi aur button (Settings/Reports) press ho jo abhi nahi bana
                        // to default Overview pe hi rakhen crash se bachne ke liye
                        selectedFragment = new AdminOverviewFragment();
                    }

                    if (selectedFragment != null) {
                        switchFragment(selectedFragment);
                        return true;
                    }
                    return false;
                });

                // Default Fragment: Jab Admin pehli baar Login kare
                if (savedInstanceState == null) {
                    adminBottomNav.setSelectedItemId(R.id.nav_admin_overview);
                    switchFragment(new AdminOverviewFragment());
                }
            }

        } catch (Exception e) {
            Log.e("AdminDashError", "Initialization failed: " + e.getMessage());
            Toast.makeText(this, "Error loading admin layout", Toast.LENGTH_SHORT).show();
        }
    }

    private void switchFragment(Fragment fragment) {
        // FrameLayout ID: adminFragmentContainer (Matching your XML)
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.adminFragmentContainer, fragment)
                    .commit();
        }
    }

    private void handleLogout() {
        // Firebase SignOut
        FirebaseAuth.getInstance().signOut();

        // Local Session Clear
        getSharedPreferences("UserSession", MODE_PRIVATE).edit().clear().apply();

        Toast.makeText(this, "Admin Logged Out", Toast.LENGTH_SHORT).show();

        // Go back to Login
        Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}