package com.tajidriver.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import static com.tajidriver.configuration.TajiCabs.NAMES;
import static com.tajidriver.configuration.TajiCabs.PHONE;
import static com.tajidriver.configuration.TajiCabs.REG_NO;
import static com.tajidriver.configuration.TajiCabs.RQ_PHONE;
import static com.tajidriver.configuration.TajiCabs.VEHMAKE;

public class RequestServices {
    private static final String TAG = RequestServices.class.getName();
    private Context context;

    public RequestServices (Context context) {
        this.context = context;
    }

    public void acceptRide() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String tajiUrl = "https://taji.kennedykitho.me/taji/firebase/request-ride/accepted";

        final String rType = "703";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tajiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "===================================== COMPLETE");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "===================================== ERROR " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", NAMES);
                params.put("request_type", rType);
                params.put("driver_phone", PHONE);
                params.put("reg_no", REG_NO);
                params.put("vehicle_make", VEHMAKE);
                params.put("passenger_phone", RQ_PHONE);

                Log.d(TAG, "=====================================" + params);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
