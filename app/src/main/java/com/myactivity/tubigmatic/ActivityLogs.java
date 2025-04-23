package com.myactivity.tubigmatic;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogs extends AppCompatActivity {

    private ListView logsListView;
    private LogsAdapter logsAdapter;
    private List<WaterPumpLog> logsList;

    private Button btnBackLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);

        logsListView = findViewById(R.id.logsListView);
        logsList = new ArrayList<>();
        logsAdapter = new LogsAdapter(this, logsList);
        logsListView.setAdapter(logsAdapter);
        btnBackLogs = findViewById(R.id.btnBackLogs);

        btnBackLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityLogs.this, HomePage.class);
                startActivity(intent);
            }
        });

        // Fetch logs from Firebase
        fetchLogs();
    }

    private void fetchLogs() {
        String deviceID = getIntent().getStringExtra("deviceID");

        if (deviceID == null || deviceID.isEmpty()) {
            Toast.makeText(this, "No device ID provided", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference logsRef = FirebaseDatabase.getInstance()
                .getReference("Devices")
                .child(deviceID)
                .child("WaterPumpLogs");

        logsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                logsList.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(ActivityLogs.this, "No logs found for device: " + deviceID, Toast.LENGTH_SHORT).show();
                    logsAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot logSnapshot : snapshot.getChildren()) {
                    String status = logSnapshot.child("status").getValue(String.class);
                    String timestamp = logSnapshot.child("timestamp").getValue(String.class);
                    Integer duration_seconds = logSnapshot.child("duration_seconds").getValue(Integer.class);

                    if (status != null && timestamp != null) {
                        logsList.add(0, new WaterPumpLog(status, timestamp, duration_seconds));
                    }
                }

                logsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ActivityLogs.this, "Error fetching logs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
