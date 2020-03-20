package com.tajidriver.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
import com.tajidriver.configuration.TajiCabs;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.database.UserDetails;
import com.tajidriver.database.UserDetailsDao;
import com.tajidriver.global.Constants;
import com.tajidriver.global.Variables;
import com.tajidriver.home.Home;
 import com.tajidriver.threads.AuthThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.tajidriver.global.Variables.ACCOUNT_EMAIL;
import static com.tajidriver.global.Variables.ACCOUNT_NAME;
import static com.tajidriver.global.Variables.ACCOUNT_PHONE;
import static com.tajidriver.global.Variables.VEHICLE_MAKE;
import static com.tajidriver.global.Variables.VEHICLE_REGNO;

public class SignUp extends AppCompatActivity {
    private static String TAG = SignUp.class.getName();

    private AppDatabase appDatabase;
    private UserDetailsDao userDetailsDao;
    private FirebaseAuth firebaseAuth;

    private Handler handler = new Handler();
    private AuthThread authThread;
    private Thread mainThread;

    private RelativeLayout authFailed;
    private TextView accountError;
    private EditText accountFName, accountLName, accountPhone, accountEmail, accountPassword,
            confirmPassword, vehicleMake, vehicleRegNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auth_sign_up);

        authThread = new AuthThread(SignUp.this, "Creating New User");
        firebaseAuth = FirebaseAuth.getInstance();
        appDatabase = AppDatabase.getDatabase(this);
        userDetailsDao = appDatabase.userDetailsDao();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        accountFName = findViewById(R.id.accountFName);
        accountLName = findViewById(R.id.accountLName);
        accountPhone = findViewById(R.id.accountPhone);
        accountEmail = findViewById(R.id.accountEmail);

        vehicleMake = findViewById(R.id.vehicleMake);
        vehicleRegNo = findViewById(R.id.vehicleRegNo);

        accountPassword = findViewById(R.id.accountPassword);
        confirmPassword = findViewById(R.id.confirmPassword);

        authFailed = findViewById(R.id.authFailed);
        authFailed.setVisibility(View.GONE);
        accountError = findViewById(R.id.accountError);

        Button accountSignUp = findViewById(R.id.accountSignUp);
        accountSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);

                if (Variables.ACTIVITY_STATE == 0) {
                    createAccount();
                }
            }
        });
    }

    private void createAccount() {
        if (!validateForm()) {
            return;
        }

        if (!comparePasswords()) {
            return;
        }

        Log.i(TAG, "Creating Firebase Account :");
        Variables.ACTIVITY_STATE = 1;
        threadTrial();

        firebaseAuth.createUserWithEmailAndPassword(email(), password())
        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Firebase Account Creation: Success");

                    // Firebase Messaging Token Registration
                    FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.e(TAG, "Getting Firebase InstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String firebaseToken = Objects.requireNonNull(task.getResult()).getToken();
                                Log.i(TAG, "Firebase InstanceId Token Acquired");
                                TajiCabs.FIREBASE_TOKEN = firebaseToken;

                                String email = email();
                                String first_name = firstName();
                                String last_name = lastName();
                                String phone_number = phoneNumber();

                                // Add User Details to Application Database
                                addUserDetails(email, first_name, last_name, phone_number, firebaseToken);

                                // Send Account Token to Server
                                registerAccountToken(firebaseToken);

                                // End Main Thread
                                Variables.ACTIVITY_STATE = 1;
                                authThread.hideProgressDialog();

                                Intent intent = new Intent(SignUp.this, Home.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                } else {
                    Log.i(TAG, "Firebase Account Creation: Failed", task.getException());
                    authFailed.setVisibility(View.VISIBLE);
                    accountError.setText(Objects.requireNonNull(task.getException()).getLocalizedMessage());

                    Variables.ACTIVITY_STATE = 0;
                    // End Main Thread
                    Log.e(TAG, "Main Thread Ended");
                    authThread.hideProgressDialog();
                }

                if (!task.isSuccessful()) {
                    authFailed.setVisibility(View.VISIBLE);
                    accountError.setText(Objects.requireNonNull(task.getException()).getLocalizedMessage());

                    Variables.ACTIVITY_STATE = 0;
                    // End Main Thread
                    Log.e(TAG, "Main Thread Ended");
                    authThread.hideProgressDialog();
                }
            }
        });
    }

    private void addUserDetails(@NonNull String email, @NonNull String first_name,
        @NonNull String last_name, @NonNull String phone_number, @NonNull String firebaseToken) {

        final String userId = UUID.randomUUID().toString();
        UserDetails userDetails = new UserDetails(userId, email, first_name, last_name,
                phone_number, firebaseToken, vehicleMake(), vehicleRegNo());

        userDetailsDao.createNewUser(userDetails);

        // Fetch User Details
        RWServices rwServices = new RWServices(appDatabase);
        rwServices.getUserDetails();
    }

    private void registerAccountToken(final String firebaseToken) {
        RequestQueue queue = Volley.newRequestQueue(SignUp.this);
        String tajiUrl = Constants.API_HEADER + Constants.REGISTER_FIREBASE_TOKEN;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tajiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "ON SUCCESS " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "ON FAILURE " + error.getMessage());
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

    private String firstName() {
        return accountFName.getText().toString();
    }

    private String lastName() {
        return accountLName.getText().toString();
    }

    private String phoneNumber() {
        return accountPhone.getText().toString();
    }

    private String email() {
        return accountEmail.getText().toString();
    }

    private String password() {
        return accountPassword.getText().toString();
    }

    private String vehicleMake() {
        return vehicleMake.getText().toString();
    }

    private String vehicleRegNo() {
        return vehicleRegNo.getText().toString();
    }

    private boolean validateForm() {
        boolean valid = true;

        String firstName = accountFName.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            accountFName.setError("Required.");
            valid = false;
        } else {
            accountFName.setError(null);
        }

        String lastName = accountLName.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            accountLName.setError("Required.");
            valid = false;
        } else {
            accountLName.setError(null);
        }

        String phoneNumber = accountPhone.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            accountPhone.setError("Required.");
            valid = false;
        } else {
            accountPhone.setError(null);
        }

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

        String confirmPwd = confirmPassword.getText().toString();
        if (TextUtils.isEmpty(confirmPwd)) {
            confirmPassword.setError("Required.");
            valid = false;
        } else {
            confirmPassword.setError(null);
        }

        return valid;
    }

    private boolean comparePasswords() {
        boolean valid = true;

        String password = accountPassword.getText().toString();
        String confirmd = confirmPassword.getText().toString();

        if (!password.equals(confirmd)) {
            accountPassword.setError("Passwords Do Not Match");
            valid = false;
        } else {
            accountPassword.setError(null);
        }

        return valid;
    }

    private void hideKeyboard(View view) {
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void threadTrial() {
//        new Thread(new Runnable() {
//            public void run() {
//                // Update the progress bar
//                handler.post(new Runnable() {
//                    public void run() {
//                        if (!Thread.interrupted()) {
//                            authThread.run();
//                            new createAccountTask().execute();
//                        }
//                    }
//                });
//            }
//        }).start();


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
