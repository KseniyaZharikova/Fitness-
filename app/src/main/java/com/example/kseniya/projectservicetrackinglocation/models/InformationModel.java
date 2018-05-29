package com.example.kseniya.projectservicetrackinglocation.models;

import io.realm.RealmObject;

/**
 * Created by Kseniya on 21.05.2018.
 */

public class InformationModel extends RealmObject {
    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }



    private  String distance;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private  String time;
}
