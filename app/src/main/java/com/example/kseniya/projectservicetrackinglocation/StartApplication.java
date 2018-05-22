package com.example.kseniya.projectservicetrackinglocation;

import android.app.Application;
import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class StartApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder().name("myrealm.realm").build();
        Realm.setDefaultConfiguration(config);

    }

    public static StartApplication get(Context context) {

        return (StartApplication) context.getApplicationContext();

    }
}
