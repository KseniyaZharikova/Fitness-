package com.example.kseniya.projectservicetrackinglocation.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Kseniya on 21.05.2018.
 */

public class InformationModel extends RealmObject {


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey
    private int id;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }


    private String distance;


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    private String time;

    public String getCurrentTimeDate() {
        return currentTimeDate;
    }

    public void setCurrentTimeDate(String currentTimeDate) {
        this.currentTimeDate = currentTimeDate;
    }

    private String currentTimeDate;

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    private  int rate;
}
