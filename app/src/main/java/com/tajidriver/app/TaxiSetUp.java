package com.tajidriver.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.tajidriver.R;
import com.tajidriver.global.Variables;
import com.tajidriver.home.Home;
import com.tajidriver.taxi.AddTaxi;

public class TaxiSetUp extends AppCompatActivity {
    private static int SETUP_COMPLETE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_set_up);
        Variables.ACTIVITY_STATE = 0;

        RelativeLayout skipTextSetup = findViewById(R.id.skipSetup);
        skipTextSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;

                    Intent intent = new Intent(TaxiSetUp.this, Home.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        Button addTaxi = findViewById(R.id.addVehicle);
        addTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;
                    Variables.TAXI_SETUP = 1;

                    Intent intent = new Intent(TaxiSetUp.this, AddTaxi.class);
                    startActivityForResult(intent, SETUP_COMPLETE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            finish();
        }
    }
}
