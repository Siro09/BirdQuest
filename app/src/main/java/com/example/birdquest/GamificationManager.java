package com.example.birdquest;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GamificationManager {

    private static final String PREFS_NAME = "GamificationPrefs";
    private static final String KEY_XP = "xp";
    private static final String KEY_LEVEL = "level";

    public static void addXP(Context context, int amount) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int currentXP = prefs.getInt(KEY_XP, 0);
        int newXP = currentXP + amount;

        Toast.makeText(context, "You gained " + amount + " XP!", Toast.LENGTH_SHORT).show();
        while (newXP >= 50) {
            setLevel(context, getUserLevel(context) + 1);
            setLevelFirestore(getUserLevel(context) + 1);
            newXP -= 50;

        }
        setXP(context, newXP);
        setXPFirestore(newXP);
    }

    public static int getUserXP(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_XP, 0);
    }

    public static int getUserLevel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_LEVEL, 1);
    }

    public static void setLevel(Context context, int level) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LEVEL, level);
        editor.apply();
        MediaPlayer mp = MediaPlayer.create(context, R.raw.success);
        mp.start();
        Toast.makeText(context, "Level up! New level"+level, Toast.LENGTH_SHORT).show();
    }
    public static void setLevelFirestore( int level) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid())
                    .update("level", level)
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
    public static void setXP(Context context, int xp) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_XP, xp);
        editor.apply();
    }
    public static void setXPFirestore( int xp) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid())
                    .update("xp", xp)
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
}
