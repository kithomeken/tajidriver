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

import com.tajidriver.auth.SignIn;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.driver.DriverHome;
import com.tajidriver.R;
import com.tajidriver.driver.SignInActivity;
import com.tajidriver.global.Variables;
import com.tajidriver.service.MessagingServices;

import java.util.Objects;

import static com.tajidriver.configuration.TajiCabs.DRIVER_DETAILS;
import static com.tajidriver.configuration.TajiCabs.EMAIL;
import static com.tajidriver.configuration.TajiCabs.FIREBASE_TOKEN;
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
            AppDatabase appDatabase = AppDatabase.getDatabase(this);
            RWServices rwServices = new RWServices(appDatabase);
            rwServices.getUserDetails();

            sharedPreferences = getSharedPreferences(DRIVER_DETAILS, Context.MODE_PRIVATE);

            if (sharedPreferences.contains("EMAIL") && sharedPreferences.contains("NAMES")
                    && sharedPreferences.contains("PHONE")) {

                //TODO: Load Preferences
                Variables.ACCOUNT_EMAIL = sharedPreferences.getString("EMAIL", "");
                Variables.ACCOUNT_NAME = sharedPreferences.getString("NAMES", "");
                Variables.ACCOUNT_PHONE = sharedPreferences.getString("PHONE", "");
            }


            FirebaseInstanceId.getInstance().getInstanceId()
            .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.e(TAG, "XXXXXXXXXXXXXXXXXXXXXXXXXX getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                FIREBASE_TOKEN = Objects.requireNonNull(task.getResult()).getToken();

                // Go To Home
                Intent intent = new Intent(StartApp.this, DriverHome.class);
                startActivity(intent);
                finish();
                }
            });
        } else{
            // Go To Sign In
            Intent intent = new Intent(StartApp.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }
}