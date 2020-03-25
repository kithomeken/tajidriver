package com.tajidriver.taxi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tajidriver.R;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.global.Variables;

public class TaxiManagement extends AppCompatActivity  {
    private static String TAG = TaxiManagement.class.getName();

    private RWServices rwServices;

    private RelativeLayout noVehicleFound;
    private LinearLayout vehicleList;
    private TextView vehicleMake, vehicleRegNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_management);
        Variables.ACTIVITY_STATE = 0;

        noVehicleFound = findViewById(R.id.noVehicleFound);
        vehicleList = findViewById(R.id.vehicleList);

        vehicleMake = findViewById(R.id.vehicleMake);
        vehicleRegNo = findViewById(R.id.vehicleRegNo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppDatabase appDatabase = AppDatabase.getDatabase(this);
        rwServices = new RWServices(appDatabase);

        checkForVehicles();

        CardView vehicleCard = findViewById(R.id.vehicleCard);
        vehicleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;
                }
            }
        });

        Button addVehicle = findViewById(R.id.addVehicle);
        addVehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;
                }
            }
        });
    }

    private void checkForVehicles() {
        String stringRegNo = rwServices.getVehicleRegNo();

        if (stringRegNo.equalsIgnoreCase("No Data Found")) {
            noVehicleFound.setVisibility(View.VISIBLE);
            vehicleList.setVisibility(View.GONE);
        } else {
            vehicleList.setVisibility(View.VISIBLE);
            noVehicleFound.setVisibility(View.GONE);

            String stringMake = rwServices.getVehicleMake();
            String stringModel = rwServices.getVehicleModel();
            String vehicleName = stringMake + " " + stringModel;

            vehicleRegNo.setText(stringRegNo);
            vehicleMake.setText(vehicleName);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Variables.ACTIVITY_STATE = 0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Variables.ACTIVITY_STATE = 0;
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
