
package com.example.birdquest.quiz;

import java.io.File;
import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.birdquest.Managers.AchievementManager;
import com.example.birdquest.R;
import com.example.birdquest.backend.BackendCaller;
import com.example.birdquest.db.AppDatabase;
import com.example.birdquest.db.BirdDao;
import com.example.birdquest.models.Bird;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.example.birdquest.Managers.GamificationManager;

public class QuizActivity extends AppCompatActivity implements  AchievementManager.DefinitionsLoadedListener {

    private static final String TAG = "QuizActivity";
    private static final long DELAY_NEXT_QUESTION = 1500; // 1.5 seconds for auto-advance
    private ImageView imageViewBird,imageViewDistribution;
    private AppDatabase appDb;

    AchievementManager achievementManager;
    // Executor for background database operations
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    // Handler to post results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private Button btnPlaySound; // New button for hard mode
    private Button btnShowDistribution; // New button for hard mode
    private Button btnShowImage; // New button for hard mode
    private Button btnShowSoundPlayer; // New button for hard mode

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private LinearLayout mediaSwitcherLayout;
    private GridLayout gridLayoutAnswers;
    // private Button buttonNextQuestion; // manual "Next" button

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerSelectedThisTurn = false; // To prevent multiple answer clicks for one question
    public static final String EXTRA_QUIZ_MODE = "QUIZ_MODE";
    public static final String MODE_NORMAL = "NORMAL";
    public static final String MODE_HARD = "HARD";
    private String currentQuizMode = MODE_NORMAL;
    private int difficultyScale = 1;
    private MediaPlayer mediaPlayer;
    private final String backendUrl = "http://192.168.0.102:8000/process/";
    File outputFile ;
    // Keys for saving state
    private static final String KEY_CURRENT_QUESTION_INDEX = "currentQuestionIndex";
    private static final String KEY_SCORE = "score";
    private static final String KEY_QUESTION_LIST = "questionList"; // If Question is Parcelable/Serializable
    private static final String KEY_ANSWER_SELECTED_THIS_TURN = "answerSelectedThisTurn";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_QUIZ_MODE)) {
            currentQuizMode = intent.getStringExtra(EXTRA_QUIZ_MODE);
        }
        Log.d(TAG, "Current Quiz Mode: " + currentQuizMode);

        textViewQuestion = findViewById(R.id.textViewQuestion);
        textViewScore = findViewById(R.id.textViewScore);
        textViewQuestionCount = findViewById(R.id.textViewQuestionCount);
        gridLayoutAnswers = findViewById(R.id.gridLayoutAnswers);
        mediaSwitcherLayout = findViewById(R.id.mediaSwitcherLayout);
        btnShowDistribution = findViewById(R.id.btnShowDistribution);
        btnShowImage = findViewById(R.id.btnShowImage);
        btnShowSoundPlayer = findViewById(R.id.btnShowSoundPlayer);
        imageViewBird = findViewById(R.id.imageViewBird); // Initialize ImageView
        btnPlaySound = findViewById(R.id.btnPlaySound);
        imageViewDistribution = findViewById(R.id.imageViewDistribution);
        // buttonNextQuestion = findViewById(R.id.buttonNextQuestion); // Uncomment if using manual next

        btnShowSoundPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnPlaySound.setEnabled(true);
                btnPlaySound.setVisibility(View.VISIBLE);
                imageViewDistribution.setVisibility(View.GONE);
                imageViewBird.setVisibility(View.GONE);
            }
        });
        btnShowDistribution.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(difficultyScale > 2)
                {
                    difficultyScale = 2;
                }
                imageViewDistribution.setVisibility(View.VISIBLE);
                imageViewBird.setVisibility(View.GONE);
                btnPlaySound.setVisibility(View.GONE);
            }
        });
        btnShowImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(difficultyScale > 1)
                {
                    difficultyScale = 1;
                }
                imageViewBird.setVisibility(View.VISIBLE);
                imageViewDistribution.setVisibility(View.GONE);
                btnPlaySound.setVisibility(View.GONE);
            }
        });

        btnPlaySound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playCurrentBirdSound();
            }
        });
        appDb = AppDatabase.getInstance(getApplicationContext());
        if (currentQuizMode.equals(MODE_NORMAL))
        {
            mediaSwitcherLayout.setVisibility(View.GONE);
        }
        else if (currentQuizMode.equals(MODE_HARD))
        {
            mediaSwitcherLayout.setVisibility(View.VISIBLE);
        }
        if (savedInstanceState != null) {
            // Restore state
            currentQuestionIndex = savedInstanceState.getInt(KEY_CURRENT_QUESTION_INDEX);
            score = savedInstanceState.getInt(KEY_SCORE);
            answerSelectedThisTurn = savedInstanceState.getBoolean(KEY_ANSWER_SELECTED_THIS_TURN);
            currentQuizMode = savedInstanceState.getString(EXTRA_QUIZ_MODE);
            Log.d(TAG, "Current Quiz Mode: " + currentQuizMode);

            if (savedInstanceState.containsKey(KEY_QUESTION_LIST)) {
                questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            }
            if (currentQuizMode.equals(MODE_NORMAL))
            {
                mediaSwitcherLayout.setVisibility(View.GONE);
            }
            else if (currentQuizMode.equals(MODE_HARD))
            {
                mediaSwitcherLayout.setVisibility(View.VISIBLE);
            }
            difficultyScale = savedInstanceState.getInt("difficultyScale");

            if (questionList != null && !questionList.isEmpty() && currentQuestionIndex < questionList.size()) {
                Log.d(TAG, "Restored state: index=" + currentQuestionIndex + ", score=" + score + ", questions=" + questionList.size());
                displayQuestion(); // Display the restored question
            } else {
                // If questionList couldn't be restored or is invalid, load fresh
                Log.d(TAG, "Question list not properly restored or empty, loading fresh.");
                loadQuestionsInBackground();
            }
        } else {
            // No saved state, load fresh
            Log.d(TAG, "No saved state, loading fresh questions.");
            loadQuestionsInBackground();

        }


        // If using a manual next button:
        /*
        buttonNextQuestion.setOnClickListener(v -> {
            if (answerSelectedThisTurn) { // Ensure an answer was selected before moving next
                loadNextQuestion();
            } else {
                Toast.makeText(QuizActivity.this, "Please select an answer.", Toast.LENGTH_SHORT).show();
            }
        });
        */
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState called");
        outState.putInt(KEY_CURRENT_QUESTION_INDEX, currentQuestionIndex);
        outState.putInt(KEY_SCORE, score);
        outState.putBoolean(KEY_ANSWER_SELECTED_THIS_TURN, answerSelectedThisTurn);
        outState.putString(EXTRA_QUIZ_MODE, currentQuizMode);
        outState.putInt("difficultyScale", difficultyScale);
        // Save the question list if it's not null and not empty
        // This requires Question class to be Parcelable or Serializable
        if (questionList != null && !questionList.isEmpty()) {

            outState.putParcelableArrayList(KEY_QUESTION_LIST, (ArrayList<? extends Parcelable>) questionList);

        }
    }
    private void playCurrentBirdSound() {

        String currentBirdSoundUrl ="";

        currentBirdSoundUrl = questionList.get(currentQuestionIndex).getSoundUrl();
        if(mediaPlayer != null && mediaPlayer.isPlaying() )
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer= null;
            btnPlaySound.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this,android.R.drawable.ic_media_play),null,null);
        }
        else if (currentBirdSoundUrl != null ) {

            mediaPlayer=MediaPlayer.create(this, Uri.parse(currentBirdSoundUrl));
            mediaPlayer.setOnCompletionListener( mp -> {
                btnPlaySound.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this,android.R.drawable.ic_media_play),null,null);
                mp.release();
            }); // Release when done
            mediaPlayer.start();
            btnPlaySound.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this,android.R.drawable.ic_media_pause),null,null);
        }

    }
    private void loadQuestionsInBackground() {
        questionList = new ArrayList<>();
        databaseExecutor.execute(() -> {
            //  This block runs on a background thread
            final List<Question> tempQuestionList = new ArrayList<>();
            boolean success = true;
            String errorMessage = null;

            try {
                BirdDao birdDao = appDb.birdDao(); // Get DAO instance
                List<Bird> quizBirds = Collections.emptyList();
                if(Objects.equals(currentQuizMode, MODE_NORMAL)) {
                    quizBirds = birdDao.getRandomBirdsWithImages(10); // DB CALL
                }
                else if(Objects.equals(currentQuizMode, MODE_HARD))
                {
                    quizBirds = birdDao.getRandomBirdsWithSoundAndDistribution(10); // DB CALL
                }
                if (quizBirds == null || quizBirds.size() < 4) {
                    Log.e(TAG, "Not enough birds with images in the database. Found: " + (quizBirds == null ? 0 : quizBirds.size()));
                    success = false;
                    errorMessage = "Not enough bird data for the quiz.";
                } else {
                    List<String> allCommonNames = Collections.emptyList();
                    if(Objects.equals(currentQuizMode, MODE_NORMAL)) {
                        allCommonNames = birdDao.getAllBirdCommonNamesWithImages(); // DB CALL
                    }
                    else if(Objects.equals(currentQuizMode, MODE_HARD))
                    {
                        allCommonNames = birdDao.getAllBirdCommonNamesWithSoundAndDistribution(); // DB CALL
                    }
                    if (allCommonNames == null || allCommonNames.isEmpty() || allCommonNames.size() < 4) { // Check for sufficient distractors
                        Log.e(TAG, "Not enough common names for distractors. Found: " + (allCommonNames == null ? 0 : allCommonNames.size()));
                        success = false;
                        errorMessage = "Error loading distractor names.";
                    }else {
                        Random random = new Random();
                        for (Bird correctBird : quizBirds) {
                            if (correctBird.getCommonName() == null || correctBird.getCommonName().trim().isEmpty() ||
                                    correctBird.getImageUrl() == null || correctBird.getImageUrl().trim().isEmpty()) {
                                Log.w(TAG, "Skipping bird with missing name or image URL: " + correctBird.commonName);
                                continue;
                            }

                            String questionText = "Identifica pasarea:";
                            List<String> options = new ArrayList<>();
                            options.add(correctBird.getCommonName());

                            // Create a list of potential distractors by removing the correct answer's name
                            List<String> potentialDistractors = new ArrayList<>(allCommonNames);
                            potentialDistractors.remove(correctBird.getCommonName());

                            if (potentialDistractors.size() < 3) {
                                Log.w(TAG, "Not enough unique distractors for bird: " + correctBird.getCommonName() + ". Skipping.");
                                continue;
                            }
                            Collections.shuffle(potentialDistractors);

                            for (int i = 0; i < 3; i++) { // Add 3 distractors
                                options.add(potentialDistractors.get(i));
                            }
                            Collections.shuffle(options);
                            int correctAnswerIndex = options.indexOf(correctBird.getCommonName());
                            if (correctAnswerIndex == -1) {
                                Log.e(TAG, "Critical: Correct answer not found in options for " + correctBird.getCommonName());
                                continue;
                            }
                            if(Objects.equals(currentQuizMode, MODE_NORMAL)) {
                                tempQuestionList.add(new Question(questionText, options, correctAnswerIndex, correctBird.getImageUrl()));
                            }
                            else if(Objects.equals(currentQuizMode, MODE_HARD))
                            {
                                tempQuestionList.add(new Question(questionText, options, correctAnswerIndex, correctBird.getImageUrl(),correctBird.getSoundUrl(),correctBird.getDistributionUrl()));
                            }
                        }
                        if (tempQuestionList.isEmpty() && !quizBirds.isEmpty()) {
                            Log.w(TAG, "Processed birds, but tempQuestionList is empty (validation/distractor issues).");
                            // success might still be true if quizBirds was valid but no questions could be formed
                            if (success) { // Only set error if not already set by earlier checks
                                errorMessage = "Could not form any valid questions.";
                                // success = false; // Decide if this state is a full failure
                            }
                        }
                    }
                }
            } catch (Exception e){ // Catch any other unexpected exceptions during background processing
                Log.e(TAG, "Error during background question loading", e);
                success = false;
                errorMessage = "An unexpected error occurred while loading questions.";
            }
            //  End of background thread block

            //  post the result back to the main thread
            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;
            mainThreadHandler.post(() -> {
                //  This block runs on the main thread
                // if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (finalSuccess && !tempQuestionList.isEmpty()) {
                    questionList.clear();
                    questionList.addAll(tempQuestionList);
                    displayQuestion();
                    Collections.shuffle(questionList); // Shuffle the final list
                    Log.i(TAG, "Successfully generated " + questionList.size() + " questions.");

                } else {
                    // Handle error: update UI to show error message
                    String errorToDisplay = finalErrorMessage != null ? finalErrorMessage : "Error: No questions available.";
                    Log.e(TAG, "Failed to load questions: " + errorToDisplay);
                    textViewQuestion.setText(errorToDisplay); // Show error in the question text view
                    imageViewBird.setVisibility(View.GONE); // Hide image if error
                    gridLayoutAnswers.setVisibility(View.GONE);
                    Toast.makeText(QuizActivity.this, errorToDisplay, Toast.LENGTH_LONG).show();
                    // Optionally, show an AlertDialog to exit or retry
                    new AlertDialog.Builder(QuizActivity.this)
                            .setTitle("Quiz Error")
                            .setMessage(errorToDisplay + "\nWould you like to try again?")
                            .setPositiveButton("Retry", (dialog, which) -> loadQuestionsInBackground())
                            .setNegativeButton("Exit", (dialog, which) -> finish())
                            .setCancelable(false)
                            .show();
                }
            });
        });

        }

    private void displayQuestion() {
        if (currentQuestionIndex < questionList.size()) {
            answerSelectedThisTurn = false; // Reset for the new question
            Question currentQuestion = questionList.get(currentQuestionIndex);
            textViewQuestion.setText(currentQuestion.getQuestionText());
            updateScoreDisplay();

            gridLayoutAnswers.removeAllViews(); // Clear previous answer buttons
            gridLayoutAnswers.setVisibility(View.VISIBLE);
            gridLayoutAnswers.setAlpha(0f); // For fade-in animation
            if (Objects.equals(currentQuizMode, MODE_NORMAL)) {
                difficultyScale =1;
                // Handle Image Display
                if (currentQuestion.getImageUrl() != null) {
                    imageViewBird.setVisibility(View.VISIBLE);
                    Glide.with(this)
                            .load(currentQuestion.getImageUrl())
                            .placeholder(R.drawable.ic_bird_placeholder) //  a placeholder image
                            .error(R.drawable.ic_launcher_foreground)       // an error image if load fails
                            .into(imageViewBird);
                } else {
                    imageViewBird.setVisibility(View.GONE); // Hide if no image URL
                    Log.w(TAG, "No image URL for question: " + currentQuestion.getQuestionText());
                }
            }
            else if(Objects.equals(currentQuizMode, MODE_HARD))
            {
                difficultyScale = 3;
                if(mediaSwitcherLayout != null )
                {
                    mediaSwitcherLayout.setVisibility(View.VISIBLE);
                    btnShowSoundPlayer.setVisibility(View.VISIBLE);
                    btnShowDistribution.setVisibility(View.VISIBLE);
                    btnShowImage.setVisibility(View.VISIBLE);
                    btnShowSoundPlayer.callOnClick();
                    Glide.with(this)
                            .load(currentQuestion.getImageUrl())
                            .placeholder(R.drawable.ic_bird_placeholder) // a placeholder image
                            .error(R.drawable.ic_launcher_foreground)       // an error image if load fails
                            .into(imageViewBird);

                    // load bird distribution map image
                    boolean reachable = isOnSameWifiSubnet(this,"192.168.0.102");
                    if (!reachable) {
                        Log.e(TAG, "Python backend not reachable.");
                        Glide.with(this)
                                .load(currentQuestion.getDistributionUrl())
                                .placeholder(R.drawable.ic_bird_placeholder) //  a placeholder image
                                .error(R.drawable.ic_launcher_foreground)       // an error image if load fails
                                .into(imageViewDistribution);
                    }
                    else {
                        outputFile = new File(getCacheDir(), currentQuestion.getCorrectAnswer()+"_map_image.png");
                        BackendCaller caller = new BackendCaller();
                        caller.callBackend(currentQuestion.getDistributionUrl(), outputFile, success -> {
                            if (success) {
                                runOnUiThread(() -> {
                                    // For example, load image into ImageView here
                                    Glide.with(this)
                                            .load(outputFile)
                                            .placeholder(R.drawable.ic_bird_placeholder) //  a placeholder image
                                            .error(R.drawable.ic_launcher_foreground)       // an error image if load fails
                                            .into(imageViewDistribution);
                                    System.out.println("Image saved at: " + outputFile.getAbsolutePath());
                                });
                            } else {
                                System.out.println("Failed to process image");
                            }
                        });
                    }

                }
            }

            List<String> options = currentQuestion.getAnswerOptions();
            for (int i = 0; i < options.size(); i++) {
                    // Inflate the answer button from item_answer_choice.xml
                    MaterialButton answerButton = (MaterialButton) LayoutInflater.from(this)
                            .inflate(R.layout.item_answer_choice, gridLayoutAnswers, false);

                    answerButton.setText(options.get(i));
                    final int optionIndex = i; // Must be final for use in lambda

                    answerButton.setOnClickListener(v -> {
                        if (!answerSelectedThisTurn) { // Process click only if no answer selected yet for this turn
                            checkAnswer(optionIndex, answerButton);
                            answerSelectedThisTurn = true;
                        }
                    });

                    // Add button to GridLayout
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = 0; // Use 0 for width/height with layout_weight for even distribution
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT;
                    params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Weight 1 for even distribution
                    params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                    params.setMargins(8, 8, 8, 8); // Or use margins from item_answer_choice.xml
                    answerButton.setLayoutParams(params);

                    gridLayoutAnswers.addView(answerButton);

                }
                gridLayoutAnswers.requestLayout(); // Ask the grid to re-measure and re-layout
                gridLayoutAnswers.invalidate();   // Ask the grid to redraw
                // Fade in animation for answer grid
                gridLayoutAnswers.animate().alpha(1f).setDuration(500).start();

                // If using manual next button
                // buttonNextQuestion.setVisibility(View.GONE);

            } else {
                // Quiz finished
                showFinalScore();
            }

    }
    private void checkAnswer(int selectedOptionIndex, MaterialButton selectedButton) {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        boolean isCorrect = (selectedOptionIndex == currentQuestion.getCorrectAnswerIndex());

        // Disable all buttons after an answer is selected
        for (int i = 0; i < gridLayoutAnswers.getChildCount(); i++) {
            View child = gridLayoutAnswers.getChildAt(i);
            if (child instanceof MaterialButton) {
                child.setEnabled(false);
            }
        }
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            btnPlaySound.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(this,android.R.drawable.ic_media_play),null,null);
            mediaPlayer= null;
        }
        if (isCorrect) {
            score=score+ difficultyScale;

            String identifiedBird = currentQuestion.getCorrectAnswer();
            GamificationManager gamificationManager = new GamificationManager();
            gamificationManager.hasUserIdentifiedBirdBefore(identifiedBird, new GamificationManager.BirdIdentificationCheckListener() {
            @Override
            public void onResult(boolean isAlreadyIdentified, boolean errorOccurred) {
                if (errorOccurred) {
                    // Handle the error (e.g., show a message, log it)
                    Log.e("BirdCheck", "Error checking bird identification status.");
                    return;
                }

                if (isAlreadyIdentified) {
                    Log.d("BirdCheck", identifiedBird + " has been identified before by this user.");
                    // UI Logic: Maybe show a "✓ Already Identified" badge
                } else {
                    Log.d("BirdCheck", identifiedBird + " has NOT been identified before by this user.");
                    GamificationManager.addUniqueCorrectBirdsIdentified(QuizActivity.this,identifiedBird);
                }
            }
            });
            selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer_green)); // Define this color
            selectedButton.setTextColor(Color.WHITE);
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
        } else {
            selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.incorrect_answer_red)); // Define this color
            selectedButton.setTextColor(Color.WHITE);
            Toast.makeText(this, "Incorrect. The answer was: " + currentQuestion.getCorrectAnswer(), Toast.LENGTH_SHORT).show();

            // Highlight the correct answer
            View correctButtonView = gridLayoutAnswers.getChildAt(currentQuestion.getCorrectAnswerIndex());
            if (correctButtonView instanceof MaterialButton) {
                ((MaterialButton) correctButtonView).setBackgroundColor(ContextCompat.getColor(this, R.color.correct_answer_green));
                ((MaterialButton) correctButtonView).setTextColor(Color.WHITE);
            }
        }
        updateScoreDisplay();

        // If using manual next button, show it now
        // buttonNextQuestion.setVisibility(View.VISIBLE);

        // Auto-advance to the next question after a delay
        new Handler(Looper.getMainLooper()).postDelayed(this::loadNextQuestion, DELAY_NEXT_QUESTION);
    }

    private void loadNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionList.size()) {
            displayQuestion();
        } else {
            showFinalScore();
        }
    }

    private void updateScoreDisplay() {
        String viewScoreDisplay = getString(R.string.score_display)+ score;
        textViewScore.setText(viewScoreDisplay);
        String questionCountDisplay = getString(R.string.question) + (currentQuestionIndex + 1) + "/" + questionList.size();
        textViewQuestionCount.setText(questionCountDisplay);
    }
    private void showFinalScore() {
        // Hide the answer grid and question text view
        gridLayoutAnswers.setVisibility(View.GONE);
        textViewQuestion.setAlpha(0f);

        textViewQuestion.setVisibility(View.GONE); // Make question text view visible again
        textViewQuestion.setAlpha(0f);


        //track gamification
        GamificationManager.addXP(this, score * 2);
        GamificationManager.addQuizCompletions(this);

        if(score >= questionList.size()){
            GamificationManager.addPerfectQuizScores(this);
        }

        achievementManager = new AchievementManager(this);
        achievementManager.registerDefinitionsLoadedListener(this);
        if (!achievementManager.areDefinitionsLoaded()) {
            achievementManager.loadAllAchievementDefinitions();
        }
        // Display final score in an AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Quiz Finished!")
                .setMessage("Your final score is: " + score + " out of " + questionList.size())
                .setPositiveButton("Play Again", (dialog, which) -> {
                    // Reset quiz and start over
                    currentQuestionIndex = 0;
                    score = 0;
                    answerSelectedThisTurn = false;
                    // Shuffle questions again if you want a different order
                    // Collections.shuffle(questionList);
                    textViewQuestion.setVisibility(View.VISIBLE); // Make question text view visible again
                    textViewQuestion.setAlpha(1f); // Reset alpha
                    gridLayoutAnswers.setVisibility(View.VISIBLE);
                    gridLayoutAnswers.setAlpha(1f);
                    if (currentQuizMode.equals(MODE_NORMAL))
                    {
                        mediaSwitcherLayout.setVisibility(View.GONE);

                    }
                    else if (currentQuizMode.equals(MODE_HARD))
                    {
                        mediaSwitcherLayout.setVisibility(View.VISIBLE);
                    }
                    this.loadQuestionsInBackground();

                    /*if (questionList != null && !questionList.isEmpty()) {
                        displayQuestion();
                    } else {
                        textViewQuestion.setText("No questions loaded!");
                        Toast.makeText(this, "Error: No questions available.", Toast.LENGTH_LONG).show();
                        // Optionally, finish the activity or show an error dialog
                    }*/
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    // Close the activity or navigate away
                    finish();
                })
                .setCancelable(false) // Prevent dialog dismissal on back press or outside touch
                .show();

        // If you had a manual "Next Question" button, hide it or change its text
        // if (buttonNextQuestion != null) {
        //     buttonNextQuestion.setVisibility(View.GONE);
        // }
    }

    @Override
    public void onDefinitionsLoaded() {
        Log.d(TAG, "Achievement definitions loaded. Now checking achievements.");
        if (achievementManager != null) {
            achievementManager.checkAndAwardAchievements();

        }
    }
    @Override
    public void onDefinitionsLoadFailed() {
        Log.e(TAG, "Failed to load achievement definitions.");
        Toast.makeText(this, "Could not load achievement data. Please try again later.", Toast.LENGTH_LONG).show();
        // Handle the failure case, maybe disable achievement-related UI
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister listener to prevent memory leaks
        if (achievementManager != null) {
            achievementManager.unregisterDefinitionsLoadedListener(this);
        }
    }
    public boolean isOnSameWifiSubnet(Context context, String backendIp) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian int to string IP
        String deviceIp = String.format(
                "%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (ipAddress >> 24 & 0xff)
        );

        Log.d("DeviceIP", "Device IP: " + deviceIp);
        Log.d("BackendIP", "Backend IP: " + backendIp);

        // Extract subnet (first 3 octets)
        String deviceSubnet = deviceIp.substring(0, deviceIp.lastIndexOf('.'));
        String backendSubnet = backendIp.substring(0, backendIp.lastIndexOf('.'));

        // Check if subnets match
        return deviceSubnet.equals(backendSubnet);
    }
}