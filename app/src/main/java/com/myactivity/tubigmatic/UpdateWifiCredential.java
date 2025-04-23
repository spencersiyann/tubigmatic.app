package com.myactivity.tubigmatic;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateWifiCredential extends AppCompatActivity {

    EditText inputSSID, inputPassword;
    Button btnUpdate;

    DatabaseReference wifiRef;
    String deviceID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_wifi_credential);

        inputSSID = findViewById(R.id.inputSSID);
        inputPassword = findViewById(R.id.inputPassword);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Get deviceID from Intent
        deviceID = getIntent().getStringExtra("deviceID");

        if (deviceID == null || deviceID.isEmpty()) {
            Toast.makeText(this, "Device ID is missing!", Toast.LENGTH_LONG).show();
            finish(); // Close activity if no deviceID
            return;
        }

        // Firebase ref to Devices > DVCxxx > WiFi
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        wifiRef = database.getReference("Devices").child(deviceID).child("WiFi");

        btnUpdate.setOnClickListener(v -> {
            String ssid = inputSSID.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!ssid.isEmpty() && !password.isEmpty()) {
                wifiRef.child("ssid").setValue(ssid);
                wifiRef.child("password").setValue(password);
                Toast.makeText(this, "Wi-Fi credentials updated for " + deviceID, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(UpdateWifiCredential.this, HomePage.class));
                finish();
            } else {
                Toast.makeText(this, "Please enter both SSID and Password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}