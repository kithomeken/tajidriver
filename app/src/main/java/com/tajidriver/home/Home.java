package com.tajidriver.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import com.tajidriver.R;
import com.tajidriver.auth.SignIn;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.database.TripDetails;
import com.tajidriver.database.TripDetailsDao;
import com.tajidriver.directions.TajiDirections;
import com.tajidriver.geolocation.LocationSharing;
import com.tajidriver.global.Variables;
import com.tajidriver.service.RequestServices;
import com.tajidriver.settings.ContactUs;
import com.tajidriver.settings.Settings;

import java.util.Objects;

import static com.tajidriver.configuration.TajiCabs.DEFAULT_ZOOM;
import static com.tajidriver.configuration.TajiCabs.DISTANCE;
import static com.tajidriver.configuration.TajiCabs.GOOGLE_API;
import static com.tajidriver.configuration.TajiCabs.REQUEST_LOCATION;

import static com.tajidriver.global.Variables.ACCOUNT_NAME;
import static com.tajidriver.global.Variables.PASSENGER_NAME;
import static com.tajidriver.global.Variables.PASSENGER_PHONE;
import static com.tajidriver.global.Variables.REQUEST_DESTINATION_LAT;
import static com.tajidriver.global.Variables.REQUEST_DESTINATION_LNG;
import static com.tajidriver.global.Variables.REQUEST_DESTINATION_NAME;
import static com.tajidriver.global.Variables.REQUEST_ORIGIN_LAT;
import static com.tajidriver.global.Variables.REQUEST_ORIGIN_LNG;
import static com.tajidriver.global.Variables.REQUEST_ORIGIN_NAME;
import static com.tajidriver.global.Variables.TRIP_COST;
import static com.tajidriver.global.Variables.TRIP_DISTANCE;
import static com.tajidriver.global.Variables.TRIP_ID;

public class Home extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener , OnMapReadyCallback {

    private static final String TAG = Home.class.getName();

    // Dependency classes
    TajiDirections tajiDirections = new TajiDirections();

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private AppDatabase appDatabase;
    private RWServices rwServices;

    protected static final int overview = 0;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private GoogleMap googleMap;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-1.2833 , 36.8167);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private Location mLastKnownLocation;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private GoogleApiClient googleApiClient;

    private ConstraintLayout noActivity, tripDetails, requestData;
    private FloatingActionButton geoLocation;
    private View requestTripPopUp, endTripPopUp;

    @Override
    public void onStart() {
        super.onStart();

        firebaseUser = firebaseAuth.getCurrentUser();
        checkFirebaseSession(firebaseUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_sreen);
        Variables.ACTIVITY_STATE = 0;

        // Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        appDatabase = AppDatabase.getDatabase(this);
        rwServices = new RWServices(appDatabase);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set display name, photo url and email address of signed in user
        View navHeaderView = navigationView.getHeaderView(0);
        TextView accountName = navHeaderView.findViewById(R.id.accountName);
        TextView accountEmail = navHeaderView.findViewById(R.id.accountEmail);

        assert firebaseUser != null;
        accountName.setText(ACCOUNT_NAME);
        accountEmail.setText(firebaseUser.getEmail());

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_API);
        }

        // Map Loading
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mLocationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Layouts
        noActivity = findViewById(R.id.noActivity);
        tripDetails = findViewById(R.id.tripDetails);
        geoLocation = findViewById(R.id.geo_location);
        requestData = findViewById(R.id.requestData);

        geoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableLoc();
                getDeviceLocation();
            }
        });

        noActivity.setVisibility(View.VISIBLE);
        requestData.setVisibility(View.GONE);
        tripDetails.setVisibility(View.GONE);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                geoLocation.getLayoutParams();
        layoutParams.setMargins(0, 0, 0,170);
        geoLocation.setLayoutParams(layoutParams);

        // Show trip request Modal
        if (Variables.TRIP_ID != null) {
            noActivity.setVisibility(View.GONE);
            tripDetails.setVisibility(View.GONE);
            requestData.setVisibility(View.VISIBLE);

            layoutParams = (CoordinatorLayout.LayoutParams)
                    geoLocation.getLayoutParams();
            layoutParams.setMargins(0, 0, 0,350 );
            geoLocation.setLayoutParams(layoutParams);

            TextView passengerDetails, fromDisp, toDisp, distanceDisp, costDisp;

            passengerDetails = findViewById(R.id.passengerDetails);
            fromDisp = findViewById(R.id.fromDisp);
            toDisp = findViewById(R.id.toDisp);
            distanceDisp = findViewById(R.id.distanceDisp);
            costDisp = findViewById(R.id.costDisp);

            String passengerString = PASSENGER_NAME + " " + PASSENGER_PHONE;
            String fromString = "From: " + REQUEST_ORIGIN_NAME;
            String toString = "To: " + REQUEST_DESTINATION_NAME;

            passengerDetails.setText(passengerString);
            fromDisp.setText(fromString);
            toDisp.setText(toString);
            distanceDisp.setText(TRIP_DISTANCE);
            costDisp.setText(TRIP_COST);

            Button acceptRequest = findViewById(R.id.acceptRequest);
            Button declineRequest = findViewById(R.id.declineRequest);

            acceptRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create Trip Request
                    rwServices.createTripRequest(TRIP_ID, REQUEST_ORIGIN_NAME, REQUEST_ORIGIN_LAT,
                            REQUEST_ORIGIN_LNG, REQUEST_DESTINATION_NAME, REQUEST_DESTINATION_LAT, REQUEST_DESTINATION_LNG,
                            PASSENGER_NAME, PASSENGER_PHONE, TRIP_DISTANCE, TRIP_COST,
                            " ", " ", "A");

                    Variables.VEHICLE_REGNO = rwServices.getVehicleRegNo();
                    Variables.VEHICLE_MAKE = rwServices.getVehicleMake();
                    rwServices.getUserDetails();

                    RequestServices requestServices = new RequestServices(getApplicationContext());
                    requestServices.acceptRide();

                    // Show Trip Details
                    showTripDetails();
                }
            });

            declineRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Resend request to server and pick other driver

                    noActivity.setVisibility(View.VISIBLE);
                    requestData.setVisibility(View.GONE);
                    tripDetails.setVisibility(View.GONE);

                    CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                            geoLocation.getLayoutParams();
                    layoutParams.setMargins(0, 0, 0,170);
                    geoLocation.setLayoutParams(layoutParams);
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();
        Intent intent;

        if (itemId == R.id.nav_settings) {
            if (Variables.ACTIVITY_STATE == 0) {
                Variables.ACTIVITY_STATE = 1;

                intent = new Intent(this, Settings.class);
                startActivity(intent);
            }
        } else if (itemId == R.id.sign_out) {
            if (Variables.ACTIVITY_STATE == 0) {
                Variables.ACTIVITY_STATE = 1;

                FirebaseAuth.getInstance().signOut();

                intent = new Intent(this, SignIn.class);
                startActivity(intent);
                finish();
            }
        } else if (itemId == R.id.nav_contacts) {
            if (Variables.ACTIVITY_STATE == 0) {
                Variables.ACTIVITY_STATE = 1;

                intent = new Intent(this, ContactUs.class);
                startActivity(intent);
            }
        }

        return false;
    }

    private void checkFirebaseSession(FirebaseUser user) {
        if (user == null) {
            //Return to SignInActivity
            Intent intent = new Intent(Home.this, SignIn.class);
            startActivity(intent);
            finish();
        }
    }

    private void showTripDetails() {
        noActivity.setVisibility(View.GONE);
        tripDetails.setVisibility(View.VISIBLE);
        requestData.setVisibility(View.GONE);

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                geoLocation.getLayoutParams();
        layoutParams.setMargins(0, 0, 0,400 );
        geoLocation.setLayoutParams(layoutParams);

        // Populate Trip Details
        TripDetailsDao tripDetailsDao = appDatabase.tripDetailsDao();
        TripDetails tripDetails = tripDetailsDao.getActiveTripDetails();

        String originName = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_name();
        String originLat = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_lat();
        String originLng = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_lng();
        String destinationName = (tripDetails == null) ? "No Data Found" : tripDetails.getDestination_name();
        String destinationLat = (tripDetails == null) ? "No Data Found" : tripDetails.getDestination_lat();
        String destinationLng = (tripDetails == null) ? "No Data Found" : tripDetails.getDestination_lng();
        final String passengerName = (tripDetails == null) ? "No Data Found" : tripDetails.getPassenger_name();
        final String passengerPhone = (tripDetails == null) ? "No Data Found" : tripDetails.getPassenger_phone();
        String stringDistance = (tripDetails == null) ? "No Data Found" : tripDetails.getTrip_distance();
        final String stringCost = (tripDetails == null) ? "No Data Found" : tripDetails.getTrip_cost();

        final TextView tripFrom, tripTo, tripDist, tripCost, tripPassengerName, tripPassengerPhone;

        tripFrom = findViewById(R.id.tripFrom);
        tripTo = findViewById(R.id.tripTo);
        tripDist = findViewById(R.id.tripDistance);
        tripCost = findViewById(R.id.tripCost);

        tripFrom.setText(originName);
        tripTo.setText(destinationName);
        tripDist.setText(stringDistance);
        tripCost.setText(stringCost);

        tripPassengerName = findViewById(R.id.tripPassengerName);
        tripPassengerPhone = findViewById(R.id.tripPassengerPhone);
        tripPassengerName.setText(passengerName);
        tripPassengerPhone.setText(passengerPhone);

        double oLat = Double.parseDouble(originLat);
        double oLng = Double.parseDouble(originLng);
        double dLat = Double.parseDouble(destinationLat);
        double dLng = Double.parseDouble(destinationLng);

        LatLng originLatLng = new LatLng(oLat, oLng);
        LatLng destinationLatLng = new LatLng(dLat, dLng);

        // Draw Directions on Map
        drawDirection(originLatLng, destinationLatLng);

        final Button startTrip, endTrip;
        startTrip = findViewById(R.id.startTrip);
        endTrip = findViewById(R.id.endTrip);

        final String tripID = (tripDetails == null) ? "No Data Found" : tripDetails.getOrigin_name();
        final String tripState = (tripDetails == null) ? "No Data Found" : tripDetails.getTripState();

        Log.e(TAG, "TRIP STATE: " + tripState);
        if (tripState.equalsIgnoreCase("A")) {
            startTrip.setVisibility(View.VISIBLE);
            endTrip.setVisibility(View.GONE);
        } else {
            startTrip.setVisibility(View.GONE);
            endTrip.setVisibility(View.VISIBLE);
        }

        startTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rwServices.startTripUpdate(tripID);
                // Send Notification to Passenger
                RequestServices requestServices = new RequestServices(getApplicationContext());
                requestServices.startTrip(passengerPhone);

                Toast.makeText(Home.this, "Trip started for " + passengerName, Toast.LENGTH_SHORT).show();
                startTrip.setVisibility(View.GONE);
                endTrip.setVisibility(View.VISIBLE);
            }
        });

        endTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TripDetailsDao tripDetailsDao = appDatabase.tripDetailsDao();
                TripDetails tripDetails = tripDetailsDao.getActiveTripDetails();
                String ended = "E";

                if (tripDetails != null) {
                    tripDetails.trip_state = ended;
                    tripDetailsDao.updateTripDetails(tripDetails);
                }

                RequestServices requestServices = new RequestServices(getApplicationContext());
                requestServices.endRide(passengerPhone, stringCost);

                googleMap.clear();
                showEndTripPopUp(view, stringCost);
            }
        });
    }

    private void drawDirection(LatLng originLatLng, LatLng destinationLatLng) {
        googleMapsUISetting(googleMap);

        DirectionsResult directionsResult = tajiDirections
                .getDirectionsDetails(originLatLng, destinationLatLng, TravelMode.DRIVING);

        if (directionsResult != null) {
            tajiDirections.addPolyline(directionsResult, googleMap);
            tajiDirections.positionCamera(directionsResult.routes[overview], googleMap);
            tajiDirections.addMarkersToMap(directionsResult, googleMap);
        }

        DISTANCE = tajiDirections.distanceInMeters(directionsResult);
    }

    private void showEndTripPopUp(View view, String tripCost){
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        noActivity.setVisibility(View.GONE);
        tripDetails.setVisibility(View.GONE);
        requestData.setVisibility(View.GONE);

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        endTripPopUp = layoutInflater.inflate(R.layout.pop_up_end_trip, null);

        TextView tripAmount;
        tripAmount = endTripPopUp.findViewById(R.id.tripAmount);
        tripAmount.setText(tripCost);

        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(endTripPopUp, width, height, focusable);
        popupWindow.setElevation(8);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        endTripPopUp.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.isShowing();

                noActivity.setVisibility(View.GONE);
                tripDetails.setVisibility(View.GONE);
                requestData.setVisibility(View.GONE);
                return true;
            }
        });

        Button closePopUp = endTripPopUp.findViewById(R.id.closePopUp);
        closePopUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                noActivity.setVisibility(View.VISIBLE);
                tripDetails.setVisibility(View.GONE);
                requestData.setVisibility(View.GONE);
            }
        });
    }

    private void showRequestTripPopUp(View view){
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        TextView passengerDetails, fromDisp, toDisp, distanceDisp, costDisp;

        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        requestTripPopUp = layoutInflater.inflate(R.layout.pop_up_request_data, null);

        passengerDetails = requestTripPopUp.findViewById(R.id.passengerDetails);
        fromDisp = requestTripPopUp.findViewById(R.id.fromDisp);
        toDisp = requestTripPopUp.findViewById(R.id.toDisp);
        distanceDisp = requestTripPopUp.findViewById(R.id.distanceDisp);
        costDisp = requestTripPopUp.findViewById(R.id.costDisp);

        String passengerString = PASSENGER_NAME + " " + PASSENGER_PHONE;
        String fromString = "From: " + REQUEST_ORIGIN_NAME;
        String toString = "To: " + REQUEST_DESTINATION_NAME;

        passengerDetails.setText(passengerString);
        fromDisp.setText(fromString);
        toDisp.setText(toString);
        distanceDisp.setText(TRIP_DISTANCE);
        costDisp.setText(TRIP_COST);

        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(requestTripPopUp, width, height, focusable);
        popupWindow.setElevation(8);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        requestTripPopUp.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                popupWindow.dismiss();
                return true;
            }
        });

        Button acceptRequest = requestTripPopUp.findViewById(R.id.acceptRequest);
        Button declineRequest = requestTripPopUp.findViewById(R.id.declineRequest);

        acceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create Trip Request
                rwServices.createTripRequest(TRIP_ID, REQUEST_ORIGIN_NAME, REQUEST_ORIGIN_LAT,
                REQUEST_ORIGIN_LNG, REQUEST_DESTINATION_NAME, REQUEST_DESTINATION_LAT, REQUEST_DESTINATION_LNG,
                PASSENGER_NAME, PASSENGER_PHONE, TRIP_DISTANCE, TRIP_COST,
                " ", " ", "A");

                // Show Trip Details
                showTripDetails();

                popupWindow.dismiss();
            }
        });

        declineRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Resend request to server and pick other driver

                popupWindow.dismiss();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get Current Location of Device
        getDeviceLocation();

        // Check if there's an active trip
        String isActive = rwServices.getActiveTrip();
        if (!isActive.equalsIgnoreCase("No Data Found")) {
            showTripDetails();
        }
    }

    private void googleMapsUISetting(GoogleMap mMap) {
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(false);

        UiSettings mUiSettings = mMap.getUiSettings();

        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(false);
        mUiSettings.setScrollGesturesEnabled(true);

        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }

        updateLocationUI();
    }

    public void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastKnownLocation = task.getResult();

                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            Log.e(TAG, "====================================" + mLastKnownLocation.getLatitude());
                            Log.e(TAG, "====================================" + mLastKnownLocation.getLongitude());

                            final String locationLat = "" + mLastKnownLocation.getLatitude();
                            final String locationLng = "" + mLastKnownLocation.getLongitude();

                            String taxiRegNo = rwServices.getTaxiRegNo();
                            String tripDetails = rwServices.getActiveTrip();

                            if (!taxiRegNo.equalsIgnoreCase("No Data Found")) {
                                mLocationRequest = new LocationRequest();
                                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                                final Handler ha=new Handler();
                                ha.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //call function

                                        ha.postDelayed(this, 10000);

                                        // Location Sharing
                                        // Share Location of Driver
                                        LocationSharing locationSharing = new LocationSharing(getApplicationContext(), locationLat, locationLng);
                                        locationSharing.captureDeviceLocation();
                                    }
                                }, 10000);
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                            googleMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                            enableLoc();
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateLocationUI() {
        if (googleMap == null) {
            return;
        }

        try {
            if (mLocationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (Exception e)  {
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(Home.this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error","Location error " + connectionResult.getErrorCode());
                        }
                    }).build();

            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(Home.this, REQUEST_LOCATION);
                                getDeviceLocation();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                        break;
                    }
                }
            });
        }
    }

    public void settingsRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();


        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(Home.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }
}
