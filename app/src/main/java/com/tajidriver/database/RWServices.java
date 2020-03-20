package com.tajidriver.database;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.tajidriver.configuration.TajiCabs;
import com.tajidriver.global.Variables;

import java.util.UUID;

public class RWServices {
    private final String TAG = RWServices.class.getName();

    private UserDetailsDao userDetailsDao;
    private TripDetailsDao tripDetailsDao;

    public RWServices(AppDatabase appDatabase) {
        userDetailsDao = appDatabase.userDetailsDao();
        tripDetailsDao = appDatabase.tripDetailsDao();
    }

    public void createUser(@NonNull String email, @NonNull String first_name,
        @NonNull String last_name, @NonNull String phone_number, @NonNull String firebaseToken,
        String vehicleMake, String vehicleRegNo) {

        final String userId = UUID.randomUUID().toString();
        UserDetails userDetails = new UserDetails(userId, email, first_name, last_name,
            phone_number, firebaseToken, vehicleMake, vehicleRegNo);

        new createUserAsyncTask(userDetailsDao).execute(userDetails);
    }

    public void getUserDetails() {
        UserDetails userDetails = userDetailsDao.getUserDetails();

        String emailAdd = (userDetails == null) ? "No Data Found" : userDetails.getEmail();
        String firstName = (userDetails == null) ? "No Data Found" : userDetails.getFirstName();
        String lastName = (userDetails == null) ? "No Data Found" : userDetails.getLastName();
        String phoneNumber = (userDetails == null) ? "No Data Found" : userDetails.getPhoneNumber();
        String firebaseToken = (userDetails == null) ? "No Data Found" : userDetails.getFirebaseToken();
        String regNo = (userDetails == null) ? "No Registration No found" : userDetails.getVehicleRegNo();
        String vehicleMake = (userDetails == null) ? "No Vehicle Make Found" : userDetails.getVehicleMake();

        Variables.ACCOUNT_EMAIL = emailAdd;
        Variables.ACCOUNT_NAME = firstName + " " + lastName;
        Variables.ACCOUNT_PHONE = phoneNumber;
        Variables.VEHICLE_MAKE = vehicleMake;
        Variables.VEHICLE_REGNO = regNo;
        TajiCabs.FIREBASE_TOKEN = firebaseToken;
    }

    public void createTripRequest(@NonNull String trip_id, @NonNull String origin_name, @NonNull String origin_lat,
        @NonNull String origin_lng, @NonNull String destination_name, @NonNull String destination_lat,
        @NonNull String destination_lng, @NonNull String passenger_name, @NonNull String passenger_phone,
        @NonNull String trip_distance, @NonNull String trip_cost, @NonNull String final_destination,
        @NonNull String trip_date, @NonNull String trip_state) {

        TripDetails tripDetails = new TripDetails(trip_id, origin_name, origin_lat, origin_lng, destination_name,
                destination_lat, destination_lng, passenger_name, passenger_phone, trip_distance,
                trip_cost, final_destination, trip_date, trip_state);

        new createTripAsyncTask(tripDetailsDao).execute(tripDetails);
    }

    public String getActiveTrip() {
        TripDetails tripDetails = tripDetailsDao.getActiveTripDetails();

        String tripId = (tripDetails == null) ? "No Data Found" : tripDetails.getTrip_id();
        String originName = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_name();
        String originLat = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_lat();
        String originLng = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_lng();
        String destinationName = (tripDetails == null) ? "No Data Found" : tripDetails.getDestination_name();
        String detinsationLat = (tripDetails == null) ? "No Data Found" : tripDetails.getDestination_lat();
        String destinationLng = (tripDetails == null) ? "No Data Found" : tripDetails.getDestination_lng();
        String passengerName = (tripDetails == null) ? "No Data Found" : tripDetails.getPassenger_name();
        String passengerPhone = (tripDetails == null) ? "No Data Found" : tripDetails.getPassenger_phone();
        String tripDistance = (tripDetails == null) ? "No Data Found" : tripDetails.getTrip_distance();
        String tripCost = (tripDetails == null) ? "No Data Found" : tripDetails.getTrip_cost();

        return tripId;
    }

    @SuppressLint("StaticFieldLeak")
    private class createUserAsyncTask extends AsyncTask<UserDetails, Void, Void> {
        UserDetailsDao userDetailsDao;

        private createUserAsyncTask(UserDetailsDao userDetailsDao) {
            this.userDetailsDao = userDetailsDao;
        }

        @Override
        protected Void doInBackground(UserDetails... userDetails) {
            userDetailsDao.createNewUser(userDetails[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            getUserDetails();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class createTripAsyncTask extends AsyncTask<TripDetails, Void, Void> {
        TripDetailsDao tripDetailsDao;

        private createTripAsyncTask(TripDetailsDao tripDetailsDao) {
            this.tripDetailsDao = tripDetailsDao;
        }

        @Override
        protected Void doInBackground(TripDetails... tripDetails) {
            tripDetailsDao.createTripRequest(tripDetails[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    public void startTripUpdate(String tripId) {
        TripDetails tripDetails = tripDetailsDao.getTripDetails(tripId);

        if (tripDetails != null) {
            tripDetails.trip_state = "I";

            tripDetailsDao.updateTripDetails(tripDetails);
        }
    }

    public void endTripUpdate(String tripId) {
        TripDetails tripDetails = tripDetailsDao.getTripDetails(tripId);

        if (tripDetails != null) {
            tripDetails.trip_state = "E";

            tripDetailsDao.updateTripDetails(tripDetails);
        }
    }
}
