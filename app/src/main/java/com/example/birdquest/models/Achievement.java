// AchievementDefinition.java
package com.example.birdquest.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Achievement {
    @Exclude
    private String id; // Document ID from Firestore
    private String name;
    private String description;
    private long xpReward;
    private String criteriaType; // e.g., "QUIZ_COMPLETIONS", "UNIQUE_BIRDS_IDENTIFIED", "PERFECT_QUIZZES"
    private long criteriaValue;
    @Exclude
    private String iconUrl; // Optional

    public Achievement() {
        // Firestore constructor
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getXpReward() { return xpReward; }
    public String getCriteriaType() { return criteriaType; }
    public long getCriteriaValue() { return criteriaValue; }
    public String getIconUrl() { return iconUrl; }

    // Setters (mainly for manual creation or if you map from DocumentSnapshot manually)
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setXpReward(long xpReward) { this.xpReward = xpReward; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }
    public void setCriteriaValue(long criteriaValue) { this.criteriaValue = criteriaValue; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}