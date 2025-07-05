package com.example.birdquest.models;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties; // For Firestore

import java.util.ArrayList;
import java.util.Collection;

@IgnoreExtraProperties // Good practice for Firestore
public class User {

    private String email;
    private int xp;
    private int level;
    // Counters for achievement tracking
    private int quizCompletions;
    private int perfectQuizScores;
    @Exclude
    private Collection<IdentifiedBird> uniqueCorrectBirdsIdentified; // Map of birdId to true
    @Exclude
    private Collection<Achievement> achievements;
    // Default constructor is required for calls to DataSnapshot.getValue(User.class)
    // and for Firestore's toObject(User.class)
    public User() {
        this.uniqueCorrectBirdsIdentified = new ArrayList<IdentifiedBird>();
        this.achievements = new ArrayList<Achievement>();
    }

    public User(String email, int xp, int level) {
        this.email = email;
        this.xp = xp;
        this.level = level;
        this.quizCompletions = 0;
        this.perfectQuizScores = 0;
        this.uniqueCorrectBirdsIdentified =  new ArrayList<>();
        this.achievements = new ArrayList<Achievement>();
    }

    public String getEmail() {
        return email;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

   public void setEmail(String email) {
        this.email = email;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setLevel(int level) {
        this.level = level;
    }
    public  void setQuizCompletions(int quizCompletions) {
        this.quizCompletions = quizCompletions;

    }
    public Collection<IdentifiedBird>  getUniqueCorrectBirdsIdentified() {
        return uniqueCorrectBirdsIdentified;
    }

    public void setUniqueCorrectBirdsIdentified(Collection<IdentifiedBird> uniqueCorrectBirdsIdentified) {
        this.uniqueCorrectBirdsIdentified = uniqueCorrectBirdsIdentified;
    }

    public int getPerfectQuizScores() {
        return perfectQuizScores;
    }

    public void setPerfectQuizScores(int perfectQuizScores) {
        this.perfectQuizScores = perfectQuizScores;
    }

    public int getQuizCompletions() {
        return quizCompletions;
    }

    public Collection<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(Collection<Achievement> achievements) {
        this.achievements = achievements;
    }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", xp=" + xp +
                ", level=" + level +
                ", quizCompletions=" + quizCompletions +
                ", perfectQuizScores=" + perfectQuizScores +
                ", uniqueCorrectBirdsIdentified=" + uniqueCorrectBirdsIdentified +
                ", achievements=" + achievements +
                '}';
    }

    public int getUniqueCorrectBirdsCount() {
        return uniqueCorrectBirdsIdentified.size();
    }
}