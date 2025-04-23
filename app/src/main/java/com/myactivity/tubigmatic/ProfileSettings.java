package com.myactivity.tubigmatic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView txtDeviceID = findViewById(R.id.txtDeviceID);

        // Retrieve deviceID from SharedPreferences
        String deviceID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("deviceID", null);

        // Set device ID to txtDeviceID
        if (deviceID != null) {
            txtDeviceID.setText(deviceID);
        } else {
            txtDeviceID.setText("Device ID not found");
        }


        Button btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(view -> showMenuDialog());

        Button btnChangePass = findViewById(R.id.btnChangePass);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnChangePass.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ChangePassword.class);
            v.getContext().startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want to delete your account?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Show long toast
                            Toast.makeText(v.getContext(), "Your account was deleted.", Toast.LENGTH_LONG).show();

                            // Redirect to LoginPage
                            Intent intent = new Intent(v.getContext(), LoginPage.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
                            v.getContext().startActivity(intent);
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Close dialog on "No"
                        .show();
            }
        });




    }


    private void showMenuDialog() {
        String[] menuItems = {"Home", "Profile", "Logout"}; // Removed "Settings"

        // Apply underline and green color to "Profile"
        SpannableString styledProfile = new SpannableString(menuItems[1]); // Index 1 = Profile
        styledProfile.setSpan(new UnderlineSpan(), 0, menuItems[1].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        styledProfile.setSpan(new ForegroundColorSpan(Color.parseColor("#3E6032")), 0, menuItems[1].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Replace the original "Profile" item with the styled version
        CharSequence[] formattedMenuItems = {menuItems[0], styledProfile, menuItems[2]};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setItems(formattedMenuItems, (dialog, which) -> {
                    if (menuItems[which].equals("Logout")) {
                        Intent intent = new Intent(this, LoginPage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
                        startActivity(intent);
                        finish(); // Close current activity
                    } else if (menuItems[which].equals("Home")) {
                        Intent intent = new Intent(this, HomePage.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
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
            params.width = 500; // Adjust width (change as needed)
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