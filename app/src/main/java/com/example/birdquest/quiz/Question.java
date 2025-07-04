// Question.java
package com.example.birdquest.quiz; // Or your desired package

import java.util.List;

public class Question {
    private String questionText;
    private List<String> answerOptions;
    private int correctAnswerIndex; // Index of the correct answer in answerOptions
    private String imageUrl; // New field to store the image URL
    public Question(String questionText, List<String> answerOptions, int correctAnswerIndex) {
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
    }
    Question(String questionText, List<String> answerOptions, int correctAnswerIndex, String imageUrl) {
        this.questionText = questionText;
        this.answerOptions = answerOptions;
        this.correctAnswerIndex = correctAnswerIndex;
        this.imageUrl = imageUrl;
    }
    public String getQuestionText() {
        return questionText;
    }

    public List<String> getAnswerOptions() {
        return answerOptions;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getCorrectAnswer() {
        if (correctAnswerIndex >= 0 && correctAnswerIndex < answerOptions.size()) {
            return answerOptions.get(correctAnswerIndex);
        }
        return null;
    }
}