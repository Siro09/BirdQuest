package com.example.birdquest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.birdquest.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editTextRegisterEmail;
    private EditText editTextRegisterPassword;
    private EditText editTextRegisterConfirmPassword; // Optional
    private Button buttonRegister;
    private ProgressBar progressBarRegister;
    private TextView textViewRegisterError;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mFirestore  = FirebaseFirestore.getInstance();
        // Initialize UI elements
        editTextRegisterEmail = findViewById(R.id.editTextRegisterEmail);
        editTextRegisterPassword = findViewById(R.id.editTextRegisterPassword);
        editTextRegisterConfirmPassword = findViewById(R.id.editTextRegisterConfirmPassword); // Optional
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBarRegister = findViewById(R.id.progressBarRegister);
        textViewRegisterError = findViewById(R.id.textViewRegisterError);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String email = editTextRegisterEmail.getText().toString().trim();
        String password = editTextRegisterPassword.getText().toString().trim();
        String confirmPassword = editTextRegisterConfirmPassword.getText().toString().trim(); // Optional

        // Input Validations
        if (TextUtils.isEmpty(email)) {
            editTextRegisterEmail.setError("Email is required.");
            editTextRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextRegisterEmail.setError("Please enter a valid email.");
            editTextRegisterEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextRegisterPassword.setError("Password is required.");
            editTextRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 6) { // Firebase default minimum password length
            editTextRegisterPassword.setError("Password must be at least 6 characters.");
            editTextRegisterPassword.requestFocus();
            return;
        }
        if (!(password.matches(".*[A-Z].*") && password.matches(".*[a-z].*") && password.matches(".*\\d.*"))) {
            editTextRegisterPassword.setError("Password must contain at least 1 uppercase letter, 1 lowercase letter, and 1 number.");
            editTextRegisterPassword.requestFocus();
            return;
        }

        // Optional: Confirm Password Validation
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextRegisterConfirmPassword.setError("Confirm password is required.");
            editTextRegisterConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextRegisterConfirmPassword.setError("Passwords do not match.");
            editTextRegisterConfirmPassword.requestFocus();

            return;
        }
        // End Input Validations

        progressBarRegister.setVisibility(View.VISIBLE);
        textViewRegisterError.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarRegister.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Registration successful.",
                                    Toast.LENGTH_SHORT).show();

                            // Optional: Send email verification
                            // user.sendEmailVerification();
                            String userId = "";

                            if (firebaseUser != null) {
                                userId = firebaseUser.getUid();
                                // new user map for Firestore
                                Map<String, Object> newUser = new HashMap<>();
                                newUser.put("email", email);
                                newUser.put("level", 1);
                                newUser.put("xp", 0);
                                newUser.put("quizCompletions", 0);
                                newUser.put("perfectQuizScores", 0);
                                newUser.put("uniqueCorrectBirdsIdentifiedCount", 0);

                                mFirestore.collection("users").document(userId).set(newUser)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> dbTask) {

                                                if (dbTask.isSuccessful()) {
                                                    Log.d(TAG, "User data added to Database successfully.");
                                                } else {
                                                    Log.e(TAG, "Failed to add user data to Database.", dbTask.getException());
                                                }
                                            }
                                        });
                            }

                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            // Clear the activity stack so user can't go back to register screen with back button
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                            // Or directly to MainActivity
                            // Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            // startActivity(intent);
                            // finish();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            textViewRegisterError.setText("Authentication failed: " + task.getException().getMessage());
                            textViewRegisterError.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
}