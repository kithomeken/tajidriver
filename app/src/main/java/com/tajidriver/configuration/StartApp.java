package com.tajidriver.configuration;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
//import com.tajidriver.d.PassengerHome;
import com.tajidriver.DriverHome;
import com.tajidriver.SignInActivity;

import static com.tajidriver.configuration.TajiCabs.EMAIL;
import static com.tajidriver.configuration.TajiCabs.IDNUM;
import static com.tajidriver.configuration.TajiCabs.NAMES;
import static com.tajidriver.configuration.TajiCabs.PASSENGER_DETAILS;
import static com.tajidriver.configuration.TajiCabs.PHONE;

public class StartApp extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private String TAG = StartApp.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        if (firebaseUser != null) {
            // Get User Details
            sharedPreferences = getSharedPreferences(PASSENGER_DETAILS, Context.MODE_PRIVATE);

            if (sharedPreferences.contains("EMAIL") && sharedPreferences.contains("NAMES")
                    && sharedPreferences.contains("PHONE")) {

                //TODO: Load Preferences
                EMAIL = sharedPreferences.getString("EMAIL", "");
                NAMES = sharedPreferences.getString("NAMES", "");
                PHONE = sharedPreferences.getString("PHONE", "");
                IDNUM = sharedPreferences.getString("ID_NUM", "");
            }

            // Go To Home
            Intent intent = new Intent(StartApp.this, DriverHome.class);
            startActivity(intent);
            finish();
        } else{
            // Go To Sign In
            Intent intent = new Intent(StartApp.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}