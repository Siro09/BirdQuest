package com.example.birdquest;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.birdquest.GamificationManager;

public class GamificationActivity extends AppCompatActivity {

    private TextView tvXp, tvLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamification);

        tvXp = findViewById(R.id.tvXp);
        tvLevel = findViewById(R.id.tvLevel);

        int xp = GamificationManager.getXP(this);
        int level = GamificationManager.getLevel(this);

        tvXp.setText("XP: " + xp);
        tvLevel.setText("Level: " + level);
    }
}
