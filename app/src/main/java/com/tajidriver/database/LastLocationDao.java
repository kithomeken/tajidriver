package com.tajidriver.database;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface LastLocationDao {
    @Insert
    void createTripRequest(TripDetails tripDetails);

//    @Query("SELECT * FROM userDetails LIMIT 1")
//    UserDetails getUserDetails();
}
