package com.example.birdquest.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "birds",
        indices = { // Optional: Add indices for faster queries if you search by these often
                @Index(value = "common_name", unique = false), // Set unique = true if common names must be unique
                @Index(value = "latin_name", unique = false)  // Set unique = true if Latin names must be unique
        })
public class Bird {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "common_name")
    public String commonName;

    @ColumnInfo(name = "latin_name")
    public String latinName;

    @ColumnInfo(name = "site_url")
    public String siteUrl; // We can populate this later or make it nullable

    @ColumnInfo(name = "image_url")
    public String imageUrl; // We can populate this later or make it nullable
    @ColumnInfo(name = "sound_url")
    public String soundUrl; // We can populate this later or make it nullable
    @ColumnInfo(name = "distribution_url")
    public String distributionUrl; // We can populate this later or make it nullable

    // Constructors
    public Bird(String commonName, String latinName, String siteUrl, String imageUrl,String soundUrl, String distributionUrl) {
        this.commonName = commonName;
        this.latinName = latinName;
        this.siteUrl = siteUrl;
        this.imageUrl = imageUrl;
        this.soundUrl = soundUrl;
        this.distributionUrl = distributionUrl;
    }
    public Bird(String commonName, String latinName, String siteUrl, String imageUrl,String soundUrl) {
        this.commonName = commonName;
        this.latinName = latinName;
        this.siteUrl = siteUrl;
        this.imageUrl = imageUrl;
        this.soundUrl = soundUrl;
    }
    public Bird(String commonName, String latinName, String siteUrl, String imageUrl) {
        this.commonName = commonName;
        this.latinName = latinName;
        this.siteUrl = siteUrl;
        this.imageUrl = imageUrl;
    }
    public Bird(String commonName, String latinName, String siteUrl) {
        this.commonName = commonName;
        this.latinName = latinName;
        this.siteUrl = siteUrl;
        this.imageUrl = null;
    }


    public Bird() {}

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getLatinName() {
        return latinName;
    }

    public void setLatinName(String latinName) {
        this.latinName = latinName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDistributionUrl() {
        return distributionUrl;
    }

    public void setDistributionUrl(String distributionUrl) {
        this.distributionUrl = distributionUrl;
    }

    public String getSoundUrl() {
        return soundUrl;
    }

    public void setSoundUrl(String soundUrl) {
        this.soundUrl = soundUrl;
    }

    // toString() method for debugging (optional)
    @Override
    public String toString() {
        return "Bird{" +
                "id=" + id +
                ", commonName='" + commonName + '\'' +
                ", latinName='" + latinName + '\'' +
                ", imageUrl='" + siteUrl + '\'' +
                '}';
    }
}

