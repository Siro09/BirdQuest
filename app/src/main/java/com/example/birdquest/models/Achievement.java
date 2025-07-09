// AchievementDefinition.java
package com.example.birdquest.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Achievement {
    @Exclude
    private String id; // Document ID from Firestore
    private String name;
    private String description;
    private long xpReward;
    private String criteriaType; // e.g., "QUIZ_COMPLETIONS", "UNIQUE_BIRDS_IDENTIFIED", "PERFECT_QUIZZES"
    private long criteriaValue;

    private Date unlockedAt;
    @Exclude
    private String iconUrl; // Optional

    public Achievement() {
        // Firestore constructor
    }
    public Achievement(Achievement achievement, Date unlockedAt) {
        this.id = achievement.getId();
        this.name = achievement.getName();
        this.description = achievement.getDescription();
        this.xpReward = achievement.getXpReward();
        this.criteriaType = achievement.getCriteriaType();
        this.unlockedAt = unlockedAt;
    }
    public Date getUnlockedAt() {
        return unlockedAt;
    }

    public void setUnlockedAt(Date unlockedAt) {
        this.unlockedAt = unlockedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getXpReward() { return xpReward; }
    public String getCriteriaType() { return criteriaType; }
    public long getCriteriaValue() { return criteriaValue; }
    public String getIconUrl() { return iconUrl; }


    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setXpReward(long xpReward) { this.xpReward = xpReward; }
    public void setCriteriaType(String criteriaType) { this.criteriaType = criteriaType; }
    public void setCriteriaValue(long criteriaValue) { this.criteriaValue = criteriaValue; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
}