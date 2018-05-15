package com.example.kseniya.projectservicetrackinglocation;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Kseniya on 13.05.2018.
 */
public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    private static final int INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 30000;
    private final String LOG_LOCATION = " SERVICE_LOCATION";
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initGoogleClient();
    }

    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        Log.e(LOG_LOCATION, "initGoogleClient");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(LOG_LOCATION, " Log.e(LOG_LOCATION, \"onStartCommand\");");
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        Log.e(LOG_LOCATION, "onStartCommand");
        return START_STICKY;

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        setLocationReguestParams();
        if (PermissionUtils.isLocationPermissionGranted(getApplicationContext())) {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            Log.e(LOG_LOCATION, "startLocationUpdates");
        }
    }

    private void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        stopSelf();
        Log.e(LOG_LOCATION, "stopLocationUpdates");
    }

    private void setLocationReguestParams() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.e(LOG_LOCATION, "setLocationReguestParams");

    }

    final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            onLocationChanged(locationResult.getLastLocation());
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
        Log.e(LOG_LOCATION, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_LOCATION, "Connection suspended" + i);
    }


    @Override
    public void onLocationChanged(Location location) {
        // TODO send location to server when ready
        EventBus.getDefault().post(location);
    }

    @Override
    public void onDestroy() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            stopLocationUpdates();
            Log.i(LOG_LOCATION, "onDestroy");
        }
        super.onDestroy();
    }
}
