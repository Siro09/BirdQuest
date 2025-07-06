package com.example.birdquest;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.birdquest.Managers.AchievementManager;
import com.example.birdquest.Managers.GamificationManager;
import com.example.birdquest.adapters.AchievementAdapter;

public class GamificationActivity extends AppCompatActivity implements AchievementManager.DefinitionsLoadedListener, GamificationManager.UserLoadedListener {
    private static final String TAG = "GamificationActivity";
    private TextView tvUserEmail, tvXp, tvLevel;
    private TextView tvQuizCompletions, tvPerfectQuizScores, tvBirdsIdentified;
    private RecyclerView rvAchievements;
    private TextView tvNoAchievements;
    private Toolbar toolbar;
    private View loadingIndicator;
    private AchievementAdapter achievementAdapter;
    private AchievementManager achievementManager;
    private GamificationManager gamificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gamification);

        // Initialize Views
        toolbar = findViewById(R.id.toolbarGamification);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // For back button
            getSupportActionBar().setTitle("Your Progress"); // Set a title
        }

        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvXp = findViewById(R.id.tvXp);
        tvLevel = findViewById(R.id.tvLevel);
        tvQuizCompletions = findViewById(R.id.tvQuizCompletions);
        tvPerfectQuizScores = findViewById(R.id.tvPerfectQuizScores);
        tvBirdsIdentified = findViewById(R.id.tvBirdsIdentified);
        rvAchievements = findViewById(R.id.rvAchievements);
        tvNoAchievements = findViewById(R.id.tvNoAchievements);



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

    }
    @Override
    public void onDefinitionsLoaded() {
        Log.d(TAG, "Achievement definitions loaded. Now checking achievements.");
        if (achievementManager != null) {
            achievementManager.checkAndAwardAchievements();

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

            int xp = gamificationManager.getXP();
            int level = gamificationManager.getLevel();
            int quizCompletions = gamificationManager.getUserQuizCompletions();
            int perfectQuizScores = gamificationManager.getUserPerfectQuizScores();
            int uniqueCorrectBirdsIdentified = gamificationManager.getUserUniqueCorrectBirdsIdentified();
            String userEmail = gamificationManager.getUserEmail();

            tvXp.setText( String.valueOf(xp));
            tvLevel.setText(String.valueOf(level));
            tvQuizCompletions.setText(String.valueOf(quizCompletions));
            tvPerfectQuizScores.setText(String.valueOf(perfectQuizScores));
            tvBirdsIdentified.setText(String.valueOf(uniqueCorrectBirdsIdentified));
            tvUserEmail.setText(userEmail);


    }

    @Override
    public void onUserLoadFailed() {
        Log.e(TAG, "Failed to load user.");
        Toast.makeText(this, "Could not load user data. Please try again later.", Toast.LENGTH_LONG).show();
        // Handle the failure case, maybe disable achievement-related UI
    }
}
