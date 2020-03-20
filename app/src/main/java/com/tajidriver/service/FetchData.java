package com.tajidriver.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.global.Constants;
import com.tajidriver.global.Variables;

import org.json.JSONException;
import org.json.JSONObject;

import static com.tajidriver.global.Variables.ACCOUNT_EMAIL;
import static com.tajidriver.global.Variables.ACCOUNT_FNAME;
import static com.tajidriver.global.Variables.ACCOUNT_LNAME;
import static com.tajidriver.global.Variables.ACCOUNT_PHONE;
import static com.tajidriver.global.Variables.VEHICLE_MAKE;
import static com.tajidriver.global.Variables.VEHICLE_REGNO;

public class FetchData {
    private static final String TAG = FetchData.class.getName();
    private Context context;
    private String accountEmail, firebaseToken;
    private JSONObject jsonObject;
    private AppDatabase appDatabase;

    public FetchData(Context context, AppDatabase appDatabase, String accountEmail, String firebaseToken) {
        this.context = context;
        this.appDatabase = appDatabase;
        this.accountEmail = accountEmail;
        this.firebaseToken = firebaseToken;
    }

    public void fetchAccountDetails() {
        String stringUrl = Constants.API_HEADER + Constants.FETCH_ACCOUNT_DETAILS + "?email=" + accountEmail;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, stringUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            jsonObject = new JSONObject(response);
                            Log.e(TAG, "JSON Object: " + jsonObject);

                            Variables.ACCOUNT_EMAIL = jsonObject.getString("email");
                            Variables.ACCOUNT_FNAME = jsonObject.getString("first_name");
                            Variables.ACCOUNT_LNAME = jsonObject.getString("last_name");
                            Variables.ACCOUNT_PHONE = jsonObject.getString("phone_number");

                            Variables.VEHICLE_MAKE = jsonObject.getString("vehicle_make");
                            Variables.VEHICLE_REGNO = jsonObject.getString("reg_no");

                            // Add Entries to Application Database
                            RWServices rwServices = new RWServices(appDatabase);
                            rwServices.createUser(ACCOUNT_EMAIL, ACCOUNT_FNAME, ACCOUNT_LNAME, ACCOUNT_PHONE,
                                    firebaseToken, VEHICLE_MAKE, VEHICLE_REGNO);
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
                    }
                });

        Volley.newRequestQueue(context).add(stringRequest);
    }
}
