package com.tajidriver.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.tajidriver.R;
import com.tajidriver.home.Home;

public class FinishSetup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_setup);

        Button finishSetup = findViewById(R.id.finishSetUp);
        finishSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FinishSetup.this, Home.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
