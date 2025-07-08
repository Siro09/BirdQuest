// AchievementManager.java
package com.example.birdquest.Managers; // Or your preferred package

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.birdquest.models.Achievement;
import com.example.birdquest.models.IdentifiedBird;
import com.example.birdquest.models.User; // Your User model
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AchievementManager {

    private static final String TAG = "AchievementManager";
    private static final String ACHIEVEMENTS_COLLECTION = "achievements";
    private static final String USERS_COLLECTION = "users";
    private static final String IDENTIFIED_BIRDS_SUBCOLLECTION = "uniqueCorrectBirdsIdentified";
    private static final String UNLOCKED_ACHIEVEMENTS_SUBCOLLECTION = "unlockedAchievements";

    // Criteria Types (constants for consistency)
    public static final String CRITERIA_QUIZ_COMPLETIONS = "QUIZ_COMPLETIONS";
    public static final String CRITERIA_UNIQUE_BIRDS_IDENTIFIED = "UNIQUE_BIRDS_IDENTIFIED";
    public static final String CRITERIA_PERFECT_QUIZZES = "PERFECT_QUIZZES";
    // Add more criteria types as needed

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context; // For Toasts

    private List<Achievement> allAchievementDefinitions; // Cache

    private boolean definitionsLoaded = false;
    private List<DefinitionsLoadedListener> listeners = new ArrayList<>();
    public interface DefinitionsLoadedListener {
        void onDefinitionsLoaded();
        void onDefinitionsLoadFailed();
    }
    public AchievementManager(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();
        this.context = context.getApplicationContext(); // Use application context to avoid leaks
        this.allAchievementDefinitions = new ArrayList<>();

    }
    public void registerDefinitionsLoadedListener(DefinitionsLoadedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // If definitions are already loaded, notify immediately
        if (definitionsLoaded) {
            listener.onDefinitionsLoaded();
        } else if (allAchievementDefinitions.isEmpty() && listeners.size() == 1) {
            // Start loading only when the first listener is registered or if explicitly called
            loadAllAchievementDefinitions();
        }
    }
    public void unregisterDefinitionsLoadedListener(DefinitionsLoadedListener listener) {
        listeners.remove(listener);
    }
    private void notifyDefinitionsLoaded() {
        for (DefinitionsLoadedListener listener : listeners) {
            listener.onDefinitionsLoaded();
        }
    }
    private void notifyDefinitionsLoadFailed() {
        for (DefinitionsLoadedListener listener : listeners) {
            listener.onDefinitionsLoadFailed();
        }
    }
    public void loadAllAchievementDefinitions() {
        if (definitionsLoaded && !allAchievementDefinitions.isEmpty()) {
            Log.d(TAG, "Definitions already loaded. Not reloading.");
            notifyDefinitionsLoaded(); // Notify any new listeners
            return;
        }
        Log.d(TAG, "Starting to load achievement definitions...");
        db.collection(ACHIEVEMENTS_COLLECTION).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        allAchievementDefinitions.clear();
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            Achievement definition = doc.toObject(Achievement.class);
                            if (definition != null) {
                                definition.setId(doc.getId());
                                allAchievementDefinitions.add(definition);
                            }
                        }
                        definitionsLoaded = true;
                        Log.d(TAG, "Loaded " + allAchievementDefinitions.size() + " achievement definitions.");
                        notifyDefinitionsLoaded();
                    } else {
                        definitionsLoaded = false; // Mark as not loaded on failure
                        Log.e(TAG, "Error loading achievement definitions: ", task.getException());
                        notifyDefinitionsLoadFailed();
                    }
                });
    }
    public boolean areDefinitionsLoaded() {
        return definitionsLoaded;
    }

    /**
     * Call this method after a relevant user action (e.g., completing a quiz, identifying a bird).
     * It fetches the latest user data and checks all achievements.
     */
    public void checkAndAwardAchievements() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "User not logged in. Cannot check achievements.");
            return;
        }
        if (allAchievementDefinitions.isEmpty()) {
            Log.w(TAG, "Achievement definitions not loaded yet. Retrying load or skipping check.");
            // Attempt to reload if empty, but be mindful of frequent reloads.
            // Consider a flag to prevent too many reload attempts in a short period.
            loadAllAchievementDefinitions();
            return;
        }

        String userId = firebaseUser.getUid();
        DocumentReference userRef = db.collection(USERS_COLLECTION).document(userId);

        userRef.get().addOnSuccessListener(userDocument -> {
            if (userDocument.exists()) {
                User user = userDocument.toObject(User.class);

                if (user == null) {
                    Log.e(TAG, "User object is null after fetching document for UID: " + userId);
                    return;
                }
                // Pass the user object and its ID
                checkAchievementsForUser(user, userId);
            } else {
                Log.w(TAG, "User document does not exist for UID: " + userId + ". Cannot check achievements.");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user for achievement check (UID: " + userId + "): ", e));
    }


    // In AchievementManager.java, inside checkAchievementsForUser method

    private void checkAchievementsForUser(@NonNull User user, @NonNull String userId) {
        // Task to get already unlocked achievements
        Task<QuerySnapshot> unlockedAchievementsTask = db.collection(USERS_COLLECTION).document(userId)
                .collection(UNLOCKED_ACHIEVEMENTS_SUBCOLLECTION).get();

        // Task to get the count of identified birds
        Task<QuerySnapshot> identifiedBirdsTask = db.collection(USERS_COLLECTION).document(userId)
                .collection(IDENTIFIED_BIRDS_SUBCOLLECTION).get(); // This gets all documents

        // Use Tasks.whenAllSuccess to wait for both tasks to complete
        Tasks.whenAllSuccess(unlockedAchievementsTask, identifiedBirdsTask)
                .addOnSuccessListener(results -> {
                    // Result 0 is from unlockedAchievementsTask
                    QuerySnapshot unlockedAchievementsSnapshot = (QuerySnapshot) results.get(0);
                    List<String> unlockedAchievementIds = new ArrayList<>();
                    if (unlockedAchievementsSnapshot != null) {
                        for (DocumentSnapshot doc : unlockedAchievementsSnapshot.getDocuments()) {
                            unlockedAchievementIds.add(doc.getId());
                        }
                    }

                    // Result 1 is from identifiedBirdsTask
                    QuerySnapshot identifiedBirdsSnapshot = (QuerySnapshot) results.get(1);

                    ArrayList<IdentifiedBird> birdsIdentified =new ArrayList<>();
                    assert identifiedBirdsSnapshot != null;
                    for (DocumentSnapshot doc : identifiedBirdsSnapshot.getDocuments()) {
                        IdentifiedBird bird = doc.toObject(IdentifiedBird.class);
                        birdsIdentified.add(bird);
                    }
                    // Update the user object with the fetched count.
                    // Make sure your User model has a method like setUniqueBirdsIdentifiedCount()
                    // or that you update the relevant field directly if it's public.
                    user.setUniqueCorrectBirdsIdentified(birdsIdentified); // Assuming User.java has this setter

                    Log.d(TAG, "User " + userId + " has identified " + birdsIdentified.size() + " unique birds.");

                    // Now that we have the user object updated with the bird count,
                    // proceed with checking all achievement definitions.
                    WriteBatch batch = db.batch();
                    boolean newAchievementAwarded = false;
                    long totalXpFromNewAchievements = 0;

                    for (Achievement definition : allAchievementDefinitions) {
                        if (!unlockedAchievementIds.contains(definition.getId())) { // Check if already unlocked
                            boolean criteriaMet = false;
                            switch (definition.getCriteriaType()) {
                                case CRITERIA_QUIZ_COMPLETIONS:
                                    criteriaMet = user.getQuizCompletions() >= definition.getCriteriaValue();
                                    break;
                                case CRITERIA_UNIQUE_BIRDS_IDENTIFIED:
                                    // Now use the count from the user object, which we just updated
                                    criteriaMet = user.getUniqueCorrectBirdsCount() >= definition.getCriteriaValue();
                                    break;
                                case CRITERIA_PERFECT_QUIZZES:
                                    criteriaMet = user.getPerfectQuizScores() >= definition.getCriteriaValue();
                                    break;
                                // Add more cases for other criteria types
                                default:
                                    Log.w(TAG, "Unknown criteria type for achievement '" + definition.getName() + "': " + definition.getCriteriaType());
                                    break;
                            }

                            if (criteriaMet) {
                                Log.i(TAG, "User '" + userId + "' met criteria for achievement: " + definition.getName());
                                awardAchievement(batch, userId, new Achievement(definition, new Date())); // awardAchievement uses AchievementDefinition
                                newAchievementAwarded = true;
                                totalXpFromNewAchievements += definition.getXpReward();
                            }
                        }
                    }

                    if (newAchievementAwarded) {
                        if (totalXpFromNewAchievements > 0) {
                            GamificationManager.addXP(context, (int) totalXpFromNewAchievements);
                            Log.d(TAG, "Batching XP increment by " + totalXpFromNewAchievements );
                        }

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Successfully awarded new achievements, updated XP, and bird count for user: " + userId);
                                    Toast.makeText(context, "New achievement(s) unlocked!", Toast.LENGTH_LONG).show();
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to commit batch for awarding achievements for user: " + userId, e));
                    } else {
                        Log.d(TAG, "No new achievements to award for user: " + userId);
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get unlocked achievements or identified birds count for UID: " + userId, e);
                });
    }

    /**
     * Adds operations to the batch to award a specific achievement to a user.
     * This creates a new document in the user's 'unlockedAchievements' subcollection.
     *
     * @param batch      The Firestore WriteBatch to add operations to.
     * @param userId     The ID of the user.
     * @param definition The definition of the achievement being awarded.
     */
    private void awardAchievement(WriteBatch batch, String userId, Achievement definition) {
        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(userId);
        DocumentReference newUnlockedAchievementRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(UNLOCKED_ACHIEVEMENTS_SUBCOLLECTION)
                .document(definition.getId());
        batch.set(newUnlockedAchievementRef, definition);
        Log.d(TAG, "Achievement unlocked: " + definition.getName());
    }


}