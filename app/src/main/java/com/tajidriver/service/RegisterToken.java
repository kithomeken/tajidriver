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

import static com.tajidriver.configuration.TajiCabs.EMAIL;
import static com.tajidriver.configuration.TajiCabs.NAMES;
import static com.tajidriver.configuration.TajiCabs.PHONE;

public class RegisterToken {
    private static final String TAG = RegisterToken.class.getName();
    public static final String BACKEND_URL_BASE = "https://taji.kennedykitho.me/taji/firebase/driver/register" ;

    private Context context;
    private IRequestListener listener;

    public RegisterToken (Context context, IRequestListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void firebaseTokenRegistration(final String firebaseToken) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String tajiUrl = BACKEND_URL_BASE;

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
                Map<String, String> params = new HashMap<String, String>();
                params.put("token", firebaseToken);
                params.put("group", "Driver");
                params.put("email", EMAIL);
                params.put("phone_number", PHONE);
                params.put("name", NAMES);
                params.put("vehicle_make", "0");
                params.put("reg_no", "0");

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
