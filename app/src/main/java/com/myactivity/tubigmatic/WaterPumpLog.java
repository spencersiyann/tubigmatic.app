package com.myactivity.tubigmatic;

public class WaterPumpLog {
    private String status;
    private String timestamp;
    private Integer duration_seconds;

    public WaterPumpLog(String status, String timestamp, Integer duration_seconds) {
        this.status = status;
        this.timestamp = timestamp;
        this.duration_seconds = duration_seconds;
    }

    public String getStatus() {
        return status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Integer getDuration_seconds() {
        return duration_seconds;
    }
}
