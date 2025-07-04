package com.example.birdquest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.birdquest.quiz.QuizActivity;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth

public class MainActivity extends AppCompatActivity {

    private Button btnQuiz, btnBirdDex, btnGamification;
    private Button btnLogout; // Declare btnLogout here
    private FirebaseAuth mAuth; // Declare FirebaseAuth instance

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

        btnQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                startActivity(intent);
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
                // You might also want to clear other user-specific data from SharedPreferences here
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
}