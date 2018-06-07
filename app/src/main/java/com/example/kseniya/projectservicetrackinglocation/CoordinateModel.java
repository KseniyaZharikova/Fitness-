package com.example.kseniya.projectservicetrackinglocation;

import android.location.Location;


public class CoordinateModel extends Location {
    boolean stop = false;


    public CoordinateModel(String provider) {
        super(provider);
    }

    public CoordinateModel(Location l) {
        super(l);
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}