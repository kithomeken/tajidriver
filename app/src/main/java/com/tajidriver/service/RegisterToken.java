package com.tajidriver.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.tajidriver.global.Constants;

import java.util.HashMap;
import java.util.Map;

import static com.tajidriver.global.Variables.ACCOUNT_EMAIL;
import static com.tajidriver.global.Variables.ACCOUNT_NAME;
import static com.tajidriver.global.Variables.ACCOUNT_PHONE;
import static com.tajidriver.global.Variables.VEHICLE_MAKE;
import static com.tajidriver.global.Variables.VEHICLE_REGNO;

class RegisterToken {
    private static final String TAG = RegisterToken.class.getName();

    private Context context;
    private IRequestListener listener;

    RegisterToken(Context context, IRequestListener listener) {
        this.context = context;
        this.listener = listener;
    }

    void firebaseTokenRegistration(final String firebaseToken) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String tajiUrl = Constants.API_HEADER + Constants.REGISTER_FIREBASE_TOKEN;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tajiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onComplete();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", firebaseToken);
                params.put("group", "Driver");
                params.put("email", ACCOUNT_EMAIL);
                params.put("phone_number", ACCOUNT_PHONE);
                params.put("name", ACCOUNT_NAME);
                params.put("vehicle_make", VEHICLE_MAKE);
                params.put("reg_no", VEHICLE_REGNO);

                Log.d(TAG, "=====================================" + params);

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

    void updateFirebaseToken(final String firebaseToken) {
        // Update Firebase Token on Sign In
        RequestQueue queue = Volley.newRequestQueue(context);
        String tajiUrl = Constants.API_HEADER + Constants.UPDATE_FIREBASE_TOKEN;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tajiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        listener.onComplete();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                listener.onError(error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", firebaseToken);
                params.put("email", ACCOUNT_EMAIL);

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
}
