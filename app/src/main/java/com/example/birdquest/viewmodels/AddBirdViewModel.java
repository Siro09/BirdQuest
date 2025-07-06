package com.example.birdquest.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.birdquest.db.AppDatabase;
import com.example.birdquest.db.BirdDao;
import com.example.birdquest.models.Bird;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddBirdViewModel extends AndroidViewModel {
    private BirdDao birdDao;
    private ExecutorService executorService;
    private MutableLiveData<Boolean> _insertionResult = new MutableLiveData<>();
    public LiveData<Boolean> insertionResult = _insertionResult;

    private MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;


    public AddBirdViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        birdDao = db.birdDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertBird(Bird bird) {
        executorService.execute(() -> {
            try {
                birdDao.insertBird(bird);
                _insertionResult.postValue(true);
            } catch (Exception e) {
                _errorMessage.postValue("Error inserting bird: " + e.getMessage());
                _insertionResult.postValue(false);
            }
        });
    }

    public void insertBirdsFromJson(String jsonString) {
        executorService.execute(() -> {
            try {
                List<Bird> birdsToInsert = new ArrayList<>();
                // Try parsing as a single JSON object first
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    birdsToInsert.add(parseBirdFromJson(jsonObject));
                } catch (JSONException e) {
                    // If single object fails, try parsing as a JSON array
                    JSONArray jsonArray = new JSONArray(jsonString);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        birdsToInsert.add(parseBirdFromJson(jsonArray.getJSONObject(i)));
                    }
                }

                if (!birdsToInsert.isEmpty()) {
                    birdDao.insertAll(birdsToInsert);
                    _insertionResult.postValue(true);
                } else {
                    _errorMessage.postValue("No valid bird data found in JSON.");
                    _insertionResult.postValue(false);
                }
            } catch (JSONException e) {
                _errorMessage.postValue("Invalid JSON format: " + e.getMessage());
                _insertionResult.postValue(false);
            } catch (Exception e) {
                _errorMessage.postValue("Error processing JSON: " + e.getMessage());
                _insertionResult.postValue(false);
            }
        });
    }

    private Bird parseBirdFromJson(JSONObject jsonObject) throws JSONException {
        // Ensure your JSON keys match these or adjust accordingly
        String commonName = jsonObject.optString("common_name", ""); // Use optString for safety
        String latinName = jsonObject.optString("latin_name", "");
        String imageUrl = jsonObject.optString("image_url", "");
        String birdPageUrl = jsonObject.optString("site_url", "");
        String soundUrl = jsonObject.optString("sound_url", "");

        // Basic validation (optional, but good)
        if (commonName.isEmpty() || latinName.isEmpty()) {
            throw new JSONException("Common name and Latin name are required in JSON.");
        }
        return new Bird(commonName, latinName, birdPageUrl, imageUrl, soundUrl);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}