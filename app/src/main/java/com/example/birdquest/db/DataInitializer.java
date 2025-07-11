package com.example.birdquest.db;

import android.content.Context;
import android.util.Log;

import com.example.birdquest.models.Bird;
import com.example.birdquest.utils.BirdDataExtractor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataInitializer {

    private static final String TAG = "DataInitializer";
    private static final String BIRD_NAMES_FILE = "birds_names.json"; // Contains common_name and latin_name
    private static final String BIRD_URL_FILE = "bird_url.json";     // Contains latin_name and url

    private static final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    public static void populateDatabase(Context context, AppDatabase appDatabase) {
        databaseExecutor.execute(() -> {
            BirdDao birdDao = appDatabase.birdDao();

            if (birdDao.getBirdCount() > 0) {
                Log.i(TAG, "Database already populated with birds. Skipping initialization.");
                return;
            }

            Log.i(TAG, "Populating database with initial bird data...");

            try {
                String namesJsonString = loadJSONFromAsset(context, BIRD_NAMES_FILE);
                String urlsJsonString = loadJSONFromAsset(context, BIRD_URL_FILE);

                if (namesJsonString == null) {
                    Log.e(TAG, "Failed to load " + BIRD_NAMES_FILE);
                    return;
                }
                if (urlsJsonString == null) {
                    Log.e(TAG, "Failed to load " + BIRD_URL_FILE + ". Image URLs might be missing.");

                }

                // Create a map for quick URL lookup by latin_name
                Map<String, String> latinNameToUrlMap = new HashMap<>();
                if (urlsJsonString != null) {
                    JSONArray urlsArray = new JSONArray(urlsJsonString);
                    for (int i = 0; i < urlsArray.length(); i++) {
                        JSONObject urlEntry = urlsArray.getJSONObject(i);
                        String latinName = urlEntry.optString("latin_name");
                        String url = urlEntry.optString("url");
                        if (latinName != null && !latinName.isEmpty() && url != null && !url.isEmpty()) {
                            latinNameToUrlMap.put(latinName, url);
                        }
                    }
                    Log.i(TAG, "Loaded " + latinNameToUrlMap.size() + " URL entries from " + BIRD_URL_FILE);
                }


                JSONArray birdsNamesArray = new JSONArray(namesJsonString);
                List<Bird> birdsToInsert = new ArrayList<>();

                for (int i = 0; i < birdsNamesArray.length(); i++) {
                    JSONObject birdNameJson = birdsNamesArray.getJSONObject(i);

                    String commonName = birdNameJson.optString("common_name", null);
                    String latinName = birdNameJson.optString("latin_name", null);
                    String siteUrl = null;
                    String imageUrl = null;
                    String soundUrl = null;
                    String distributionUrl = null;
                    if (commonName == null || commonName.trim().isEmpty()) {
                        Log.w(TAG, "Skipping bird with missing or empty common_name at index " + i + " in " + BIRD_NAMES_FILE);
                        continue;
                    }
                    if (latinName == null || latinName.trim().isEmpty()) {
                        Log.w(TAG, "Bird '" + commonName + "' has missing or empty latin_name. URL cannot be matched.");
                        // Continue with null imageUrl or skip, depending on requirements
                    } else {
                        // Try to find the URL using the latin_name
                        siteUrl = latinNameToUrlMap.get(latinName);
                        if (siteUrl == null) {
                            Log.w(TAG, "No URL found for latin_name: '" + latinName + "' (Common Name: '" + commonName + "')");
                        }
                    }
                    if (siteUrl != null) {
                        ArrayList<String> dataUrls = BirdDataExtractor.extractMatchingDataUrl(siteUrl, latinName);
                        if(dataUrls!=null) {
                            imageUrl = dataUrls.get(0);
                            soundUrl = dataUrls.get(1);
                            distributionUrl = dataUrls.get(2);
                        }
                    }
                    Bird bird = new Bird(commonName, latinName, siteUrl,imageUrl,soundUrl,distributionUrl);
                    birdsToInsert.add(bird);
                }

                if (!birdsToInsert.isEmpty()) {
                    birdDao.insertAll(birdsToInsert);
                    Log.i(TAG, "Successfully inserted " + birdsToInsert.size() + " birds into the database.");
                } else {
                    Log.i(TAG, "No valid birds found in " + BIRD_NAMES_FILE + " to insert.");
                }

            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "Error reading JSON from assets: " + e.getMessage(), e);
            }
        });
    }

    private static String loadJSONFromAsset(Context context, String fileName) throws IOException {
        String json = null;
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            json = new String(buffer, StandardCharsets.UTF_8);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing input stream: ", e);
                }
            }
        }
        return json;
    }

}