package com.tajidriver.home;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.TextView;

import com.tajidriver.R;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;

public class Profile extends AppCompatActivity {
    private RWServices rwServices;
    private TextView accountName, accountEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        AppDatabase appDatabase = AppDatabase.getDatabase(this);
        rwServices = new RWServices(appDatabase);

        String firstName = rwServices.getFirstName();
        String lastName = rwServices.getLastName();
        String phoneNumber = rwServices.getPhoneNumber();
        String emailAdd = rwServices.getEmailAdd();
        String stringName = firstName + " " + lastName;

        accountName = findViewById(R.id.accountName);
        accountEmail = findViewById(R.id.accountEmail);

        accountName.setText(stringName);
        accountEmail.setText(emailAdd);

        TextView accountFirstName, accountLastName, accountPhone, emailAccount;

        accountFirstName = findViewById(R.id.accountFName);
        accountLastName = findViewById(R.id.accountLastName);
        accountPhone = findViewById(R.id.accountPhone);
        emailAccount = findViewById(R.id.emailAccount);

        accountFirstName.setText(firstName);
        accountLastName.setText(lastName);
        accountPhone.setText(phoneNumber);
        emailAccount.setText(emailAdd);
    }
}
