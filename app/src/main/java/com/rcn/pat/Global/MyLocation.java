package com.rcn.pat.Global;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MyLocation {
    private String address;

    @PrimaryKey(autoGenerate = true)
    private int id;
    private double latitude;
    private double longitude;
    private long time;
    private String observaciones="";
    private String timeRead;


    public MyLocation() {
    }

    public MyLocation(double latitude, double longitude, int id) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTimeRead() {
        return timeRead;
    }

    public void setTimeRead(String timeRead) {
        this.timeRead = timeRead;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}