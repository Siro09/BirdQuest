package com.example.birdquest.Managers;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.birdquest.R;
import com.example.birdquest.models.IdentifiedBird;
import com.example.birdquest.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GamificationManager {
    private static final String TAG = "GamificationManager";
    private static final String USERS_COLLECTION = "users";
    private static final String PREFS_NAME = "GamificationPrefs";
    private static final String KEY_XP = "xp";
    private static final String KEY_LEVEL = "level";
    private static final String KEY_QUIZ_COMPLETIONS = "quizCompletions";
    private static final String KEY_PERFECT_QUIZ_SCORES = "perfectQuizScores";
    private static final String KEY_UNIQUE_CORRECT_BIRDS_IDENTIFIED = "uniqueCorrectBirdsIdentified";

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
    public static int getQuizCompletions(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final int[] quizCompletions = {1};
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Long quizCompletionsLong = document.getLong(KEY_QUIZ_COMPLETIONS);
                                    if (quizCompletionsLong != null) {
                                        quizCompletions[0] = quizCompletionsLong.intValue();
                                    } else {
                                        Log.w(TAG, "quizCompletions field is null or missing in user document for UID: " + currentUser.getUid());
                                        // Handle case where Level might be missing
                                    }

                                }
                                Log.d("FirestoreUpdate", "User quizCompletions updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user quizCompletions: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
        return quizCompletions[0];

    }
    public static int getPerfectQuizScores(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final int[] perfectQuizScores = {1};
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Long perfectQuizScoresLong = document.getLong(KEY_PERFECT_QUIZ_SCORES);
                                    if (perfectQuizScoresLong != null) {
                                        perfectQuizScores[0] = perfectQuizScoresLong.intValue();
                                    } else {
                                        Log.w(TAG, "perfectQuizScores field is null or missing in user document for UID: " + currentUser.getUid());
                                        // Handle case where Level might be missing
                                    }

                                }
                                Log.d("FirestoreUpdate", "User perfectQuizScores updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user perfectQuizScores: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
        return perfectQuizScores[0];
    }
    public static int getUniqueCorrectBirdsIdentified(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final int[] uniqueCorrectBirdsIdentified = {1};
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Long uniqueCorrectBirdsIdentifiedLong = document.getLong(KEY_UNIQUE_CORRECT_BIRDS_IDENTIFIED);
                                    if (uniqueCorrectBirdsIdentifiedLong != null) {
                                        uniqueCorrectBirdsIdentified[0] = uniqueCorrectBirdsIdentifiedLong.intValue();
                                    } else {
                                        Log.w(TAG, "uniqueCorrectBirdsIdentified field is null or missing in user document for UID: " + currentUser.getUid());
                                        // Handle case where Level might be missing
                                    }

                                }
                                Log.d("FirestoreUpdate", "User uniqueCorrectBirdsIdentified updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user uniqueCorrectBirdsIdentified: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
        return uniqueCorrectBirdsIdentified[0];
    }
    public static void addQuizCompletions(Context context) {

        setQuizCompletionsFirestore(GamificationManager.getQuizCompletions(context) + 1);
    }
    public static void addPerfectQuizScores(Context context) {

        setPerfectQuizScoresFirestore(GamificationManager.getPerfectQuizScores(context) + 1);
    }
    public static void addUniqueCorrectBirdsIdentified(Context context,String birdIdentifier) {

        setUniqueCorrectBirdsIdentifiedFirestore(birdIdentifier);
    }
    public static void setQuizCompletionsFirestore(int quizCompletions) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update("quizCompletions", quizCompletions)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("FirestoreUpdate", "User quizCompletions updated successfully.");
                            }
                            else {
                                Log.e("FirestoreUpdate", "Error updating user quizCompletions: ", task.getException());
                            }
                    }
                    });

        }
    }
    public static void setUniqueCorrectBirdsIdentifiedFirestore(String uniqueCorrectBirdsIdentified ) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        IdentifiedBird birdToInsert = new IdentifiedBird(true);
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid()).collection(KEY_UNIQUE_CORRECT_BIRDS_IDENTIFIED)
                    .document(uniqueCorrectBirdsIdentified).set(birdToInsert)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("FirestoreUpdate", "User uniqueCorrectBirdsIdentified updated successfully.");
                            }
                            else {
                                Log.e("FirestoreUpdate", "Error updating user uniqueCorrectBirdsIdentified: ", task.getException());
                            }
                        }
                    });

        }
    }
    public static void setPerfectQuizScoresFirestore(int perfectQuizScores) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update("perfectQuizScores", perfectQuizScores)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("FirestoreUpdate", "User perfectQuizScores updated successfully.");
                            }
                            else {
                                Log.e("FirestoreUpdate", "Error updating user perfectQuizScores: ", task.getException());
                            }
                        }
                    });

        }
    }
    public static void addXP(Context context, int amount) {

        int currentXP = GamificationManager.getUserXPFirestore(context);
        int newXP = currentXP + amount;
        boolean leveledUp = false;
        Toast.makeText(context, "You gained " + amount + " XP!", Toast.LENGTH_SHORT).show();

            while (newXP >= 50) {
                setLevelFirestore(getUserLevelFirestore(context) + 1);
                newXP -= 50;
                leveledUp=true;
            }
        if(leveledUp)
        {
            MediaPlayer mp = MediaPlayer.create(context, R.raw.success);
            mp.start();
            int level = GamificationManager.getUserLevelFirestore(context);
            Toast.makeText(context, "Level up! New level"+level, Toast.LENGTH_SHORT).show();
        }


        setXPFirestore(newXP);
    }

    public static int getUserXPFirestore(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final int[] xp = {1};
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Long xpLong = document.getLong("xp");
                                    if (xpLong != null) {
                                        xp[0] = xpLong.intValue();
                                    } else {
                                        Log.w(TAG, "xp field is null or missing in user document for UID: " + currentUser.getUid());
                                        // Handle case where Level might be missing
                                    }

                                }
                                Log.d("FirestoreUpdate", "User xp updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user xp: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
        return xp[0];
    }

    public static int getUserLevelFirestore(Context context) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final int[] level = {1};
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    Long levelLong = document.getLong("level");
                                    if (levelLong != null) {
                                        level[0] = levelLong.intValue();
                                    } else {
                                        Log.w(TAG, "Level field is null or missing in user document for UID: " + currentUser.getUid());
                                        // Handle case where Level might be missing
                                    }

                                }
                                Log.d("FirestoreUpdate", "User level updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user level: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
        return level[0];
    }

    public static void setLevelFirestore( int level) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update(KEY_LEVEL, level)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("FirestoreUpdate", "User level updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user level: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
    }

    public static void setXPFirestore( int xp) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .update(KEY_XP, xp)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("FirestoreUpdate", "User xp updated successfully.");
                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user xp: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                            }
                        }
                    });
        }
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
                        Map<String, Object> uniqueBirdsMap = (Map<String, Object>) document.get(KEY_UNIQUE_CORRECT_BIRDS_IDENTIFIED);
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

        if (currentUser != null) {
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    user = document.toObject(User.class);

                                }
                                Log.d("FirestoreUpdate", "User level updated successfully.");

                                // Optionally, notify the user or update UI
                            } else {
                                Log.e("FirestoreUpdate", "Error updating user level: ", task.getException());
                                // Optionally, show an error message to the user
                                // Consider retry logic if appropriate
                                userLoaded = false;
                                notifyUserLoadFailed();
                            }
                        }
                    });
            firebaseFirestore.collection(USERS_COLLECTION).document(currentUser.getUid()).collection(KEY_UNIQUE_CORRECT_BIRDS_IDENTIFIED)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            QuerySnapshot identifiedBirdsSnapshot = task.getResult();

                            ArrayList<IdentifiedBird> birdsIdentified =new ArrayList<>();
                            assert identifiedBirdsSnapshot != null;
                            for (DocumentSnapshot doc : identifiedBirdsSnapshot.getDocuments()) {
                                IdentifiedBird bird = doc.toObject(IdentifiedBird.class);
                                birdsIdentified.add(bird);
                            }
                            user.setUniqueCorrectBirdsIdentified(birdsIdentified); // Assuming User.java has this setter
                            userLoaded = true;
                            notifyUserLoaded();
                            Log.d(TAG, "User " +  " has identified " + birdsIdentified.size() + " unique birds.");
                        }

                    });
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
