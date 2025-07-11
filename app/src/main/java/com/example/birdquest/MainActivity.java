package com.example.birdquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.birdquest.quiz.QuizActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button btnQuiz, btnBirdDex, btnGamification,btnAddBird;
    private Button btnLogout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        btnQuiz = findViewById(R.id.btnQuiz);
        btnBirdDex = findViewById(R.id.btnBirdDex);
        btnGamification = findViewById(R.id.btnGamification);
        btnLogout = findViewById(R.id.btnLogout); // Initialize btnLogout
        btnAddBird = findViewById(R.id.btnAddBird);

        btnAddBird.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddBirdActivity.class);
                startActivity(intent);
            }
        });
        btnQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showQuizModeDialog();
            }
        });

        btnBirdDex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BirdDexActivity.class);
                startActivity(intent);
            }
        });

        btnGamification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GamificationActivity.class);
                startActivity(intent);
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() { // Changed to new View.OnClickListener
            @Override
            public void onClick(View v) {
                // 1. Sign out from Firebase
                mAuth.signOut();

                // 2. Clear local session data (SharedPreferences) - this is still good practice
                SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("logged_in", false);
                // might also want to clear other user-specific data from SharedPreferences here
                // editor.remove("user_email"); // Example
                // editor.remove("user_id");   // Example
                editor.apply();

                // 3. Navigate to LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                // Add flags to clear the back stack and prevent the user from navigating back
                // to MainActivity after logging out.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Call finish to destroy MainActivity
            }
        });
    }
    private void showQuizModeDialog() {
        final String[] quizModes = {"Normal (Image Quiz)", "Hard (Sound Quiz)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Quiz Mode");
        builder.setItems(quizModes, (dialog, which) -> {
            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
            if (which == 0) { // Normal Mode
                intent.putExtra("QUIZ_MODE", "NORMAL");
            } else { // Hard Mode
                intent.putExtra("QUIZ_MODE", "HARD");
            }
            startActivity(intent);
        });
        builder.show();
    }
}