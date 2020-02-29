package com.tajidriver.configuration;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.tajidriver.driver.DriverHome;
import com.tajidriver.R;
import com.tajidriver.driver.SignInActivity;
import com.tajidriver.service.MessagingServices;

import static com.tajidriver.configuration.TajiCabs.DRIVER_DETAILS;
import static com.tajidriver.configuration.TajiCabs.EMAIL;
import static com.tajidriver.configuration.TajiCabs.IDNUM;
import static com.tajidriver.configuration.TajiCabs.NAMES;
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
            sharedPreferences = getSharedPreferences(DRIVER_DETAILS, Context.MODE_PRIVATE);

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