package com.example.birdquest.models;
import com.google.firebase.firestore.IgnoreExtraProperties; // For Firestore

@IgnoreExtraProperties // Good practice for Firestore too
public class User {

    private String email;
    private int xp;      // Using long for XP in case it gets very large
    private int level;

    // Default constructor is required for calls to DataSnapshot.getValue(User.class)
    // and for Firestore's toObject(User.class)
    public User() {
    }

    public User(String email, int xp, int level) {
        this.email = email;
        this.xp = xp;
        this.level = level;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    // Setters (optional, but can be useful if you modify user objects locally before saving)
    public void setEmail(String email) {
        this.email = email;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    // You might want to add other methods if needed, e.g.,
    // public void addXp(long amount) {
    //     this.xp += amount;
    //     // Potentially update level here based on new XP
    // }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", xp=" + xp +
                ", level=" + level +
                '}';
    }
}