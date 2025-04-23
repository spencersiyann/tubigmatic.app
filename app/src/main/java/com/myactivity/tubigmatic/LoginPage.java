package com.myactivity.tubigmatic;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPage extends AppCompatActivity {

    private boolean isPasswordVisible = false; // Track visibility state

    // Hardcoded credentials
    private static final String HARDCODED_DEVICE_ID = "DVC123";
    private static final String HARDCODED_PASSWORD = "Syani1234";

    EditText inputDeviceID, inputPassword;
    Button btnLogin;
    FirebaseAuth mAuth;

    private FirebaseDatabase database;
    private DatabaseReference devicesRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance();
        devicesRef = database.getReference("Devices");

        // Initialize UI elements
         btnLogin = findViewById(R.id.btnLogin);
         inputDeviceID = findViewById(R.id.inputDeviceID);
         inputPassword = findViewById(R.id.inputPassword);
        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);
        TextView passwordError = findViewById(R.id.passwordError);
        TextView deviceError = findViewById(R.id.deviceError);
        TextView txtForgotPass = findViewById(R.id.txtForgotPass);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceID = inputDeviceID.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                passwordError.setVisibility(View.GONE); // Hide error initially
                deviceError.setVisibility(View.GONE);

                if (TextUtils.isEmpty(deviceID)) {
                    Toast.makeText(LoginPage.this, "Device ID is required", Toast.LENGTH_LONG).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginPage.this, "Password is required", Toast.LENGTH_LONG).show();
                    return;
                }

                validateLogin(deviceID, password);
            }
        });




       /* btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredDeviceID = inputDeviceID.getText().toString().trim();
                String enteredPassword = inputPassword.getText().toString().trim();

                passwordError.setVisibility(View.GONE); // Hide error initially
                deviceError.setVisibility(View.GONE);

                if (TextUtils.isEmpty(enteredDeviceID)) {
                    deviceError.setText("Device ID is required!");
                    deviceError.setVisibility(View.VISIBLE);
                    return;
                }

                if (TextUtils.isEmpty(enteredPassword)) {
                    passwordError.setText("Password is required!");
                    passwordError.setVisibility(View.VISIBLE);
                    return;
                }

                // Reference to the Devices node
                DatabaseReference devicesRef = FirebaseDatabase.getInstance()
                        .getReference()
                        .child("tubigmatic-admin")
                        .child("Devices")
                        .child(enteredDeviceID);

                devicesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean deviceFound = false;

                        for (DataSnapshot deviceSnapshot : snapshot.getChildren()) {
                            String storedDeviceID = deviceSnapshot.child("deviceID").getValue(String.class);
                            String storedPassword = deviceSnapshot.child("password").getValue(String.class);

                            // Debugging logs
                            Log.d("DEBUG", "Checking Device: " + storedDeviceID);

                            if (storedDeviceID != null && storedDeviceID.equals(enteredDeviceID)) {
                                deviceFound = true;

                                if (storedPassword != null && storedPassword.equals(enteredPassword)) {
                                    Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginPage.this, HomePage.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    passwordError.setText("Wrong Device ID or Password!");
                                    passwordError.setVisibility(View.VISIBLE);
                                }
                                break; // Stop loop since device is found
                            }
                        }

                        if (!deviceFound) {
                            deviceError.setText("Device ID not found!");
                            deviceError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(LoginPage.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }); */


        // Toggle password visibility
        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.hidepassword);
                } else {
                    inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.seepassword);
                }
                inputPassword.setSelection(inputPassword.getText().length()); // Move cursor to end
                isPasswordVisible = !isPasswordVisible;
            }
        });



        /* Login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceID = inputDeviceID.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                passwordError.setVisibility(View.GONE); // Hide error initially
                deviceError.setVisibility(View.GONE);

                // Check if fields are empty
                if (deviceID.isEmpty()) {
                    deviceError.setText("");
                    deviceError.setText("Device ID is required!");
                    deviceError.setVisibility(View.VISIBLE);

                } else if (password.isEmpty()) {
                    passwordError.setText("             Password is required!");
                    passwordError.setVisibility(View.VISIBLE);
                } else {
                    // Validate hardcoded credentials
                    if (deviceID.equals(HARDCODED_DEVICE_ID) && password.equals(HARDCODED_PASSWORD)) {
                        Toast.makeText(LoginPage.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginPage.this, HomePage.class);
                        startActivity(intent);
                    } else {
                        passwordError.setText("Wrong DeviceID or Password!");
                        passwordError.setVisibility(View.VISIBLE);
                    }
                }
            }
        }); */

        // Forgot password click listener
        txtForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginPage.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }

    private void validateLogin(String deviceID, String password) {
        // Look for the deviceID in the database
        devicesRef.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    // Device ID exists, check if the password matches
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {

                        // Save deviceID to SharedPreferences
                        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("deviceID", deviceID)
                                .apply();

                        // Proceed to Homepage (login successful)
                        Toast.makeText(LoginPage.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginPage.this, HomePage.class);
                        intent.putExtra("deviceID", deviceID); // Pass deviceID
                        startActivity(intent);

                    } else {
                        // Incorrect password
                        Toast.makeText(LoginPage.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Device ID not found
                    Toast.makeText(LoginPage.this, "Device ID not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(LoginPage.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public void onBackPressed() {
        // Disable back button when logged in
    }
}