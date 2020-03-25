package com.tajidriver.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface VehicleDetailsDao {
    @Insert
    void addVehicle(VehicleDetails vehicleDetails);

    @Query("SELECT * FROM vehicleDetails LIMIT 1")
    VehicleDetails getVehicleDetails();
}
