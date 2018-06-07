package com.example.kseniya.projectservicetrackinglocation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.kseniya.projectservicetrackinglocation.utils.PermissionUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Kseniya on 13.05.2018.
 */
public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, LocationListener {
    private static final int INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 10000;
    private final String LOG_LOCATION = " SERVICE_LOCATION";
    private GoogleApiClient mGoogleApiClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;

    public class LocalBinder extends Binder {


        public LocationUpdateService getService() {
            return LocationUpdateService.this;
        }


    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
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
    public void startLocationUpdates() {
        setLocationReguestParams();
        if (PermissionUtils.isLocationPermissionGranted(getApplicationContext())) {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback, Looper.myLooper());
            Log.e(LOG_LOCATION, "startLocationUpdates");
        }
    }

    public void stopLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        stopSelf();
        Log.e(LOG_LOCATION, "stopLocationUpdates");
    }

    public void getLastLocation(OnCompleteListener<Location> onCompleteListener) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //  Log.e(LOG_LOCATION, "stopLocationUpdates");
            mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(onCompleteListener);
        }
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
