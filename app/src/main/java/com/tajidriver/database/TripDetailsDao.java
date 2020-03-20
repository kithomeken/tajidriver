package com.tajidriver.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TripDetailsDao {
    @Insert
    void createTripRequest(TripDetails tripDetails);

    @Query("SELECT * FROM tripDetails WHERE trip_state = 'A'")
    TripDetails getActiveTripDetails();

    @Query("SELECT * FROM tripDetails WHERE trip_state = 'NP'")
    TripDetails requestTripData();

    @Update(onConflict = OnConflictStrategy.IGNORE)
    void updateTripDetails(TripDetails tripDetails);

    @Query("SELECT * FROM tripDetails WHERE trip_id = :tripId")
    TripDetails getTripDetails(String tripId);
}
