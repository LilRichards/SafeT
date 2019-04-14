package com.example.sharn.safetui;

public class RouterInfo {
    String name;
    public double latitude;
    public double longitude;
    int rssi;
    double distance;
    int freq;
    double unitrssi;

    public RouterInfo(String name, int rssi, double distance, int freq, double unitrssi) {
        this.name = name;
        this.latitude = 0.0;
        this.longitude = 0.0;
        this.rssi = rssi;
        this.distance = distance;
        this.freq = freq;
        this.unitrssi = unitrssi;
    }
    public String getName(){
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int getFreq() {
        return freq;
    }

    public double getDistance() {
        return distance;
    }

    public int getRssi() {
        return rssi;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
