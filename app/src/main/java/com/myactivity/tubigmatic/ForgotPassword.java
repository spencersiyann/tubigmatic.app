package com.myactivity.tubigmatic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnBackInForgotPass = findViewById(R.id.btnBackInForgotPass);

        btnBackInForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPassword.this, LoginPage.class);
                startActivity(intent);
            }
        });

        EditText inputEmailForgotPass = findViewById(R.id.inputEmailForgotPass);
        Button btnSend = findViewById(R.id.btnSend);
        EditText inputDeviceIDRequest = findViewById(R.id.inputDeviceIDRequest);

        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("Requests");
        DatabaseReference devicesRef = FirebaseDatabase.getInstance().getReference("Devices");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmailForgotPass.getText().toString().trim();
                String deviceID = inputDeviceIDRequest.getText().toString().trim();

                if (deviceID.isEmpty()) {
                    inputDeviceIDRequest.setError("Please enter your Device ID.");
                }  else if (email.isEmpty()) {
                    inputEmailForgotPass.setError("Please enter your email.");
                } else if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
                    inputEmailForgotPass.setError("Your email is not verified. Use a valid Gmail address.");
                } else {
                    // Check if the device ID exists in the 'Devices' table
                    devicesRef.child(deviceID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // Device ID exists, proceed with sending request
                                requestsRef.child(deviceID).child("email").setValue(email)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ForgotPassword.this, "Request sent for " + email, Toast.LENGTH_SHORT).show();
                                                    Toast.makeText(ForgotPassword.this, "We'll notify you once the request was processed, Thank you!", Toast.LENGTH_LONG).show();
                                                    startActivity(new Intent(ForgotPassword.this, LoginPage.class));
                                                } else {
                                                    Toast.makeText(ForgotPassword.this, "Failed to send request. Try again.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(ForgotPassword.this, "Sorry, we can't process your request because the Device ID is not found.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ForgotPassword.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}
