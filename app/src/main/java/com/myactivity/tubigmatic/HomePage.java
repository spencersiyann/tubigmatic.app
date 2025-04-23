package com.myactivity.tubigmatic;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomePage extends AppCompatActivity {

    private TextView fetchMinimumLevelTxt, fetchMaximumLevelTxt;
    Button btnSetLevel;

    private ProgressBar progressBar;
    private TextView percentageText;

    FirebaseAuth mAuth;
    FirebaseUser user;

    private String deviceID;

    private DatabaseReference moistureRef, pumpStatusRef, moistureLevelsRef;
    private TextView pumpStatus;

    private Button btnLogs, btnWifiCredential;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deviceID = getIntent().getStringExtra("deviceID");


        TextView txtDeviceID1 = findViewById(R.id.txtDeviceID1);
        // Fetch deviceID from intent
        deviceID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("deviceID", null);

        // Set device ID to txtDeviceID
        if (deviceID != null) {
            txtDeviceID1.setText("Device ID: " + deviceID);
        } else {
            txtDeviceID1.setText("Device ID not found");
        }


        Button btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(view -> showMenuDialog(deviceID));

        fetchMinimumLevelTxt = findViewById(R.id.fetchMinimumLevelTxt);
        fetchMaximumLevelTxt = findViewById(R.id.fetchMaximumLevelTxt);
        btnSetLevel = findViewById(R.id.btnSetLevel);

        progressBar = findViewById(R.id.Gauge);
        percentageText = findViewById(R.id.percentageText);
       // Button btnMoistureLevel = findViewById(R.id.btnMoistureLevel);
        pumpStatus = findViewById(R.id.pumpStatus);
        Button btnOpenSelectPlantsDialog = findViewById(R.id.btnOpenSelectPlantsDialog);

        btnLogs = findViewById(R.id.btnLogs);

        btnLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, ActivityLogs.class);
                intent.putExtra("deviceID", deviceID); // Pass deviceID
                startActivity(intent);
            }
        });

        btnWifiCredential = findViewById(R.id.btnWifiCredential);

        btnWifiCredential.setOnClickListener(v -> {
            Intent intent = new Intent(HomePage.this, UpdateWifiCredential.class);
            intent.putExtra("deviceID", deviceID);  // Replace this with actual logged-in deviceID
            startActivity(intent);
        });



        btnOpenSelectPlantsDialog.setOnClickListener(v -> showPlantSelectionDialog(deviceID));

      //  btnMoistureLevel.setOnClickListener(v -> openMoistureInputDialog());

        btnSetLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSetLevelDialog();
            }
        });

        TextView browseWebsite = findViewById(R.id.browseWebsiteTxt);

        browseWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String facebookUrl = "http://tubigmatic.free.nf/";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl));
                startActivity(intent);
            }
        });

        TextView txtUserManual = findViewById(R.id.txtUserManual);

        txtUserManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePage.this, Instruction.class);
                startActivity(intent);
            }
        });

        // 🔹 Initialize Firebase Database reference
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference("Devices").child(deviceID);


        // 🔹 Fetch real-time soil moisture data
        deviceRef.child("SoilMoisture").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Integer soilMoistureLevel = snapshot.getValue(Integer.class);
                    if (soilMoistureLevel != null) {
                        updateUI(soilMoistureLevel);
                    }
                } else {
                    Log.e("Firebase", "No SoilMoisture data for this device");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "SoilMoisture DB Error: " + error.getMessage());
            }
        });

        deviceRef.child("PumpStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pumpState = snapshot.getValue(String.class);
                    if (pumpState != null) {
                        updatePumpStatusUI(pumpState);
                    }
                } else {
                    Log.e("Firebase", "No PumpStatus data for this device");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "PumpStatus DB Error: " + error.getMessage());
            }
        });


        // 🔹 Initialize Firebase database reference
       // moistureLevelsRef = FirebaseDatabase.getInstance().getReference("MoistureLevels");

        // 🔹 Fetch real-time minLevel & maxLevel from Firebase
        deviceRef.child("MoistureLevels").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int minLevel = snapshot.child("minLevel").getValue(Integer.class);
                    int maxLevel = snapshot.child("maxLevel").getValue(Integer.class);

                    fetchMinimumLevelTxt.setText(minLevel + "%");
                    fetchMaximumLevelTxt.setText(maxLevel + "%");
                } else {
                    Log.e("Firebase", "No data found for MoistureLevels");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database error: " + error.getMessage());
            }
        });

    }





    private void updatePumpStatusUI(String pumpState) {
        pumpStatus.setText("Pump Status: " + pumpState);
    }


    private void updateUI(int soilMoistureLevel) {
        progressBar.setProgress(soilMoistureLevel);
        percentageText.setText(soilMoistureLevel + "%");
    }


    private void showPlantSelectionDialog(String deviceID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_select_plant, null);
        builder.setView(dialogView);

        Spinner spinnerPlants = dialogView.findViewById(R.id.spinnerPlants);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnAddPlant = dialogView.findViewById(R.id.btnAddPlant);
        TextView spinnerErrorText = dialogView.findViewById(R.id.spinnerErrorText);

        DatabaseReference plantsRef = FirebaseDatabase.getInstance()
                .getReference("Devices")
                .child(deviceID)
                .child("Plants");

        List<String> plantList = new ArrayList<>();
        plantList.add("Select Plant");
        Map<String, String[]> plantLevels = new HashMap<>();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, plantList);
        spinnerPlants.setAdapter(adapter);

        plantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                plantList.clear();
                plantList.add("Select Plant");
                plantLevels.clear();

                for (DataSnapshot plantSnapshot : snapshot.getChildren()) {
                    String plantName = plantSnapshot.child("PlantName").getValue(String.class);
                    String minLevel = String.valueOf(plantSnapshot.child("MinLevel").getValue(Integer.class));
                    String maxLevel = String.valueOf(plantSnapshot.child("MaxLevel").getValue(Integer.class));

                    if (plantName != null && minLevel != null && maxLevel != null) {
                        plantList.add(plantName);
                        plantLevels.put(plantName, new String[]{minLevel + "%", maxLevel + "%"});
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Failed to load plants", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String selectedPlant = spinnerPlants.getSelectedItem().toString();

            if (selectedPlant.equals("Select Plant")) {
                spinnerErrorText.setText("Please select a plant.");
                spinnerErrorText.setVisibility(View.VISIBLE);
            } else {
                String[] levels = plantLevels.get(selectedPlant);
                if (levels != null) {
                    int minLevel = Integer.parseInt(levels[0].replace("%", ""));
                    int maxLevel = Integer.parseInt(levels[1].replace("%", ""));

                    fetchMinimumLevelTxt.setText(levels[0]);
                    fetchMaximumLevelTxt.setText(levels[1]);

                    // Update Firebase MoistureLevels under the same deviceID
                    Map<String, Integer> moistureLevels = new HashMap<>();
                    moistureLevels.put("minLevel", minLevel);
                    moistureLevels.put("maxLevel", maxLevel);

                    DatabaseReference moistureLevelsRef = FirebaseDatabase.getInstance()
                            .getReference("Devices")
                            .child(deviceID)
                            .child("MoistureLevels");

                    moistureLevelsRef.setValue(moistureLevels)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "Moisture levels updated successfully!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "Failed to update moisture levels!", Toast.LENGTH_SHORT).show();
                            });
                }
            }
        });

        btnAddPlant.setOnClickListener(v -> showAddPlantDialog());
        dialog.show();
    }


    private void showAddPlantDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_plant, null);
        builder.setView(dialogView);

        EditText inputPlantName = dialogView.findViewById(R.id.inputPlantName);
        EditText inputMinLevel = dialogView.findViewById(R.id.inputMinLevel);
        EditText inputMaxLevel = dialogView.findViewById(R.id.inputMaxLevel);
        Button btnSavePlant = dialogView.findViewById(R.id.btnSavePlant);

        // 🔹 Updated to point to current device's Plants list
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("Devices")
                .child(deviceID)
                .child("Plants");

        AlertDialog dialog = builder.create();

        btnSavePlant.setOnClickListener(v -> {
            String plantName = inputPlantName.getText().toString().trim();
            String minLevel = inputMinLevel.getText().toString().trim();
            String maxLevel = inputMaxLevel.getText().toString().trim();

            if (plantName.isEmpty() || minLevel.isEmpty() || maxLevel.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int min = Integer.parseInt(minLevel);
            int max = Integer.parseInt(maxLevel);

            if (min < 0 || min > 100 || max < 0 || max > 100 || min >= max) {
                Toast.makeText(this, "Min level must be between 0-100 and less than max level", Toast.LENGTH_SHORT).show();
                return;
            }

            String plantId = databaseReference.push().getKey();
            Map<String, Object> plantData = new HashMap<>();
            plantData.put("PlantName", plantName);
            plantData.put("MinLevel", min);
            plantData.put("MaxLevel", max);

            if (plantId != null) {
                databaseReference.child(plantId).setValue(plantData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Plant added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to add plant", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();
    }






    private void openSetLevelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_level, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Spinner minLevelSpinner = dialogView.findViewById(R.id.minLevelSpinner);
        Spinner maxLevelSpinner = dialogView.findViewById(R.id.maxLevelSpinner);
        Button btnSetLevel1 = dialogView.findViewById(R.id.btnSetLevel1);

        Integer[] levelOptions = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, levelOptions);

        minLevelSpinner.setAdapter(adapter);
        maxLevelSpinner.setAdapter(adapter);

        minLevelSpinner.setSelection(2); // Default to 20
        maxLevelSpinner.setSelection(8); // Default to 80

        btnSetLevel1.setOnClickListener(v -> {
            int minLevel = (int) minLevelSpinner.getSelectedItem();
            int maxLevel = (int) maxLevelSpinner.getSelectedItem();

            if (maxLevel < minLevel) {
                Toast.makeText(dialogView.getContext(), "Max level must be greater than Min level", Toast.LENGTH_SHORT).show();
            } else if (maxLevel == minLevel) {
                Toast.makeText(dialogView.getContext(), "Max level must not be equal to Min level", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Integer> moistureLevels = new HashMap<>();
                moistureLevels.put("minLevel", minLevel);
                moistureLevels.put("maxLevel", maxLevel);

                // ✅ Save under correct deviceID path
                DatabaseReference moistureLevelsRef = FirebaseDatabase.getInstance().getReference("Devices")
                        .child(deviceID)
                        .child("MoistureLevels");

                moistureLevelsRef.setValue(moistureLevels)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(dialogView.getContext(), "Levels Set Successfully!", Toast.LENGTH_SHORT).show();

                            fetchMinimumLevelTxt.setText(minLevel + "%");
                            fetchMaximumLevelTxt.setText(maxLevel + "%");

                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(dialogView.getContext(), "Failed to set levels!", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();
    }



    private void showMenuDialog(String deviceID) {
        String[] menuItems = {"Home", "Change Password", "Logout"}; // Removed "Settings"

        // Apply underline and green color to "Home"
        SpannableString styledHome = new SpannableString(menuItems[0]);
        styledHome.setSpan(new UnderlineSpan(), 0, menuItems[0].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledHome.setSpan(new ForegroundColorSpan(Color.parseColor("#3E6032")), 0, menuItems[0].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Replace the original "Home" item with the styled version
        CharSequence[] formattedMenuItems = {styledHome, menuItems[1], menuItems[2]};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setItems(formattedMenuItems, (dialog, which) -> {
                    if (menuItems[which].equals("Logout")) {
                        Intent intent = new Intent(this, LoginPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
                        startActivity(intent);
                        finish(); // Close current activity
                    } else if (menuItems[which].equals("Change Password")) {

                        // Save deviceID to SharedPreferences
                        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("deviceID", deviceID)
                                .apply();

                        Intent intent = new Intent(this, ChangePassword.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
                        intent.putExtra("deviceID", deviceID); // Pass deviceID
                        startActivity(intent);
                        finish(); // Close current activity
                    } else {
                        Toast.makeText(this, "Selected: " + menuItems[which], Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Adjust the width and position after showing the dialog
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = 640; // Adjust width (change as needed)
            params.gravity = Gravity.TOP | Gravity.START; // Position at upper left
            params.x = 20; // Adjust horizontal position
            params.y = 100; // Adjust vertical position
            window.setAttributes(params);
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button when logged in
    }

}