package com.example.smartumbrella;

public class LocationLog {
    private final String timestamp;
    private final double latitude;
    private final double longitude;

    public LocationLog(String timestamp, double latitude, double longitude) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
