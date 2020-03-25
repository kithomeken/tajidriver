package com.tajidriver.taxi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.tajidriver.R;
import com.tajidriver.app.FinishSetup;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.global.Constants;
import com.tajidriver.global.Variables;
import com.tajidriver.threads.AuthThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddTaxi extends AppCompatActivity {
    private static String TAG = AddTaxi.class.getName();

    private RWServices rwServices;
    private AuthThread authThread;
    private Thread mainThread;
    private Handler handler = new Handler();

    private EditText textBrand, textModel, textLicence, textYear, textColor, textCapacity;
    private TextView taxiError;
    private LinearLayout errorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_taxi);

        Variables.ACTIVITY_STATE = 0;
        AppDatabase appDatabase = AppDatabase.getDatabase(this);
        rwServices = new RWServices(appDatabase);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout vehicleBrand, vehicleModel, yearOfManuf,
                licensePlate, vehicleColor, seatingCapacity;

        vehicleBrand = findViewById(R.id.vehicleBrand);
        vehicleModel = findViewById(R.id.vehicleModel);
        yearOfManuf  = findViewById(R.id.yearOfManuf);
        licensePlate = findViewById(R.id.licensePlate);
        vehicleColor = findViewById(R.id.vehicleColor);
        seatingCapacity = findViewById(R.id.seatingCapacity);

        errorLayout = findViewById(R.id.errorLayout);
        errorLayout.setVisibility(View.GONE);

        textBrand = findViewById(R.id.textBrand);
        textModel = findViewById(R.id.textModel);
        textYear  = findViewById(R.id.textYear);
        textLicence = findViewById(R.id.textLicense);
        textColor = findViewById(R.id.textColor);
        textCapacity = findViewById(R.id.textCapacity);
        taxiError = findViewById(R.id.taxiError);

        final Button addTaxi = findViewById(R.id.addTaxi);
        addTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    errorLayout.setVisibility(View.GONE);
                    addTaxi();
                }
            }
        });
    }

    private void addTaxi() {
        Variables.ACTIVITY_STATE = 1;
        authThread = new AuthThread(AddTaxi.this, "Adding your new Taxi");

        if (!validateInputs()) {
            Variables.ACTIVITY_STATE = 0;
            String error = "Kindly fill all the inputs";

            errorLayout.setVisibility(View.VISIBLE);
            taxiError.setText(error);
            return;
        }

        runProgressDialog();
        taxiCheck(textLicence.getText().toString());
    }

    private void taxiCheck(String licensePlate) {
        String stringUrl = Constants.API_HEADER + Constants.TAXI_CHECK + "?license_plate=" + licensePlate;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, stringUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            if (status.equalsIgnoreCase("Failed")) {
                                String stringErr = "This taxi is registered under a different account";
                                errorLayout.setVisibility(View.VISIBLE);
                                taxiError.setText(stringErr);

                                Variables.ACTIVITY_STATE = 0;
                                authThread.hideProgressDialog();
                            } else {
                                registerTaxi();

                                String brand = textBrand.getText().toString();
                                String model = textModel.getText().toString();
                                String year = textYear.getText().toString();
                                String license = textLicence.getText().toString();
                                String color = textColor.getText().toString();
                                String capacity = textCapacity.getText().toString();

                                rwServices.addTaxiVehicle(brand, model, year, license, color, capacity);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "STACKTRACE: " + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Volley Error: " + error.getMessage());

                        String stringErr = "Error add taxi. Kindly check your internet connectivity";
                        errorLayout.setVisibility(View.VISIBLE);
                        taxiError.setText(stringErr);

                        Variables.ACTIVITY_STATE = 0;
                        authThread.hideProgressDialog();
                    }
                });

        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    private void registerTaxi() {
        RequestQueue queue = Volley.newRequestQueue(AddTaxi.this);
        String tajiUrl = Constants.API_HEADER + Constants.REGISTER_TAXI;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tajiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "ON SUCCESS " + response);

                        Variables.ACTIVITY_STATE = 0;
                        authThread.hideProgressDialog();

                        if (Variables.TAXI_SETUP == 1) {
                            // Redirect to Finish Setup
                            Intent intent =  new Intent(AddTaxi.this, FinishSetup.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Back to Taxi Management
                            Intent intent =  new Intent(AddTaxi.this, TaxiManagement.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }, new Response.ErrorListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ON FAILURE " + error.getMessage());
                String stringErr = "Error adding taxi. Kindly check your internet connectivity";
                errorLayout.setVisibility(View.VISIBLE);
                taxiError.setText(stringErr);

                Variables.ACTIVITY_STATE = 0;
                authThread.hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String brand = textBrand.getText().toString();
                String model = textModel.getText().toString();
                String year = textYear.getText().toString();
                String license = textLicence.getText().toString();
                String color = textColor.getText().toString();
                String capacity = textCapacity.getText().toString();

                String emailAdd = rwServices.getEmailAdd();

                params.put("brand", brand);
                params.put("model", model);
                params.put("year", year);
                params.put("license", license);
                params.put("color", color);
                params.put("capacity", capacity);
                params.put("email", emailAdd);

                return params;
            }
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private boolean validateInputs() {
        boolean valid = true;

        String stringBrand = textBrand.getText().toString();
        if (TextUtils.isEmpty(stringBrand)) {
            textBrand.setError("Required.");
            valid = false;
        } else {
            textBrand.setError(null);
        }

        String stringModel = textModel.getText().toString();
        if (TextUtils.isEmpty(stringModel)) {
            textModel.setError("Required.");
            valid = false;
        } else {
            textModel.setError(null);
        }

        String stringYear = textYear.getText().toString();
        if (TextUtils.isEmpty(stringYear)) {
            textYear.setError("Required");
            valid = false;
        } else {
            textYear.setError(null);
        }

        String license = textLicence.getText().toString();
        if (TextUtils.isEmpty(license)) {
            textLicence.setError("Required");
            valid = false;
        } else {
            textLicence.setError(null);
        }

        String color = textColor.getText().toString();
        if (TextUtils.isEmpty(color)) {
            textColor.setError("Required");
            valid = false;
        } else {
            textColor.setError(null);
        }

        String capacity = textCapacity.getText().toString();
        if (TextUtils.isEmpty(capacity)) {
            textCapacity.setError("Required");
            valid = false;
        } else {
            textCapacity.setError(null);
        }

        return valid;
    }

    private void runProgressDialog() {
        mainThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                // Update the progress bar
                handler.post(new Runnable() {
                    public void run() {
                        if (!mainThread.interrupted()) {
                            authThread.run();
                            Log.e(TAG, "RUNNING AUTH THREAD");
                        }
                    }
                });
            }
        };

        mainThread.start();
    }

    protected void onStop() {
        if (Variables.TAXI_SETUP == 1) {
            setResult(1);
        }
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        if (Variables.TAXI_SETUP == 1) {
            setResult(1);
        }
        super.onDestroy();
    }
}
