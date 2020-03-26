package com.tajidriver.auth;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.tajidriver.R;
import com.tajidriver.app.OnBoardingUI;
import com.tajidriver.configuration.Firebase;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.database.UserDetails;
import com.tajidriver.database.UserDetailsDao;
import com.tajidriver.database.VehicleDetails;
import com.tajidriver.database.VehicleDetailsDao;
import com.tajidriver.driver.DriverHome;
import com.tajidriver.global.Constants;
import com.tajidriver.global.Variables;
import com.tajidriver.service.IRequestListener;
import com.tajidriver.service.MessagingServices;
import com.tajidriver.threads.AuthThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.tajidriver.global.Variables.ACCOUNT_EMAIL;
import static com.tajidriver.global.Variables.ACCOUNT_FNAME;
import static com.tajidriver.global.Variables.ACCOUNT_LNAME;
import static com.tajidriver.global.Variables.ACCOUNT_PHONE;
import static com.tajidriver.global.Variables.VEHICLE_MAKE;
import static com.tajidriver.global.Variables.VEHICLE_REGNO;

public class SignIn extends Firebase {
    private static final String TAG = SignIn.class.getName();

    private EditText accountEmail, accountPassword;
    private RelativeLayout authFailed;
    private TextView accountError;
    private Button accountSignIn;

    private FirebaseAuth firebaseAuth;
    private Context context;
    private UserDetailsDao userDetailsDao;
    private VehicleDetailsDao vehicleDetailsDao;

    private Handler handler = new Handler();
    private AuthThread authThread;
    private Thread mainThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_sign_in);

        Variables.ACTIVITY_STATE = 0;
        authThread = new AuthThread(SignIn.this, "Authenticating");

        firebaseAuth = FirebaseAuth.getInstance();
        AppDatabase appDatabase = AppDatabase.getDatabase(this);
        context = getApplicationContext();
        userDetailsDao = appDatabase.userDetailsDao();
        vehicleDetailsDao = appDatabase.vehicleDetailsDao();

        // Relative Layout Declarations
        relativeLayoutDeclaration();
        authFailed = findViewById(R.id.authFailed);
        authFailed.setVisibility(View.GONE);

        accountEmail = findViewById(R.id.accountEmail);
        accountError = findViewById(R.id.accountError);
        accountSignIn = findViewById(R.id.accountSignIn);
        accountPassword = findViewById(R.id.accountPassword);

        accountSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
                driverSignIn();
            }
        });
    }

    private void relativeLayoutDeclaration() {
        RelativeLayout signUpLink = findViewById(R.id.signUpLink);
        RelativeLayout forgotLink = findViewById(R.id.forgotPasswordLink);

        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {
                    Intent intent = new Intent(getApplicationContext(), SignUp.class);
                    startActivity(intent);
                }
            }
        });

        forgotLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Variables.ACTIVITY_STATE == 0) {

                }
            }
        });
    }

    private boolean validateInputs() {
        boolean valid = true;

        String email = accountEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            accountEmail.setError("Required.");
            valid = false;
        } else {
            accountEmail.setError(null);
        }

        String password = accountPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            accountPassword.setError("Required.");
            valid = false;
        } else {
            accountPassword.setError(null);
        }

        return valid;
    }

    private String accountEmail() {
        return accountEmail.getText().toString().trim();
    }

    private String accountPassword() {
        return accountPassword.getText().toString().trim();
    }

    public void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void driverSignIn() {
        if (!validateInputs()) {
            return;
        }

        Variables.ACTIVITY_STATE = 1;
        accountSignIn.setVisibility(View.GONE);
        authFailed.setVisibility(View.GONE);
        progressThread();

        final String email = accountEmail();
        String password = accountPassword();

        firebaseAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                Log.d(TAG, "Firebase Signing In: success");

                FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, " getInstanceId failed", task.getException());
                        Variables.ACTIVITY_STATE = 0;
                        accountSignIn.setVisibility(View.VISIBLE);

                        return;
                    }

                    String firebaseToken = Objects.requireNonNull(task.getResult()).getToken();

                    // Get Account Details
                    accountSetUp(firebaseToken);

                    // Get Taxi Vehicle Details
                    getTaxiDetails(accountEmail());

                    // Update Firebase Token
                    updateFirebaseToken(firebaseToken, accountEmail());
                    }
                });
            } else {
                authFailed.setVisibility(View.VISIBLE);
                accountError.setText(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                Log.e(TAG, "Authentication Failed: " + task.getException().getLocalizedMessage());
            }

            if (!task.isSuccessful()) {
                authFailed.setVisibility(View.VISIBLE);
                accountError.setText(Objects.requireNonNull(task.getException()).getLocalizedMessage());
                Log.e(TAG, "Authentication Failed: " + task.getException().getMessage());
            }

            Variables.ACTIVITY_STATE = 0;
            accountSignIn.setVisibility(View.VISIBLE);
            }
        });

    }

    private void accountSetUp(final String firebaseToken) {
        String stringUrl = Constants.API_HEADER + Constants.FETCH_ACCOUNT_DETAILS + "?email=" + accountEmail;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, stringUrl,
        new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e(TAG, "JSON Object: " + jsonObject);

                    Variables.ACCOUNT_EMAIL = jsonObject.getString("email");
                    Variables.ACCOUNT_FNAME = jsonObject.getString("first_name");
                    Variables.ACCOUNT_LNAME = jsonObject.getString("last_name");
                    Variables.ACCOUNT_PHONE = jsonObject.getString("phone_number");

                    Variables.VEHICLE_MAKE = jsonObject.getString("vehicle_make");
                    Variables.VEHICLE_REGNO = jsonObject.getString("reg_no");

                    // Add Entries to Application Database
                    createUser(ACCOUNT_EMAIL, ACCOUNT_FNAME, ACCOUNT_LNAME, ACCOUNT_PHONE,
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
                FirebaseAuth.getInstance().signOut();
                authFailed.setVisibility(View.VISIBLE);

                String failed = "Error signing you into your Taji Driver account. Kindly check your internet connectivity";
                accountError.setText(failed);

                Variables.ACTIVITY_STATE = 0;
                // End Main Thread
                authThread.hideProgressDialog();
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    private void getTaxiDetails(String emailAdd) {
        String stringUrl = Constants.API_HEADER + Constants.TAXI_DETAILS + "?email=" + emailAdd;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, stringUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            Log.e(TAG, "JSON Object: " + jsonObject);

                            String vehicleBrand = jsonObject.getString("make");
                            String vehicleModel = jsonObject.getString("model");
                            String vehicleYear  = jsonObject.getString("year");
                            String vehicleRegNo = jsonObject.getString("reg_no");
                            String vehicleColor = jsonObject.getString("color");
                            String vehicleCap   = jsonObject.getString("seating_capacity");

                            // Add Entries to Application Database
                            addTaxiVehicle(vehicleBrand, vehicleModel, vehicleYear, vehicleRegNo,
                                    vehicleColor, vehicleCap);
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

                        FirebaseAuth.getInstance().signOut();
                        authFailed.setVisibility(View.VISIBLE);

                        String failed = "Error signing you into your Taji Driver account. Kindly check your internet connectivity";
                        accountError.setText(failed);

                        Variables.ACTIVITY_STATE = 0;
                        // End Main Thread
                        authThread.hideProgressDialog();
                    }
                });

        Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
    }

    private void updateFirebaseToken(final String firebaseToken, final String emailAdd) {
        // Update Firebase Token on Sign In
        RequestQueue queue = Volley.newRequestQueue(context);
        String tajiUrl = Constants.API_HEADER + Constants.UPDATE_FIREBASE_TOKEN;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, tajiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "RESPONSE: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "VOLLEY ERROR: " + error.getMessage());

                FirebaseAuth.getInstance().signOut();
                authFailed.setVisibility(View.VISIBLE);

                String failed = "Error signing you into your Taji Driver account. Kindly check your internet connectivity";
                accountError.setText(failed);

                Variables.ACTIVITY_STATE = 0;
                // End Main Thread
                authThread.hideProgressDialog();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("token", firebaseToken);
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

    private void createUser(@NonNull String email, @NonNull String first_name,
        @NonNull String last_name, @NonNull String phone_number, @NonNull String firebaseToken,
        String vehicleMake, String vehicleRegNo) {

        final String userId = UUID.randomUUID().toString();
        UserDetails userDetails = new UserDetails(userId, email, first_name, last_name,
                phone_number, firebaseToken, vehicleMake, vehicleRegNo);

        userDetailsDao.createNewUser(userDetails);

        finishSignIn();
    }

    public void addTaxiVehicle(@NonNull String vehicleMake, @NonNull String vehicleModel,
        @NonNull String yearOfManuf, @NonNull String vehicleRegNo,
        @NonNull String vehicleColor, @NonNull String seatingCapacity) {

        final String vehicleId = UUID.randomUUID().toString();
        VehicleDetails vehicleDetails = new VehicleDetails(vehicleId, vehicleRegNo, vehicleMake, vehicleModel,
                yearOfManuf, vehicleColor, seatingCapacity);

        vehicleDetailsDao.addVehicle(vehicleDetails);
    }

    private void finishSignIn(){
        Intent intent = new Intent(SignIn.this, OnBoardingUI.class);
        startActivity(intent);
        finish();
    }

    public void progressThread() {
        mainThread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();

                // Update the progress bar
                handler.post(new Runnable() {
                    public void run() {
                        if (!mainThread.interrupted()) {
                            authThread.run();
                            Log.e(TAG, "RUNNING THREAD");
                        }
                    }
                });
            }
        };

        mainThread.start();
    }

}











