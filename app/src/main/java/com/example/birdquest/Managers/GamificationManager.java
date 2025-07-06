package com.example.birdquest.Managers;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Added for optional callback

import com.example.birdquest.R;
import com.example.birdquest.models.Achievement; // Keep if used by User model
import com.example.birdquest.models.IdentifiedBird;
import com.example.birdquest.models.User; // Keep if used for caching, though not directly in these static methods
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
// import java.util.Map; // Not used in the provided snippet

public class GamificationManager {
    private static final String TAG = "GamificationManager";
    private static final String USERS_COLLECTION = "users";
    private static final String UNLOCKED_ACHIEVEMENTS_SUBCOLLECTION = "unlockedAchievements";
    // private static final String PREFS_NAME = "GamificationPrefs";
    private static final String KEY_XP = "xp";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_QUIZ_COMPLETIONS = "quizCompletions";
    private static final String KEY_PERFECT_QUIZ_SCORES = "perfectQuizScores";
    private static final String IDENTIFIED_BIRDS_SUBCOLLECTION = "uniqueCorrectBirdsIdentified";

    private User user; // Cache

    private boolean userLoaded = false;
    private List<GamificationManager.UserLoadedListener> listeners = new ArrayList<>();
    public int getXP() {
        return user.getXp();
    }
    public int getLevel() {
        return user.getLevel();
    }
    public String getUserEmail()
    {
        return user.getEmail();
    }
    public int getUserQuizCompletions()
    {
        return user.getQuizCompletions();
    }
    public int getUserPerfectQuizScores()
    {
        return user.getPerfectQuizScores();
    }
    public int getUserUniqueCorrectBirdsIdentified()
    {
        return user.getUniqueCorrectBirdsCount();
    }
    public Collection<Achievement> getUserAchievements()
    {
        return  user.getAchievements();
    }

    // --- Callback Interfaces ---
    public interface XpCallback {
        void onXpReceived(int xp);
        void onError(Exception e);
    }

    public interface LevelCallback {
        void onLevelReceived(int level);
        void onError(Exception e);
    }

    public interface QuizCompletionsCallback {
        void onQuizCompletionsReceived(int count);
        void onError(Exception e);
    }

    public interface PerfectQuizScoresCallback {
        void onPerfectQuizScoresReceived(int count);
        void onError(Exception e);
    }

    public interface UniqueBirdsCountCallback {
        void onUniqueBirdsCountReceived(int count);
        void onError(Exception e);
    }

    // Optional: For write operations if you need to know when they complete
    public interface WriteOperationCallback {
        void onSuccess();
        void onError(Exception e);
    }


    // --- Methods to GET data with Callbacks ---

    public static void getUserXP(Context context, XpCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Long xpLong = document.getLong(KEY_XP);
                                if (xpLong != null) {
                                    callback.onXpReceived(xpLong.intValue());
                                } else {
                                    Log.w(TAG, "XP field is null or missing for UID: " + currentUser.getUid());
                                    callback.onXpReceived(0); // Default or specific error handling
                                }
                            } else {
                                Log.w(TAG, "User document does not exist for UID: " + currentUser.getUid());
                                callback.onXpReceived(0); // Default or specific error handling
                            }
                        } else {
                            Log.e(TAG, "Error getting user XP: ", task.getException());
                            callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null, cannot fetch XP.");
            callback.onError(new Exception("User not logged in"));
        }
    }

    public static void getUserLevel(Context context, LevelCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Long levelLong = document.getLong(KEY_LEVEL);
                                if (levelLong != null) {
                                    callback.onLevelReceived(levelLong.intValue());
                                } else {
                                    Log.w(TAG, "Level field is null or missing for UID: " + currentUser.getUid());
                                    callback.onLevelReceived(1); // Default level
                                }
                            } else {
                                Log.w(TAG, "User document does not exist for UID: " + currentUser.getUid());
                                callback.onLevelReceived(1); // Default level
                            }
                        } else {
                            Log.e(TAG, "Error getting user level: ", task.getException());
                            callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null, cannot fetch level.");
            callback.onError(new Exception("User not logged in"));
        }
    }


    public static void getQuizCompletions(Context context, QuizCompletionsCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Long quizCompletionsLong = document.getLong(KEY_QUIZ_COMPLETIONS);
                                if (quizCompletionsLong != null) {
                                    callback.onQuizCompletionsReceived(quizCompletionsLong.intValue());
                                } else {
                                    Log.w(TAG, KEY_QUIZ_COMPLETIONS + " field is null or missing for UID: " + currentUser.getUid());
                                    callback.onQuizCompletionsReceived(0);
                                }
                            } else {
                                Log.w(TAG, "User document does not exist for UID: " + currentUser.getUid());
                                callback.onQuizCompletionsReceived(0);
                            }
                        } else {
                            Log.e(TAG, "Error getting " + KEY_QUIZ_COMPLETIONS + ": ", task.getException());
                            callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null, cannot fetch " + KEY_QUIZ_COMPLETIONS);
            callback.onError(new Exception("User not logged in"));
        }
    }

    public static void getPerfectQuizScores(Context context, PerfectQuizScoresCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Long perfectScoresLong = document.getLong(KEY_PERFECT_QUIZ_SCORES);
                                if (perfectScoresLong != null) {
                                    callback.onPerfectQuizScoresReceived(perfectScoresLong.intValue());
                                } else {
                                    Log.w(TAG, KEY_PERFECT_QUIZ_SCORES + " field is null or missing for UID: " + currentUser.getUid());
                                    callback.onPerfectQuizScoresReceived(0);
                                }
                            } else {
                                Log.w(TAG, "User document does not exist for UID: " + currentUser.getUid());
                                callback.onPerfectQuizScoresReceived(0);
                            }
                        } else {
                            Log.e(TAG, "Error getting " + KEY_PERFECT_QUIZ_SCORES + ": ", task.getException());
                            callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null, cannot fetch " + KEY_PERFECT_QUIZ_SCORES);
            callback.onError(new Exception("User not logged in"));
        }
    }

    public static void getUniqueCorrectBirdsIdentifiedCount(Context context, UniqueBirdsCountCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .collection(IDENTIFIED_BIRDS_SUBCOLLECTION)
                    .whereEqualTo("correctlyIdentified", true) // Assuming you have such a field
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null) {
                                callback.onUniqueBirdsCountReceived(querySnapshot.size());
                            } else {
                                // This case should ideally not happen if task is successful
                                callback.onUniqueBirdsCountReceived(0);
                            }
                        } else {
                            Log.e(TAG, "Error getting unique correct birds identified count: ", task.getException());
                            callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null, cannot fetch unique correct birds identified count.");
            callback.onError(new Exception("User not logged in"));
        }
    }


    // --- Methods to SET data (can also have optional callbacks) ---

    public static void setQuizCompletionsFirestore(int quizCompletions, @Nullable WriteOperationCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update(KEY_QUIZ_COMPLETIONS, quizCompletions)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User " + KEY_QUIZ_COMPLETIONS + " updated successfully.");
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Error updating user " + KEY_QUIZ_COMPLETIONS + ": ", task.getException());
                            if (callback != null) callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null. Cannot update " + KEY_QUIZ_COMPLETIONS);
            if (callback != null) callback.onError(new Exception("User not logged in"));
        }
    }

    public static void setUniqueCorrectBirdsIdentifiedFirestore(String uniqueCorrectBirdsIdentified, @Nullable WriteOperationCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Assuming IdentifiedBird model has a field like 'correctlyIdentified'
        IdentifiedBird birdToInsert = new IdentifiedBird(true); // Example: true means correctly identified
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid()).collection(IDENTIFIED_BIRDS_SUBCOLLECTION)
                    .document(uniqueCorrectBirdsIdentified).set(birdToInsert) // Using bird's name/ID as document ID
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User " + IDENTIFIED_BIRDS_SUBCOLLECTION + " updated successfully for bird: " + uniqueCorrectBirdsIdentified);
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Error updating user " + IDENTIFIED_BIRDS_SUBCOLLECTION + ": ", task.getException());
                            if (callback != null) callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null. Cannot update " + IDENTIFIED_BIRDS_SUBCOLLECTION);
            if (callback != null) callback.onError(new Exception("User not logged in"));
        }
    }

    public static void setPerfectQuizScoresFirestore(int perfectQuizScores, @Nullable WriteOperationCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update(KEY_PERFECT_QUIZ_SCORES, perfectQuizScores)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User " + KEY_PERFECT_QUIZ_SCORES + " updated successfully.");
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Error updating user " + KEY_PERFECT_QUIZ_SCORES + ": ", task.getException());
                            if (callback != null) callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null. Cannot update " + KEY_PERFECT_QUIZ_SCORES);
            if (callback != null) callback.onError(new Exception("User not logged in"));
        }
    }

    public static void setLevelFirestore(int level, @Nullable WriteOperationCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update(KEY_LEVEL, level)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User level updated successfully.");
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Error updating user level: ", task.getException());
                            if (callback != null) callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null. Cannot update level.");
            if (callback != null) callback.onError(new Exception("User not logged in"));
        }
    }

    public static void setXPFirestore(int xp, @Nullable WriteOperationCallback callback) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update(KEY_XP, xp)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User xp updated successfully.");
                            if (callback != null) callback.onSuccess();
                        } else {
                            Log.e(TAG, "Error updating user xp: ", task.getException());
                            if (callback != null) callback.onError(task.getException());
                        }
                    });
        } else {
            Log.w(TAG, "Current user is null. Cannot update XP.");
            if (callback != null) callback.onError(new Exception("User not logged in"));
        }
    }


    // --- Main Logic Method (add) using Callbacks ---
    /**
     * Increments the user's quiz completions count by 1.
     *
     * @param context  The application context.
     */
    public static void addQuizCompletions(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        getQuizCompletions(context, new QuizCompletionsCallback() {
            @Override
            public void onQuizCompletionsReceived(int currentCompletions) {
                int newCompletions = currentCompletions + 1;
                setQuizCompletionsFirestore(newCompletions, new WriteOperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Quiz completions incremented to " + newCompletions);
                        // You could add a Toast here if desired
                        // Toast.makeText(context, "Quiz completed!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to set new quiz completions count.", e);

                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to get current quiz completions to increment.", e);

            }
        });
    }
    /**
     * Increments the user's perfect quiz scores count by 1.
     *
     * @param context  The application context.

     */
    public static void addPerfectQuizScores(Context context) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        getPerfectQuizScores(context, new PerfectQuizScoresCallback() {
            @Override
            public void onPerfectQuizScoresReceived(int currentPerfectScores) {
                int newPerfectScores = currentPerfectScores + 1;
                setPerfectQuizScoresFirestore(newPerfectScores, new WriteOperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Perfect quiz scores incremented to " + newPerfectScores);
                        // You could add a Toast here if desired
                        // Toast.makeText(context, "Perfect quiz score!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to set new perfect quiz scores count.", e);

                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to get current perfect quiz scores to increment.", e);

            }
        });
    }
    /**
     * Adds a bird to the user's list of uniquely identified correct birds.
     * If the bird is new for the user, it also awards XP.
     *
     * @param context        The application context.
     * @param birdIdentifier A unique string identifying the bird (e.g., common name, scientific name, or a unique ID).
     *                       This will be used as the document ID in the subcollection.
     */
    public static void addUniqueCorrectBirdsIdentified(Context context, String birdIdentifier) {

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                // If not already identified, then add it
                FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
                // Assuming IdentifiedBird model has a field like 'correctlyIdentified' and a timestamp
                IdentifiedBird birdData = new IdentifiedBird(true); // true for correctlyIdentified
                // You could add more data to IdentifiedBird, like a timestamp:
                // birdData.setTimestamp(System.currentTimeMillis());
                firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                        .collection(IDENTIFIED_BIRDS_SUBCOLLECTION).document(birdIdentifier)
                        .set(birdData)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Bird '" + birdIdentifier + "' added to " + IDENTIFIED_BIRDS_SUBCOLLECTION + " for user " + currentUser.getUid());
                                Toast.makeText(context, birdIdentifier + " added to your identified list!", Toast.LENGTH_SHORT).show();

                            } else {
                                Log.e(TAG, "Error adding bird '" + birdIdentifier + "' to " + IDENTIFIED_BIRDS_SUBCOLLECTION + ": ", task.getException());

                            }
                        });

    }
    public static void addXP(Context context, int amount) {
        getUserXP(context, new XpCallback() {
            @Override
            public void onXpReceived(int currentXP) {
                int newXP = currentXP + amount;
                final boolean[] leveledUp = {false}; // Use array to modify in inner class

                Toast.makeText(context, "You gained " + amount + " XP!", Toast.LENGTH_SHORT).show();

                getUserLevel(context, new LevelCallback() {
                    @Override
                    public void onLevelReceived(int currentLevel) {
                        int newLevel = currentLevel;
                        int tempNewXP = newXP; // Use a temporary variable for XP calculation during leveling

                        while (tempNewXP >= 50) { // Assuming 50 XP per level
                            newLevel++;
                            tempNewXP -= 50;
                            leveledUp[0] = true;
                        }

                        final int finalNewXP = tempNewXP; // XP after leveling
                        final int finalNewLevel = newLevel;

                        if (leveledUp[0]) {
                            setLevelFirestore(finalNewLevel, new WriteOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    MediaPlayer mp = MediaPlayer.create(context, R.raw.success);
                                    mp.setOnCompletionListener(MediaPlayer::release); // Release when done
                                    mp.start();
                                    Toast.makeText(context, "Level up! New level " + finalNewLevel, Toast.LENGTH_SHORT).show();
                                    // Now set the final XP after level up
                                    setXPFirestore(finalNewXP, new WriteOperationCallback() {
                                        @Override
                                        public void onSuccess() {
                                            Log.d(TAG, "XP updated successfully after level up.");
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            Log.e(TAG, "Error setting XP after level up.", e);
                                        }
                                    });
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "Error setting level.", e);
                                    // Still try to set XP even if level setting failed
                                    setXPFirestore(finalNewXP, null); // Or handle error more gracefully
                                }
                            });
                        } else {
                            // No level up, just set the new XP
                            setXPFirestore(finalNewXP, new WriteOperationCallback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "XP updated successfully.");
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "Error setting XP.", e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Error getting user level for addXP: ", e);
                        Toast.makeText(context, "Could not update level. Please try again.", Toast.LENGTH_SHORT).show();
                        // Fallback: Still try to update XP if level fetch failed
                        setXPFirestore(newXP, null); // newXP here is before level calculation
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error getting user XP for addXP: ", e);
                Toast.makeText(context, "Could not update XP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    /**
     * Interface for callback to receive the result of the check.
     */
    public interface BirdIdentificationCheckListener {
        void onResult(boolean isAlreadyIdentified, boolean errorOccurred);
    }
    /**
     * Checks if the current user has previously identified a specific bird.
     *
     * @param birdIdentifier The unique identifier for the bird (e.g., common name or a specific ID).
     * @param listener       Callback to receive the result.
     */
    public void hasUserIdentifiedBirdBefore(String birdIdentifier, @NonNull BirdIdentificationCheckListener listener) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Log.w(TAG, "User not logged in. Cannot check bird identification.");
            listener.onResult(false, true); // Error occurred
            return;
        }

        if (birdIdentifier == null || birdIdentifier.trim().isEmpty()) {
            Log.w(TAG, "Bird identifier is null or empty.");
            listener.onResult(false, true); // Error occurred (invalid input)
            return;
        }
        String userId = firebaseUser.getUid();
        DocumentReference userDocRef = db.collection(USERS_COLLECTION).document(userId);

        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        // Option 1: Directly access the map field if your User model isn't strictly needed here
                        Map<String, Object> uniqueBirdsMap = (Map<String, Object>) document.get(IDENTIFIED_BIRDS_SUBCOLLECTION);
                        if (uniqueBirdsMap != null && uniqueBirdsMap.containsKey(birdIdentifier)) {
                            listener.onResult(true, false); // Bird was identified before
                        } else {
                            listener.onResult(false, false); // Bird was not identified before (or map is null/empty)
                        }
                    } else {
                        Log.d(TAG, "No such user document for UID: " + userId + ". Assuming bird not identified.");
                        listener.onResult(false, false); // User document doesn't exist
                    }
                } else {
                    Log.e(TAG, "Error getting user document: ", task.getException());
                    listener.onResult(false, true); // Error occurred
                }
            }
        });
    }

    public interface UserLoadedListener {
        void onUserLoaded();
        void onUserLoadFailed();
    }
    public void registerUserLoadedListener(GamificationManager.UserLoadedListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
        // If  already loaded, notify immediately
        if (userLoaded) {
            listener.onUserLoaded();
        } else if (user==null && listeners.size() == 1) {
            // Start loading only when the first listener is registered or if explicitly called
            loadUser();
        }
    }

    public void loadUser() {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUid="";
        if (currentUser != null) {
            userUid = currentUser.getUid();
        }
        // Task to get user
        Task<DocumentSnapshot> userTask = firebaseFirestore.collection(USERS_COLLECTION).document(userUid).get();
        // Task to get already unlocked achievements
        Task<QuerySnapshot> unlockedAchievementsTask = firebaseFirestore.collection(USERS_COLLECTION).document(userUid)
                .collection(UNLOCKED_ACHIEVEMENTS_SUBCOLLECTION).get();

        // Task to get the count of identified birds
        Task<QuerySnapshot> identifiedBirdsTask = firebaseFirestore.collection(USERS_COLLECTION).document(userUid)
                .collection(IDENTIFIED_BIRDS_SUBCOLLECTION).get(); // This gets all documents

        if (currentUser != null) {
            String finalUserUid = userUid;
            Tasks.whenAllSuccess(userTask,unlockedAchievementsTask, identifiedBirdsTask)
                    .addOnSuccessListener(results -> {
                        DocumentSnapshot userDocument = (DocumentSnapshot) results.get(0);
                        QuerySnapshot unlockedAchievementsSnapshot = (QuerySnapshot) results.get(1);
                        QuerySnapshot identifiedBirdsSnapshot = (QuerySnapshot) results.get(2);
                        if(userDocument!=null)
                        {
                            user = userDocument.toObject(User.class);
                        }
                        ArrayList<Achievement> unlockedAchievements = new ArrayList<>();
                        if (unlockedAchievementsSnapshot != null) {
                            for (DocumentSnapshot doc : unlockedAchievementsSnapshot.getDocuments()) {
                                Achievement achievement = doc.toObject(Achievement.class);
                                unlockedAchievements.add(achievement);
                            }
                        }
                        user.setAchievements(unlockedAchievements);

                        ArrayList<IdentifiedBird> birdsIdentified =new ArrayList<>();
                        assert identifiedBirdsSnapshot != null;
                        for (DocumentSnapshot doc : identifiedBirdsSnapshot.getDocuments()) {
                            IdentifiedBird bird = doc.toObject(IdentifiedBird.class);
                            birdsIdentified.add(bird);
                        }
                        user.setUniqueCorrectBirdsIdentified(birdsIdentified);
                        userLoaded = true;
                        notifyUserLoaded();

                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get user for UID: " + finalUserUid, e);
                    });;
        }

    }

    public void unregisterUserLoadedListener(AchievementManager.DefinitionsLoadedListener listener) {
        listeners.remove(listener);
    }
    private void notifyUserLoaded() {
        for (GamificationManager.UserLoadedListener listener : listeners) {
            listener.onUserLoaded();
        }
    }
    private void notifyUserLoadFailed() {
        for (GamificationManager.UserLoadedListener listener : listeners) {
            listener.onUserLoadFailed();
        }
    }
    public boolean isUserLoaded() {
        return userLoaded;
    }
}
