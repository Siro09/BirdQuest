package com.example.birdquest; // Adjust package name as needed

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.birdquest.R; // Adjust R import
import com.example.birdquest.models.Bird; // Adjust Bird model import
import com.example.birdquest.viewmodels.AddBirdViewModel; // Adjust ViewModel import
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AddBirdActivity extends AppCompatActivity {

    private AddBirdViewModel addBirdViewModel;

    private TextInputLayout tilCommonName, tilLatinName;
    private TextInputEditText editTextCommonName, editTextLatinName,
            editTextImageUrl, editTextBirdPageUrl, editTextSoundUrl, editTextJsonInput;
    private Button buttonAddManual, buttonAddJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bird); // Your layout file name

        // Initialize ViewModel
        addBirdViewModel = new ViewModelProvider(this).get(AddBirdViewModel.class);

        // Initialize Views
        tilCommonName = findViewById(R.id.tilCommonName);
        tilLatinName = findViewById(R.id.tilLatinName);
        editTextCommonName = findViewById(R.id.editTextCommonName);
        editTextLatinName = findViewById(R.id.editTextLatinName);
        editTextImageUrl = findViewById(R.id.editTextImageUrl);
        editTextBirdPageUrl = findViewById(R.id.editTextBirdPageUrl);
        editTextSoundUrl = findViewById(R.id.editTextSoundUrl);
        editTextJsonInput = findViewById(R.id.editTextJsonInput);
        buttonAddManual = findViewById(R.id.buttonAddManual);
        buttonAddJson = findViewById(R.id.buttonAddJson);

        // Set Click Listeners
        buttonAddManual.setOnClickListener(v -> addBirdManually());
        buttonAddJson.setOnClickListener(v -> addBirdsFromJson());

        // Observe ViewModel results
        addBirdViewModel.insertionResult.observe(this, success -> {
            if (success) {
                Toast.makeText(AddBirdActivity.this, "Bird(s) added successfully!", Toast.LENGTH_SHORT).show();
                // Optionally clear fields or finish activity
                clearManualInputFields();
                editTextJsonInput.setText(""); // Clear JSON input
                // finish(); // Uncomment to close activity on success
            }
            // Error messages are handled by the errorMessage LiveData
        });

        addBirdViewModel.errorMessage.observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(AddBirdActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addBirdManually() {
        String commonName = editTextCommonName.getText().toString().trim();
        String latinName = editTextLatinName.getText().toString().trim();
        String imageUrl = editTextImageUrl.getText().toString().trim();
        String birdPageUrl = editTextBirdPageUrl.getText().toString().trim();
        String soundUrl = editTextSoundUrl.getText().toString().trim();

        // Basic Validation
        boolean isValid = true;
        if (TextUtils.isEmpty(commonName)) {
            tilCommonName.setError("Common name is required");
            isValid = false;
        } else {
            tilCommonName.setError(null);
        }

        if (TextUtils.isEmpty(latinName)) {
            tilLatinName.setError("Latin name is required");
            isValid = false;
        } else {
            tilLatinName.setError(null);
        }

        if (!isValid) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Bird bird = new Bird(commonName, latinName, imageUrl, birdPageUrl, soundUrl);
        addBirdViewModel.insertBird(bird);
    }

    private void addBirdsFromJson() {
        String jsonInput = editTextJsonInput.getText().toString().trim();
        if (TextUtils.isEmpty(jsonInput)) {
            Toast.makeText(this, "JSON input cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }
        addBirdViewModel.insertBirdsFromJson(jsonInput);
    }

    private void clearManualInputFields() {
        editTextCommonName.setText("");
        editTextLatinName.setText("");
        editTextImageUrl.setText("");
        editTextBirdPageUrl.setText("");
        editTextSoundUrl.setText("");
        tilCommonName.setError(null);
        tilLatinName.setError(null);
        editTextCommonName.requestFocus(); // Set focus to the first field
    }
}