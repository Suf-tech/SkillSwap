package com.example.skillswap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class AdminSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Aapki XML (fragment_admin_settings.xml) ko inflate kar raha hai
        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Toolbar ka title update karein taake Admin ko pata chale wo kis section mein hai
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle("Admin Settings");
            }
        }
    }
}