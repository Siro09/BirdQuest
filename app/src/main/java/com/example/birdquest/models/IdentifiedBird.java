package com.example.birdquest.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class IdentifiedBird {
    private Boolean isIdentified;
    @Exclude
    private String id;

    // Default constructor is required for calls to DataSnapshot.getValue(IdentifiedBird.class)
    public IdentifiedBird() {
        // Default constructor required for calls to DataSnapshot.getValue(IdentifiedBird.class)
    }

    public IdentifiedBird(Boolean isIdentified) {
        this.isIdentified = isIdentified;
    }

    public Boolean getIdentified() {
        return isIdentified;
    }

    public void setIdentified(Boolean identified) {
        isIdentified = identified;
    }
}
