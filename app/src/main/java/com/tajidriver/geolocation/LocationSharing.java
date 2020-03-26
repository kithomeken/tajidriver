package com.tajidriver.geolocation;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;

import java.util.HashMap;
import java.util.Map;

import static com.tajidriver.configuration.TajiCabs.FIREBASE_TOKEN;

public class LocationSharing {
    private static final String TAG =LocationSharing.class.getName();
    private String locationLat, locationLng;
    private Context context;

    public LocationSharing(Context context, String locationLat, String locationLng) {
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.context = context;
    }

    public void captureDeviceLocation() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String captureLocationUrl = "https://taji.kennedykitho.me/taji/firebase/location/sharing";

        final String requestType = "704";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, captureLocationUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "===================================== COMPLETE: " + response);
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

                AppDatabase appDatabase = AppDatabase.getDatabase(context);
                RWServices rwServices = new RWServices(appDatabase);

                String tripId = rwServices.getActiveTrip();
                String firebaseToken = rwServices.getFirebaseToken();

                params.put("request_type", requestType);
                params.put("latitude", locationLat);
                params.put("longitude", locationLng);
                params.put("token", firebaseToken);

                if (tripId.equalsIgnoreCase("No Data Found")) {
                    params.put("activity_state", "E");
                } else {
                    params.put("activity_state", "A");
                }

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
