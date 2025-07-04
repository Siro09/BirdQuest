package com.example.birdquest;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.birdquest.R;

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
            setLevel(context, getLevel(context) + 1);
            newXP -= 50;

        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_XP, newXP);
        editor.apply();

    }

    public static int getXP(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_XP, 0);
    }

    public static int getLevel(Context context) {
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
}
