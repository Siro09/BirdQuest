package com.example.birdquest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.birdquest.adapters.BirdAdapter;
import com.example.birdquest.db.AppDatabase;
import com.example.birdquest.models.Bird;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BirdDexActivity extends AppCompatActivity {

    private static final String TAG = "BirdDexActivity";

    private RecyclerView recyclerViewBirdDex;
    private BirdAdapter adapter;
    private AppDatabase db; // AppDatabase instance
    // private BirdDao birdDao; // If not using LiveData directly from DB instance
    private SearchView searchViewBirdDex;
    // Using a simple Executor for background tasks if not using ViewModel with LiveData from DAO
    private final ExecutorService databaseReadExecutor = Executors.newSingleThreadExecutor();
    private LiveData<List<Bird>> currentBirdsLiveData;
    private Observer<List<Bird>> birdsObserver;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_birddex); //  layout file for this activity

        searchViewBirdDex = findViewById(R.id.searchViewBirdDex);
        recyclerViewBirdDex = findViewById(R.id.recyclerViewBirdDex);
        recyclerViewBirdDex.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BirdAdapter(this); // Pass context to adapter for Glide
        recyclerViewBirdDex.setAdapter(adapter);

        // Get database instance
        db = AppDatabase.getInstance(getApplicationContext());
        // birdDao = db.birdDao(); // If you prefer to get DAO instance here

        loadBirds("");

        setupSearchView();
    }

    private void loadBirds(String query) {
        // If there's an existing observer, remove it to avoid multiple observers
        // on different LiveData objects.
        if (currentBirdsLiveData != null && birdsObserver != null) {
            currentBirdsLiveData.removeObserver(birdsObserver);
        }

        birdsObserver = new Observer<List<Bird>>() {
            @Override
            public void onChanged(List<Bird> birds) {
                if (birds != null) {
                    Log.d(TAG, "Birds loaded/updated: " + birds.size() + " for query: '" + query + "'");
                    adapter.setBirds(birds); // Update the adapter with the new list
                    if (birds.isEmpty() && !query.isEmpty()) {
                        Toast.makeText(BirdDexActivity.this, "No birds found matching '" + query + "'", Toast.LENGTH_SHORT).show();
                    } else if (birds.isEmpty() && query.isEmpty()) {
                        Toast.makeText(BirdDexActivity.this, "No birds found in the database.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "Birds list is null from LiveData for query: '" + query + "'");
                }
            }
        };
        if (query == null || query.trim().isEmpty()) {
            currentBirdsLiveData = db.birdDao().getAllBirdsLiveDataImageASC(); // Use your method for all birds
        } else {
            // Add wildcards for LIKE query
            String searchQuery = query.trim() ;
            currentBirdsLiveData = db.birdDao().searchBirdsByNameOrSpecies(searchQuery);
        }
        currentBirdsLiveData.observe(this, birdsObserver);
    }


    private void setupSearchView() {
        searchViewBirdDex.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // User pressed search button or enter
                loadBirds(query);
                searchViewBirdDex.clearFocus(); // Optional: Hide keyboard
                return true; // Indicates we've handled the action
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the user types each character
                loadBirds(newText);
                return true; // Indicates we've handled the action
            }
        });

        // Optional: Handle the close button (X) on the SearchView
        searchViewBirdDex.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                loadBirds(""); // Load all birds when search is closed
                return false; // Let the SearchView perform its default close action
            }
        });

        //  Set  query hint
        searchViewBirdDex.setQueryHint("Search birds by name or species...");
    }
    // It's good practice to shutdown the executor when the activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If you use databaseReadExecutor , uncomment the following:
        /*
        if (databaseReadExecutor != null && !databaseReadExecutor.isShutdown()) {
            databaseReadExecutor.shutdown();
        }
        */
    }
}