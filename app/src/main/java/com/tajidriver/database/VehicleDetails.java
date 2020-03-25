package com.tajidriver.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "vehicleDetails",
        indices = {@Index(value = "reg_no", unique = true)})
public class VehicleDetails {
    public VehicleDetails(@NonNull String vehicleId, @NonNull String vehicleRegNo,
        @NonNull String vehicleMake, @NonNull String vehicleModel, @NonNull String yearOfManuf,
        @NonNull String vehicleColor, @NonNull String seatingCapacity) {
        this.vehicleId = vehicleId;
        this.vehicleRegNo = vehicleRegNo;
        this.vehicleMake = vehicleMake;
        this.vehicleModel = vehicleModel;
        this.yearOfManuf = yearOfManuf;
        this.vehicleColor = vehicleColor;
        this.seatingCapacity = seatingCapacity;
    }

    @PrimaryKey/*(autoGenerate = true)*/
    @NonNull
    @ColumnInfo(name = "vehicle_id")
    public String vehicleId;

    @NonNull
    @ColumnInfo(name = "reg_no")
    public String vehicleRegNo;

    @ColumnInfo(name = "vehicle_make")
    @NonNull
    public String vehicleMake;

    @ColumnInfo(name = "vehicle_model")
    @NonNull
    public String vehicleModel;

    @ColumnInfo(name = "year_of_manuf")
    @NonNull
    public String yearOfManuf;

    @ColumnInfo(name = "vehicle_color")
    @NonNull
    public String vehicleColor;

    @ColumnInfo(name = "seating_capacity")
    @NonNull
    public String seatingCapacity;

    @NonNull
    public String getVehicleId() {
        return vehicleId;
    }

    @NonNull
    public String getVehicleRegNo() {
        return vehicleRegNo;
    }

    @NonNull
    public String getVehicleMake() {
        return vehicleMake;
    }

    @NonNull
    public String getVehicleModel() {
        return vehicleModel;
    }

    @NonNull
    public String getYearOfManuf() {
        return yearOfManuf;
    }

    @NonNull
    public String getVehicleColor() {
        return vehicleColor;
    }

    @NonNull
    public String getSeatingCapacity() {
        return seatingCapacity;
    }
}
