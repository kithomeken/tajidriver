package com.tajidriver.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tajidriver.R;
import com.tajidriver.global.Variables;
import com.tajidriver.home.Home;

public class OnBoardingUI extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_boarding_ui);
        Variables.ACTIVITY_STATE = 0;

        final Button finishSetup = findViewById(R.id.finishSetUp);
        finishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Variables.ACTIVITY_STATE = 1;

                    Intent intent = new Intent(OnBoardingUI.this, Home.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
