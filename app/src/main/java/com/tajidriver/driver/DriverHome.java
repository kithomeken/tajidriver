package com.tajidriver.driver;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.tajidriver.R;
import com.tajidriver.directions.TajiDirections;
import com.tajidriver.service.RequestServices;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import static com.tajidriver.configuration.TajiCabs.ACTIVITY_STATE;
import static com.tajidriver.configuration.TajiCabs.DEFAULT_ZOOM;
import static com.tajidriver.configuration.TajiCabs.DEST_LTNG;
import static com.tajidriver.configuration.TajiCabs.DEST_NAME;
import static com.tajidriver.configuration.TajiCabs.DISTANCE;
import static com.tajidriver.configuration.TajiCabs.GOOGLE_API;
import static com.tajidriver.configuration.TajiCabs.ORIG_LTNG;
import static com.tajidriver.configuration.TajiCabs.ORIG_NAME;
import static com.tajidriver.configuration.TajiCabs.REQUEST_LOCATION;
import static com.tajidriver.configuration.TajiCabs.RQ_COST;
import static com.tajidriver.configuration.TajiCabs.RQ_DEST;
import static com.tajidriver.configuration.TajiCabs.RQ_NAME;
import static com.tajidriver.configuration.TajiCabs.RQ_ORIG;
import static com.tajidriver.configuration.TajiCabs.RQ_PHONE;

@SuppressLint("MissingPermission")
public class DriverHome extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback {

    // TAG
    private static final String TAG = DriverHome.class.getName();

    // Dependency classes
    TajiDirections tajiDirections = new TajiDirections();


    protected static final int overview = 0;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-1.2833 , 36.8167);
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    private Location mLastKnownLocation;
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private GoogleApiClient googleApiClient;

    private LinearLayout noRequestBlock, requestBlock, tripBlock;
    private FloatingActionButton geoLocation;
    private TextView fromDisp, toDisp, distanceDisp, costDisp, requestName, requestPhone, tripCost;
    private Button acceptRequest, declineRequest;

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkFirebaseSession(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), GOOGLE_API);
        }

        noRequestBlock = findViewById(R.id.noRequestBlock);
        requestBlock = findViewById(R.id.requestBlock);
        tripBlock = findViewById(R.id.tripBlock);

        fromDisp = findViewById(R.id.fromDisp);
        toDisp = findViewById(R.id.toDisp);
        distanceDisp = findViewById(R.id.distanceCovered);
        costDisp = findViewById(R.id.costDisp);
        tripCost = findViewById(R.id.tripCost);
        requestName = findViewById(R.id.requestName);
        requestPhone = findViewById(R.id.requestPhone);

        acceptRequest = findViewById(R.id.acceptRequest);
        declineRequest = findViewById(R.id.declineRequest);

        acceptRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                acceptRideNotification();
                drawDirections();
            }
        });

        if (RQ_NAME != null) {
            noRequestBlock.setVisibility(View.GONE);
            requestBlock.setVisibility(View.VISIBLE);
            tripBlock.setVisibility(View.GONE);

            // Load Ride Request Information (RRInformation)
            RRInformation();
        } else {
            noRequestBlock.setVisibility(View.VISIBLE);
            requestBlock.setVisibility(View.GONE);
            tripBlock.setVisibility(View.GONE);
        }

        mLocationRequest = new LocationRequest().setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set display name, photo url and email address of signed in user
        View navHeaderView = navigationView.getHeaderView(0);
        TextView accountName = (TextView) navHeaderView.findViewById(R.id.accountName);
        TextView accountEmail = (TextView) navHeaderView.findViewById(R.id.accountEmail);

        assert user != null;
        accountName.setText(user.getDisplayName());
        accountEmail.setText(user.getEmail());

        geoLocation = findViewById(R.id.geo_location);
        geoLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });
    }

    private void drawDirections() {
        if (DEST_LTNG != null && ORIG_LTNG != null) {
            mMap.clear();
            googleMapsUISetting(mMap);

            DirectionsResult directionsResult = tajiDirections
                    .getDirectionsDetails(ORIG_LTNG, DEST_LTNG, TravelMode.DRIVING);

            if (directionsResult != null) {
                tajiDirections.addPolyline(directionsResult, mMap);
                tajiDirections.positionCamera(directionsResult.routes[overview], mMap);
                tajiDirections.addMarkersToMap(directionsResult, mMap);
            }

            DISTANCE = tajiDirections.distanceInMeters(directionsResult);

            fromDisp.setText("From: " + ORIG_NAME);
            toDisp.setText("To: " + DEST_NAME);
            distanceDisp.setText(DISTANCE);

            noRequestBlock.setVisibility(View.GONE);
            requestBlock.setVisibility(View.GONE);
            tripBlock.setVisibility(View.VISIBLE);

            changeMarginBottom();
        }
    }

    private void changeMarginBottom() {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)
                geoLocation.getLayoutParams();
        layoutParams.setMargins(0, 0, 0,300 );
        geoLocation.setLayoutParams(layoutParams);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.google_maps_theme)));

        if (!success) {
            Log.e(TAG, "==========================Style parsing failed.");
        }

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get Current Location of Device
        getDeviceLocation();
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    public void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastKnownLocation = task.getResult();

                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                            Log.e(TAG, "====================================" + mLastKnownLocation);

                            mLocationRequest = new LocationRequest();
                            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());

                            mMap.animateCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);

                            enableLoc();

                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_home, menu);
        return true;
    }

    private void checkFirebaseSession(FirebaseUser user) {
        if (user != null) {
            // Do nothing

        } else{
            //Return to SignInActivity
            Intent intent = new Intent(DriverHome.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }

        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (Exception e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void settingsRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
                            status.startResolutionForResult(DriverHome.this, REQUEST_CHECK_SETTINGS);
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

    private void RRInformation() {
        // Ride Request Information
        requestName.setText("You have a ride request from " + RQ_NAME);
        requestPhone.setText("Phone  No: " + RQ_PHONE);
        costDisp.setText(RQ_COST);
        tripCost.setText(RQ_COST);

        String[] orig =  RQ_ORIG.split(",");
        double oLat = Double.parseDouble(orig[0]);
        double oLng = Double.parseDouble(orig[1]);

        String[] dest =  RQ_DEST.split(",");
        double dLat = Double.parseDouble(dest[0]);
        double dLng = Double.parseDouble(dest[1]);

        ORIG_LTNG = new LatLng(oLat, oLng);
        DEST_LTNG = new LatLng(dLat, dLng);
    }

    private void acceptRideNotification() {
        ACTIVITY_STATE = 703;

        RequestServices requestServices = new RequestServices(getApplicationContext());
        requestServices.acceptRide();
    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getDeviceLocation();
                        break;

                    case Activity.RESULT_CANCELED:
                        settingsRequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    private boolean hasGPSDevice(Context context) {
        final LocationManager mgr = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (mgr == null)
            return false;

        final List<String> providers = mgr.getAllProviders();

        if (providers == null)
            return false;

        return providers.contains(LocationManager.GPS_PROVIDER);
    }

    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(DriverHome.this)
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
                                status.startResolutionForResult(DriverHome.this, REQUEST_LOCATION);
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

}



