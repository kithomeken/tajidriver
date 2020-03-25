package com.tajidriver.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.tajidriver.R;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.global.Variables;
import com.tajidriver.taxi.TaxiManagement;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Variables.ACTIVITY_STATE = 0;
        AppDatabase appDatabase = AppDatabase.getDatabase(this);
        RWServices rwServices = new RWServices(appDatabase);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CardView taxiManagement, privacyPolicy, profileCard;
        taxiManagement = findViewById(R.id.taxiManagement);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        profileCard = findViewById(R.id.profileCard);

        TextView accountName, accountEmail;
        accountName = findViewById(R.id.accountName);
        accountEmail = findViewById(R.id.accountEmail);

        String firstName = rwServices.getFirstName();
        String lastName = rwServices.getLastName();
        String emailAdd = rwServices.getEmailAdd();
        String stringAccountName = firstName + " " + lastName;

        accountEmail.setText(emailAdd);
        accountName.setText(stringAccountName);

        taxiManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;

                    Intent intent = new Intent(Settings.this, TaxiManagement.class);
                    startActivity(intent);
                }
            }
        });

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;

                    Intent intent = new Intent(Settings.this, PrivacyPolicy.class);
                    startActivity(intent);
                }
            }
        });

        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;

                    Intent intent = new Intent(Settings.this, UserProfile.class);
                    startActivity(intent);
                }
            }
        });
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
