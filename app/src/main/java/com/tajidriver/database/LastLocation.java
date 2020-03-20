package com.tajidriver.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lastLocation")
public class LastLocation {
    public LastLocation(@NonNull String locationId, @NonNull String locationLat,
        @NonNull String locationLng, @NonNull String locationDate) {

        this.locationId = locationId;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.locationDate = locationDate;
    }

    @PrimaryKey/*(autoGenerate = true)*/
    @NonNull
    @ColumnInfo(name = "location_id")
    public String locationId;

    @ColumnInfo(name = "location_lat")
    @NonNull
    public String locationLat;

    @ColumnInfo(name = "locationLng")
    @NonNull
    public String locationLng;

    @ColumnInfo(name = "location_date")
    @NonNull
    public String locationDate;

    @NonNull
    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(@NonNull String locationId) {
        this.locationId = locationId;
    }

    @NonNull
    public String getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(@NonNull String locationLat) {
        this.locationLat = locationLat;
    }

    @NonNull
    public String getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(@NonNull String locationLng) {
        this.locationLng = locationLng;
    }

    @NonNull
    public String getLocationDate() {
        return locationDate;
    }

    public void setLocationDate(@NonNull String locationDate) {
        this.locationDate = locationDate;
    }
}
