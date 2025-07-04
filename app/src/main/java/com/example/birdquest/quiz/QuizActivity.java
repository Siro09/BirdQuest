// QuizActivity.java
package com.example.birdquest.quiz; // Or your desired package

import java.util.concurrent.ExecutorService; // Added
import java.util.concurrent.Executors;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.birdquest.R; // Your R file
import com.example.birdquest.db.AppDatabase;
import com.example.birdquest.db.BirdDao;
import com.example.birdquest.models.Bird;
import com.google.android.material.button.MaterialButton; // For MaterialButton styling

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.example.birdquest.GamificationManager;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final long DELAY_NEXT_QUESTION = 1500; // 1.5 seconds for auto-advance
    private ImageView imageViewBird; // Add this for displaying the bird image
    private AppDatabase appDb;

    // Executor for background database operations
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    // Handler to post results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private GridLayout gridLayoutAnswers;
    // private Button buttonNextQuestion; // Use if you want manual "Next" button

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answerSelectedThisTurn = false; // To prevent multiple answer clicks for one question

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.textViewQuestion);
        textViewScore = findViewById(R.id.textViewScore);
        textViewQuestionCount = findViewById(R.id.textViewQuestionCount);
        gridLayoutAnswers = findViewById(R.id.gridLayoutAnswers);
        imageViewBird = findViewById(R.id.imageViewBird); // Initialize ImageView
        // buttonNextQuestion = findViewById(R.id.buttonNextQuestion); // Uncomment if using manual next
        appDb = AppDatabase.getInstance(getApplicationContext());
        loadQuestionsInBackground(); // Load your quiz questions

        /*if (questionList != null && !questionList.isEmpty()) {
            displayQuestion();
        } else {
            textViewQuestion.setText("No questions loaded!");
            Toast.makeText(this, "Error: No questions available.", Toast.LENGTH_LONG).show();
            // Optionally, finish the activity or show an error dialog
        }*/

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

    private void loadQuestionsHardCoded() {
        // In a real app, load from a database, file, or network
        // For this example, we'll hardcode some questions
        questionList = new ArrayList<>();

        questionList.add(new Question("What is the main color of a Blue Jay?",
                Arrays.asList("Red", "Blue", "Green", "Yellow"), 1));
        questionList.add(new Question("Which bird is known for its ability to mimic sounds?",
                Arrays.asList("Sparrow", "Robin", "Mockingbird", "Eagle"), 2));
        questionList.add(new Question("What do flamingos primarily eat to get their pink color?",
                Arrays.asList("Fish", "Insects", "Shrimp & Algae", "Seeds"), 2));
        questionList.add(new Question("Which of these birds cannot fly?",
                Arrays.asList("Penguin", "Pigeon", "Pelican", "Parrot"), 0));
        questionList.add(new Question("What is the national bird of the United States?",
                Arrays.asList("Dove", "Bald Eagle", "Turkey", "Cardinal"), 1));

        // Shuffle questions for variety (optional)
         Collections.shuffle(questionList);

    }
    private void loadQuestionsInBackground() {
        questionList = new ArrayList<>();
        databaseExecutor.execute(() -> {
            // --- This block runs on a background thread ---
            final List<Question> tempQuestionList = new ArrayList<>();
            boolean success = true;
            String errorMessage = null;

            try {
                BirdDao birdDao = appDb.birdDao(); // Get DAO instance

                List<Bird> quizBirds = birdDao.getRandomBirdsWithImages(10); // DB CALL

                if (quizBirds == null || quizBirds.size() < 4) {
                    Log.e(TAG, "Not enough birds with images in the database. Found: " + (quizBirds == null ? 0 : quizBirds.size()));
                    success = false;
                    errorMessage = "Not enough bird data for the quiz.";
                } else {
                    List<String> allCommonNames = birdDao.getAllBirdCommonNamesWithImages(); // DB CALL
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

                            String questionText = "Identify this bird:";
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
                            tempQuestionList.add(new Question(questionText, options, correctAnswerIndex, correctBird.getImageUrl()));
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
            // --- End of background thread block ---

            // Now, post the result back to the main thread
            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;
            mainThreadHandler.post(() -> {
                // --- This block runs on the main thread ---
                // if (progressBar != null) progressBar.setVisibility(View.GONE);

                if (finalSuccess && !tempQuestionList.isEmpty()) {
                    questionList.clear();
                    questionList.addAll(tempQuestionList);
                    Collections.shuffle(questionList); // Shuffle the final list
                    Log.i(TAG, "Successfully generated " + questionList.size() + " questions.");
                    displayQuestion();
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
        /** bad database calls dont use this method for it will CRASH main thread
         *
        *
         */
    private void loadQuestions()
    {

        questionList = new ArrayList<>();

        BirdDao birdDao = appDb.birdDao();

        List<Bird> quizBirds = birdDao.getRandomBirdsWithImages(10);

        if (quizBirds == null || quizBirds.size() < 4) { // Need at least 4 birds for 1 correct + 3 distractors
            Log.e(TAG, "Not enough birds with images in the database to create a quiz. Found: " + (quizBirds == null ? 0 : quizBirds.size()));
            Toast.makeText(this, "Not enough bird data for the quiz.", Toast.LENGTH_LONG).show();
            // Handle this case: show error, finish activity, etc.
            textViewQuestion.setText("Error: Not enough bird data for quiz.");
            return;
        }

        // 2. Get all common names to use as potential distractors
        List<String> allCommonNames = birdDao.getAllBirdCommonNames();
        if (allCommonNames == null || allCommonNames.isEmpty()) {
            Log.e(TAG, "No common names found in the database for distractors.");
            Toast.makeText(this, "Error loading distractor names.", Toast.LENGTH_LONG).show();
            return;
        }

        Random random = new Random();
        for (Bird correctBird : quizBirds) {
            if (correctBird.getCommonName() == null || correctBird.getCommonName().trim().isEmpty() ||
                    correctBird.getImageUrl() == null || correctBird.getImageUrl().trim().isEmpty()) {
                Log.w(TAG, "Skipping bird with missing name or image URL: " + correctBird.commonName);
                continue; // Skip this bird if essential info is missing
            }

            String questionText = "Identify this bird:"; // Or store image URL here if Question model changes
            List<String> options = new ArrayList<>();
            options.add(correctBird.getCommonName()); // Add the correct answer

            // 3. Select 3 unique distractor names
            Set<String> distractorSet = new HashSet<>();
            distractorSet.add(correctBird.getCommonName()); // Ensure distractors are different from correct answer

            while (options.size() < 4 && distractorSet.size() < allCommonNames.size()) {
                String randomDistractorName = allCommonNames.get(random.nextInt(allCommonNames.size()));
                if (!distractorSet.contains(randomDistractorName)) {
                    options.add(randomDistractorName);
                    distractorSet.add(randomDistractorName);
                }
            }
            // If not enough unique distractors, fill with any available (less ideal)
            // This might happen if allCommonNames is very small
            int currentOptionSize = options.size();
            for(int i=0; i < (4-currentOptionSize) && i < allCommonNames.size(); i++){
                if(!options.contains(allCommonNames.get(i))){
                    options.add(allCommonNames.get(i));
                }
            }
            Collections.shuffle(options); // Shuffle the order of options

            int correctAnswerIndex = options.indexOf(correctBird.getCommonName());

            // You might need to adjust your Question class to store the image URL
            // or pass it separately to displayQuestion.
            // For now, let's assume Question can store an identifier or the image URL directly.
            // If Question stores image URL: new Question(questionText, options, correctAnswerIndex, correctBird.getImageUrl())
            // If Question only stores text: new Question(questionText, options, correctAnswerIndex)
            // and you'll fetch the bird again in displayQuestion or pass the bird object.
            // Let's modify Question to hold an image URL.

            questionList.add(new Question(questionText, options, correctAnswerIndex, correctBird.getImageUrl()));
        }
        if (questionList.isEmpty() && !quizBirds.isEmpty()) {
            Log.w(TAG, "Finished processing birds, but questionList is still empty. This might be due to all birds failing validation or lacking enough distractors.");
        } else if (!questionList.isEmpty()) {
            Collections.shuffle(questionList); // Shuffle the order of all generated questions
            Log.i(TAG, "Successfully generated " + questionList.size() + " questions for the quiz.");
        }
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

            // Handle Image Display
            if (currentQuestion.getImageUrl() != null) {
                imageViewBird.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(currentQuestion.getImageUrl())
                        .placeholder(R.drawable.ic_bird_placeholder) // Optional: a placeholder image
                        .error(R.drawable.ic_launcher_foreground)       // Optional: an error image if load fails
                        .into(imageViewBird);
            } else {
                imageViewBird.setVisibility(View.GONE); // Hide if no image URL
                Log.w(TAG, "No image URL for question: " + currentQuestion.getQuestionText());
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
                params.setMargins(8,8,8,8); // Or use margins from item_answer_choice.xml
                answerButton.setLayoutParams(params);

                gridLayoutAnswers.addView(answerButton);

            }
            gridLayoutAnswers.requestLayout(); // Ask the grid to re-measure and re-layout
            gridLayoutAnswers.invalidate();   // Ask the grid to redraw
            // Fade in animation for answer grid
            gridLayoutAnswers.animate().alpha(1f).setDuration(500).start();

            // If using manual next button, hide it until an answer is selected
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

        if (isCorrect) {
            score++;
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


        //add xp
        GamificationManager.addXP(this,score * 5);

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
}