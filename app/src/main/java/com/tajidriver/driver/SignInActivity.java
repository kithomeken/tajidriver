package com.tajidriver.driver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import static com.tajidriver.configuration.TajiCabs.EMAIL;

public class SignInActivity extends Firebase implements View.OnClickListener {
    private static final String TAG = "Sign In Activity";

    private FirebaseAuth mAuth;
    private TextView authFailed;
    private EditText emailText;
    private EditText passwordText;

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
        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        authFailed = findViewById(R.id.authFailed);
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);

        findViewById(R.id.accountSignIn).setOnClickListener(this);
        findViewById(R.id.accountSignUp).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.accountSignUp) {
            hideKeyboard(v);

            // Navigate to Create Account Page
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        } else if (i == R.id.accountSignIn) {
            hideKeyboard(v);

            // Sign In User
            signIn(emailText.getText().toString(), passwordText.getText().toString());
        }
    }


    private boolean validateForm() {
        boolean valid = true;

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

        return valid;
    }

    private void signIn(final String email, String password) {
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "Firebase Signing In: success");

                            // Firebase Messaging Token Registration
                            FirebaseInstanceId.getInstance().getInstanceId()
                                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    if (!task.isSuccessful()) {
                                        Log.e(TAG, "XXXXXXXXXXXXXXXXXXXXXXXXXX getInstanceId failed", task.getException());
                                        return;
                                    }

                                    // Get new Instance ID token
                                    String token = task.getResult().getToken();
                                    EMAIL = email();

                                    // Log and toast
                                    Log.d(TAG, "XXXXXXXXXXXXXXXXXXXXXXXXX " +  token);

                                    MessagingServices messagingService =  new MessagingServices();
                                    Context context = getApplicationContext();
                                    messagingService.onNewToken(context, token);

                                    Intent intent = new Intent(SignInActivity.this, DriverHome.class);
                                    startActivity(intent);

                                    hideProgressDialog();
                                    finish();
                                    }
                                });
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "Firebase Signing In: Failed", task.getException());

                            authFailed.setVisibility(View.VISIBLE);

                            updateUI(null);
                        }

                        if (!task.isSuccessful()) {
                            authFailed.setText(R.string.auth_failed);
                        }

                        hideProgressDialog();
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Sign In User Automatically
            Intent intent = new Intent(SignInActivity.this, DriverHome.class);
            startActivity(intent);
            finish();

            hideProgressDialog();
        }
    }

    private String email() {
        return emailText.getText().toString();
    }
}
