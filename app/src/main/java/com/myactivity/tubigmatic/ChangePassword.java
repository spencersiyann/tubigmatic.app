package com.myactivity.tubigmatic;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.regex.Pattern;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePassword extends AppCompatActivity {

    EditText currentPassTxt, newPassTxt, confirmPassTxt;
    Button btnSavePass;
    private boolean isPasswordVisible = false; // Track visibility state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnBackInChangePass = findViewById(R.id.btnBackInChangePass);

        btnBackInChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangePassword.this, HomePage.class);
                startActivity(intent);
            }
        });

        TextView fetchDeviceIDCP = findViewById(R.id.fetchDeviceIDCP);
        // Fetch deviceID from intent
        String deviceID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("deviceID", null);

        // Set device ID to txtDeviceID
        if (deviceID != null) {
            fetchDeviceIDCP.setText("Device ID: " + deviceID);
        } else {
            fetchDeviceIDCP.setText("Device ID not found");
        }


        Button btnMenu = findViewById(R.id.btnMenu);

        btnMenu.setOnClickListener(view -> showMenuDialog());

        EditText currentPassTxt = findViewById(R.id.currentPassTxt);
        EditText newPassTxt = findViewById(R.id.newPassTxt);
        EditText confirmPassTxt = findViewById(R.id.confirmPassTxt);
        Button btnSavePass = findViewById(R.id.btnSavePass);
        ImageView btnTogglePassword1 = findViewById(R.id.btnTogglePassword1);
        ImageView btnTogglePassword2 = findViewById(R.id.btnTogglePassword2);
        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);

        btnTogglePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    currentPassTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.hidepassword);
                } else {
                    currentPassTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword.setImageResource(R.drawable.seepassword);
                }
                currentPassTxt.setSelection(currentPassTxt.getText().length()); // Move cursor to end
                isPasswordVisible = !isPasswordVisible;
            }
        });


        btnTogglePassword1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    newPassTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword1.setImageResource(R.drawable.hidepassword);
                } else {
                    newPassTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword1.setImageResource(R.drawable.seepassword);
                }
                newPassTxt.setSelection(newPassTxt.getText().length()); // Move cursor to end
                isPasswordVisible = !isPasswordVisible;
            }
        });

        btnTogglePassword2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    confirmPassTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    btnTogglePassword2.setImageResource(R.drawable.hidepassword);
                } else {
                    confirmPassTxt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    btnTogglePassword2.setImageResource(R.drawable.seepassword);
                }
                confirmPassTxt.setSelection(confirmPassTxt.getText().length()); // Move cursor to end
                isPasswordVisible = !isPasswordVisible;
            }
        });




        btnSavePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPass = currentPassTxt.getText().toString().trim();
                String newPass = newPassTxt.getText().toString().trim();
                String confirmPass = confirmPassTxt.getText().toString().trim();

                if (currentPass.isEmpty()) {
                    currentPassTxt.setError("This field is empty.");
                    return;
                }
                if (newPass.isEmpty()) {
                    newPassTxt.setError("This field is empty.");
                    return;
                }
                if (newPass.length() < 8 || newPass.length() > 16 ||
                        !Pattern.compile("[A-Z]").matcher(newPass).find() ||
                        !Pattern.compile("[a-z]").matcher(newPass).find() ||
                        !Pattern.compile("[0-9]").matcher(newPass).find()) {
                    newPassTxt.setError("Password must be 8-16 characters with 1 uppercase, 1 lowercase, and at least 1 number.");
                    return;
                }
                if (confirmPass.isEmpty()) {
                    confirmPassTxt.setError("This field is empty.");
                    return;
                }
                if (!confirmPass.equals(newPass)) {
                    confirmPassTxt.setError("Passwords must match.");
                    return;
                }

                // Fetch stored password from Firebase
                String deviceID = getSharedPreferences("UserPrefs", MODE_PRIVATE).getString("deviceID", null);
                if (deviceID == null) {
                    Toast.makeText(ChangePassword.this, "Device ID not found.", Toast.LENGTH_LONG).show();
                    return;
                }

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Devices").child(deviceID);
                databaseReference.child("password").get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String storedPassword = task.getResult().getValue(String.class);
                        if (!currentPass.equals(storedPassword)) {
                            currentPassTxt.setError("Incorrect current password.");
                        } else {
                            // Update password
                            databaseReference.child("password").setValue(newPass)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(ChangePassword.this, "Password changed successfully!", Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ChangePassword.this, HomePage.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(ChangePassword.this, "Failed to update password.", Toast.LENGTH_LONG).show());
                        }
                    } else {
                        Toast.makeText(ChangePassword.this, "Error fetching password.", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    private void showMenuDialog() {
        String[] menuItems = {"Home", "Change Password", "Logout"}; // Removed "Settings"

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
                        Intent intent = new Intent(this, ProfileSettings.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clears back stack
                        startActivity(intent);
                        finish(); // Close current activity
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