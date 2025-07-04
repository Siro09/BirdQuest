package com.example.birdquest.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.birdquest.models.Bird;

import java.util.List;

// BirdDao.java (Example)
// Example BirdDao.java
@Dao
public interface BirdDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE) // Or REPLACE, depending on desired behavior
    void insert(Bird bird);


    @Delete
    void delete(Bird bird);
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(List<Bird> birds);

    @Query("SELECT * FROM birds")
    LiveData<List<Bird>> getAllBirds(); // Example: Get all birds as LiveData
    @Query("SELECT * FROM birds ORDER BY common_name ASC")
    LiveData<List<Bird>> getAllBirdsLivedataASC();
    @Query("SELECT * FROM birds ORDER BY common_name DESC")
    LiveData<List<Bird>> getAllBirdsLivedataDESC();
    @Query("SELECT * FROM birds WHERE image_url IS NOT NULL AND (common_name LIKE '%'||:query||'%' OR latin_name LIKE '%'||:query||'%') ORDER BY common_name ASC")
    LiveData<List<Bird>> searchBirdsByNameOrSpecies(String query);
    @Query("SELECT * FROM birds WHERE image_url IS NOT NULL ORDER BY common_name ASC")
    LiveData<List<Bird>> getAllBirdsLiveDataImageASC();

    /** Get count number of random birds with images
     * @param count
     * @return
     */
    @Query("SELECT * FROM birds WHERE image_url IS NOT NULL AND image_url != '' ORDER BY RANDOM() LIMIT :count")
    List<Bird> getRandomBirdsWithImages(int count);

    @Query("SELECT common_name FROM birds")
    List<String> getAllBirdCommonNames();

    @Query("SELECT common_name FROM birds WHERE image_url IS NOT NULL AND image_url != ''")
    List<String> getAllBirdCommonNamesWithImages();
    @Query("SELECT * FROM birds WHERE id = :birdId") // If you need to get a bird by ID
    Bird getBirdById(int birdId);
    @Query("SELECT * FROM birds WHERE common_name LIKE :name OR latin_name LIKE :name")
    LiveData<List<Bird>> findBirdsByName(String name); // Example search

    @Query("SELECT COUNT(*) FROM birds")
    int getBirdCount();
}