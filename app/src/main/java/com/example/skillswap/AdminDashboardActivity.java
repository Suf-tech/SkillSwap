package com.example.skillswap;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNavigation);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_admin_overview) {
                    switchFragment(new AdminOverviewFragment());
                    return true;
                } else if (id == R.id.nav_admin_users) {
                    switchFragment(new AdminUsersFragment());
                    return true;
                } else if (id == R.id.nav_admin_reports) {
                    switchFragment(new AdminReportsFragment());
                    return true;
                } else if (id == R.id.nav_admin_settings) {
                    switchFragment(new AdminSettingsFragment());
                    return true;
                } else if (id == R.id.nav_admin_logout) {
                    Intent intent = new Intent(AdminDashboardActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            }
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_admin_overview);
            switchFragment(new AdminOverviewFragment());
        }
    }

    private void switchFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(com.example.skillswap.R.id.adminFragmentContainer, fragment)
                .commit();
    }
}


