package com.example.birdquest.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

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

    // Constructors
    public Bird(String commonName, String latinName, String sireUrl, String imageUrl) {
        this.commonName = commonName;
        this.latinName = latinName;
        this.siteUrl = sireUrl;
        this.imageUrl = imageUrl;
    }
    public Bird(String commonName, String latinName, String sireUrl) {
        this.commonName = commonName;
        this.latinName = latinName;
        this.siteUrl = sireUrl;
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

