package com.tajidriver.driver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.tajidriver.R;
import com.tajidriver.configuration.Firebase;
import com.tajidriver.service.MessagingServices;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import static com.tajidriver.configuration.TajiCabs.DRIVER_DETAILS;
import static com.tajidriver.configuration.TajiCabs.EMAIL;
import static com.tajidriver.configuration.TajiCabs.NAMES;
import static com.tajidriver.configuration.TajiCabs.PHONE;

public class SignUpActivity extends Firebase implements View.OnClickListener {
    private static final String TAG = "Sign Up Activity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView regFailed;
    private static String STATUS;

    private EditText firstText;
    private EditText lastText;
    private EditText idText;
    private EditText phoneText;
    private EditText emailText;
    private EditText passwordText;
    private EditText confirmText;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        regFailed = findViewById(R.id.regFailed);

        firstText = findViewById(R.id.first_name);
        lastText = findViewById(R.id.last_name);
        idText = findViewById(R.id.id_number);
        phoneText = findViewById(R.id.phone_number);
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);
        confirmText = findViewById(R.id.confirm_password);

        findViewById(R.id.accountSignIn).setOnClickListener(this);
        findViewById(R.id.accountSignUp).setOnClickListener(this);

        // Firestore DB
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();

        FirebaseFirestoreSettings cacheSettings = new FirebaseFirestoreSettings.Builder()
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build();

        db.setFirestoreSettings(settings);
        db.setFirestoreSettings(cacheSettings);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.accountSignUp) {
            hideKeyboard(v);

            // Create User Account
            createAccount(emailText.getText().toString(), passwordText.getText().toString());
        } else if (i == R.id.accountSignIn) {
            hideKeyboard(v);

            // Return to Sign In Page
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String firstName = firstText.getText().toString();
        if (TextUtils.isEmpty(firstName)) {
            firstText.setError("Required.");
            valid = false;
        } else {
            firstText.setError(null);
        }

        String lastName = lastText.getText().toString();
        if (TextUtils.isEmpty(lastName)) {
            lastText.setError("Required.");
            valid = false;
        } else {
            lastText.setError(null);
        }

        String idNumber = idText.getText().toString();
        if (TextUtils.isEmpty(idNumber)) {
            idText.setError("Required.");
            valid = false;
        } else {
            idText.setError(null);
        }

        String phoneNumber = phoneText.getText().toString();
        if (TextUtils.isEmpty(phoneNumber)) {
            phoneText.setError("Required.");
            valid = false;
        } else {
            phoneText.setError(null);
        }

        String email = emailText.getText().toString();
        if (TextUtils.isEmpty(email)) {
            emailText.setError("Required.");
            valid = false;
        } else {
            emailText.setError(null);
        }

        String password = passwordText.getText().toString();
        if (TextUtils.isEmpty(password)) {
            passwordText.setError("Required.");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        String confirmPwd = confirmText.getText().toString();
        if (TextUtils.isEmpty(confirmPwd)) {
            confirmText.setError("Required.");
            valid = false;
        } else {
            confirmText.setError(null);
        }

        if (!password.equals(confirmPwd)) {
            passwordText.setError("Passwords Do Not Match");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }

    private void createAccount(String email, String password) {
        Log.i(TAG, "Account Sign Up :" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign up success, update UI with the signed-in user's information
                    Log.i(TAG, "createUserWithEmail:success");

                    // Registration Status
                    registerTokens registerTokens = new registerTokens();
                    registerTokens.execute();
                } else {
                    // If sign in fails, display a message to the user.
                    Log.i(TAG, "createUserWithEmail:failure", task.getException());

                    regFailed.setVisibility(View.VISIBLE);
                    regFailed.setText(task.getException().getLocalizedMessage());
                    updateUI(null);
                }

                if (!task.isSuccessful()) {
                    regFailed.setText(task.getException().getLocalizedMessage());
                }

                hideProgressDialog();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressDialog();

        if (user != null) {
            // Sign In User Automatically
            Intent intent = new Intent(SignUpActivity.this, DriverHome.class);
            startActivity(intent);
            finish();

        }
    }

    private String email() {
        return emailText.getText().toString();
    }

    private String firstName() {
        return firstText.getText().toString();
    }

    private String lastName() {
        return lastText.getText().toString();
    }

    private String idNumber() {
        return idText.getText().toString();
    }

    private String phoneNumber() {
        return phoneText.getText().toString();
    }

    private class registerTokens extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPreferences;

            String email = email();
            String phone = phoneNumber();
            String idNum = idNumber();
            String names =firstName() + " " + lastName();

            sharedPreferences = getApplicationContext().getSharedPreferences(DRIVER_DETAILS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("EMAIL", email);
            editor.putString("PHONE", phone);
            editor.putString("ID_NUM", idNum);
            editor.putString("NAMES", names);
            editor.apply();

            EMAIL = email;
            NAMES = names;
            PHONE = phone;

            Log.i(TAG, "Firebase Instance Created");
            STATUS = "Success";

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
                            String token = task.getResult().getToken();
                            Log.i(TAG, "Firebase InstanceId Token: " +  token);

                            MessagingServices messagingService =  new MessagingServices();
                            Context context = getApplicationContext();
                            messagingService.onNewToken(token, context);

                            // Finish Sign In Activity
                            Intent intent = new Intent(SignUpActivity.this, DriverHome.class);
                            startActivity(intent);
                            finish();
                        }
                    });

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

}
