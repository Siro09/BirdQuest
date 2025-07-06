package com.example.birdquest.db;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.birdquest.models.Bird;

@Database(entities = {Bird.class}, version = 6, exportSchema = false) // Increment version number!
public abstract class AppDatabase extends RoomDatabase {

    private static final String TAG = "AppDatabase";
    public abstract BirdDao birdDao();

    private static volatile AppDatabase INSTANCE;
    private static Context applicationContext;
    static final Migration MIGRATION_5_6 = new Migration(5, 6) { // Assuming previous was 3
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

            database.execSQL("ALTER TABLE birds ADD COLUMN sound_url TEXT");

            Log.i(TAG, "Migration from version 5 to 6 (added image column) executed.");
        }
    };
    public static AppDatabase getInstance(final Context context) {
        if (applicationContext == null) {
            applicationContext = context.getApplicationContext();
        }

        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    Log.d(TAG, "Creating new database instance");
                    INSTANCE = Room.databaseBuilder(applicationContext,
                                    AppDatabase.class, "bird_database") // Your database name
                            .fallbackToDestructiveMigration()
                            .addMigrations(MIGRATION_5_6)
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.i(TAG, "Database onCreate: Tables created (potentially after destructive migration).");
            // Data will be populated by onOpen if needed
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            Log.i(TAG, "Database onOpen: Checking if data population is needed.");
            if (applicationContext != null && INSTANCE != null) {
                DataInitializer.populateDatabase(applicationContext, INSTANCE);
            } else {
                Log.e(TAG, "onOpen: Context or INSTANCE is null. Cannot populate data.");
            }
        }
    };
}