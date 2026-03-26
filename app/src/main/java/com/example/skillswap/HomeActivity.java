package com.example.skillswap;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Skill> skillList;
    SkillAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        skillList = new ArrayList<>();

        // Dummy Data (Ye list mein show hoga)
        skillList.add(new Skill("Learn Java", "Sara"));
        skillList.add(new Skill("Learn Photoshop", "Ali"));
        skillList.add(new Skill("Learn English", "Usman"));
        skillList.add(new Skill("Graphic Design", "Ahmed"));
        skillList.add(new Skill("Web Development", "Zain"));

        adapter = new SkillAdapter(this, skillList);
        recyclerView.setAdapter(adapter);
    }
}