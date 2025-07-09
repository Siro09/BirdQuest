
package com.example.birdquest;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.birdquest.R;
import com.example.birdquest.adapters.LeaderboardAdapter;
import com.example.birdquest.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "LeaderboardActivity";

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter leaderboardAdapter;
    private List<User> userList = new ArrayList<>();

    private FirebaseFirestore db;

    private ProgressBar progressBarLeaderboard;
    private TextView tvNoDataLeaderboard;
    private RadioGroup rgSortOptions;

    private String currentSortField = "level"; // Firestore field name for default sort
    private Query.Direction currentSortDirection = Query.Direction.DESCENDING;
    private String currentSortCriteriaDisplay = "Nivel"; // For adapter display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Toolbar toolbar = findViewById(R.id.toolbarLeaderboard);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Clasament");
        }

        db = FirebaseFirestore.getInstance();

        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        progressBarLeaderboard = findViewById(R.id.progressBarLeaderboard);
        tvNoDataLeaderboard = findViewById(R.id.tvNoDataLeaderboard);
        rgSortOptions = findViewById(R.id.rgSortOptions);

        setupRecyclerView();
        setupSortOptions();

        loadLeaderboardData();
    }

    private void setupRecyclerView() {
        leaderboardAdapter = new LeaderboardAdapter();
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(leaderboardAdapter);
        leaderboardAdapter.setCurrentSortCriteria(currentSortCriteriaDisplay);
    }

    private void setupSortOptions() {
        rgSortOptions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSortByLevel) {
                currentSortField = "level"; // Câmpul din Firestore
                currentSortCriteriaDisplay = "Nivel";
            } else if (checkedId == R.id.rbSortByBirds) {
                currentSortField = "uniqueCorrectBirdsIdentifiedCount"; // Câmpul din Firestore
                currentSortCriteriaDisplay = "Păsări";
            } else if (checkedId == R.id.rbSortByQuizzes) {
                currentSortField = "perfectQuizScores"; // Câmpul din Firestore
                currentSortCriteriaDisplay = "Quizuri";
            }
            // Toate sortările sunt DESCENDING pentru clasament
            currentSortDirection = Query.Direction.DESCENDING;
            leaderboardAdapter.setCurrentSortCriteria(currentSortCriteriaDisplay);
            loadLeaderboardData(); // Reîncarcă datele cu noua sortare
        });
    }

    private void loadLeaderboardData() {
        showLoading(true);
        userList.clear();


        // Și câmpurile: level, uniqueBirdsIdentifiedCount, perfectQuizScoresCount
        db.collection("users")
                .orderBy(currentSortField, currentSortDirection)
                .limit(100) // Limitează la a primii 100 pentru performanță
                .get()
                .addOnCompleteListener(task -> {
                    showLoading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        if (task.getResult().isEmpty()) {
                            showNoDataMessage(true);
                        } else {
                            showNoDataMessage(false);
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                User user = document.toObject(User.class);

                                User mappedUser = new User();
                                mappedUser.setEmail(document.getString("email"));
                                if (document.getLong("level") != null) {
                                    mappedUser.setLevel(document.getLong("level").intValue());
                                }
                                if (document.getLong("uniqueCorrectBirdsIdentifiedCount") != null) {

                                    mappedUser.setUniqueCorrectBirdsIdentifiedCount(document.getLong("uniqueCorrectBirdsIdentifiedCount").intValue());
                                }
                                if (document.getLong("perfectQuizScores") != null) {
                                    mappedUser.setPerfectQuizScores(document.getLong("perfectQuizScores").intValue());
                                }
                                userList.add(mappedUser);
                            }
                            leaderboardAdapter.setUsers(userList);
                        }
                    } else {
                        Log.e(TAG, "Error getting leaderboard documents: ", task.getException());
                        Toast.makeText(LeaderboardActivity.this, "Eroare la încărcarea clasamentului.", Toast.LENGTH_SHORT).show();
                        showNoDataMessage(true);
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBarLeaderboard.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
            tvNoDataLeaderboard.setVisibility(View.GONE);
        } else {
            progressBarLeaderboard.setVisibility(View.GONE);
            rvLeaderboard.setVisibility(View.VISIBLE);
        }
    }

    private void showNoDataMessage(boolean show) {
        if (show) {
            tvNoDataLeaderboard.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
        } else {
            tvNoDataLeaderboard.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Sau finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}