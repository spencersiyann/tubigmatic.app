package com.myactivity.tubigmatic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class LogsAdapter extends ArrayAdapter<WaterPumpLog> {

    public LogsAdapter(Context context, List<WaterPumpLog> logs) {
        super(context, 0, logs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.logs_list_item, parent, false);
        }

        WaterPumpLog log = getItem(position);

        TextView statusText = convertView.findViewById(R.id.statusText);
        TextView timeText = convertView.findViewById(R.id.timeText);

        if (log != null) {
            statusText.setText(log.getStatus());
            timeText.setText(log.getTimestamp());

            // If the log has a duration, append it
            if (log.getDuration_seconds() != null) {
                String durationText = " Duration: " + log.getDuration_seconds() + "s";
                timeText.append(durationText); // Add duration after timestamp
            }

            // Change text color based on status
            if ("ON".equals(log.getStatus())) {
                statusText.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else if ("OFF".equals(log.getStatus())) {
                statusText.setTextColor(getContext().getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        return convertView;
    }
}
