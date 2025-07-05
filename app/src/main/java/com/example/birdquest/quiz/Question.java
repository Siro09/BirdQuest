// Question.java
package com.example.birdquest.quiz; // Or your desired package

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Question implements Parcelable {
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

    // Parcelable constructor
    protected Question(Parcel in) {
        questionText = in.readString();
        answerOptions = in.createStringArrayList();
        correctAnswerIndex = in.readInt();
        imageUrl = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(questionText);
        dest.writeStringList(answerOptions);
        dest.writeInt(correctAnswerIndex);
        dest.writeString(imageUrl);
    }
    // Parcelable creator
    public static final Creator<Question> CREATOR = new Creator<Question>() {
        @Override
        public Question createFromParcel(Parcel in) {
            return new Question(in);
        }

        @Override
        public Question[] newArray(int size) {
            return new Question[size];
        }
    };

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