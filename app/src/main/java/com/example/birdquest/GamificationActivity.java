package com.example.birdquest;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.birdquest.Managers.AchievementManager;
import com.example.birdquest.Managers.GamificationManager;

public class GamificationActivity extends AppCompatActivity implements AchievementManager.DefinitionsLoadedListener, GamificationManager.UserLoadedListener {
    private static final String TAG = "GamificationActivity";
    private TextView tvXp, tvLevel;
    private AchievementManager achievementManager;
    private GamificationManager gamificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamification);

        tvXp = findViewById(R.id.tvXp);
        tvLevel = findViewById(R.id.tvLevel);

        achievementManager = new AchievementManager(this);
        achievementManager.registerDefinitionsLoadedListener(this);

        if (!achievementManager.areDefinitionsLoaded()) {
            achievementManager.loadAllAchievementDefinitions();
        }
        gamificationManager = new GamificationManager();
        gamificationManager.registerUserLoadedListener(this);
        if (!gamificationManager.isUserLoaded()) {
            gamificationManager.loadUser();
        }
        int xp = GamificationManager.getUserXPFirestore(this);
        int level = GamificationManager.getUserLevelFirestore(this);

        tvXp.setText("XP: " + xp);
        tvLevel.setText("Level: " + level);
    }
    @Override
    public void onDefinitionsLoaded() {
        Log.d(TAG, "Achievement definitions loaded. Now checking achievements.");
        if (achievementManager != null) {
            achievementManager.checkAndAwardAchievements();

            tvXp = findViewById(R.id.tvXp);
            tvLevel = findViewById(R.id.tvLevel);
            int xp = gamificationManager.getXP();
            int level = gamificationManager.getLevel();

            tvXp.setText("XP: " + xp);
            tvLevel.setText("Level: " + level);
        }
    }
    @Override
    public void onDefinitionsLoadFailed() {
        Log.e(TAG, "Failed to load achievement definitions.");
        Toast.makeText(this, "Could not load achievement data. Please try again later.", Toast.LENGTH_LONG).show();
        // Handle the failure case, maybe disable achievement-related UI
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister listener to prevent memory leaks
        if (achievementManager != null) {
            achievementManager.unregisterDefinitionsLoadedListener(this);
        }
    }

    @Override
    public void onUserLoaded() {
        Log.d(TAG, "User loaded.");

            tvXp = findViewById(R.id.tvXp);
            tvLevel = findViewById(R.id.tvLevel);
            int xp = gamificationManager.getXP();
            int level = gamificationManager.getLevel();

            tvXp.setText("XP: " + xp);
            tvLevel.setText("Level: " + level);

    }

    @Override
    public void onUserLoadFailed() {
        Log.e(TAG, "Failed to load user.");
        Toast.makeText(this, "Could not load user data. Please try again later.", Toast.LENGTH_LONG).show();
        // Handle the failure case, maybe disable achievement-related UI
    }
}
