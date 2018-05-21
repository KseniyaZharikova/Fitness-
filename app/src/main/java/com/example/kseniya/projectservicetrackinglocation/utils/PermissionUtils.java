package com.example.kseniya.projectservicetrackinglocation.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

import com.example.kseniya.projectservicetrackinglocation.utils.AppConstants;


public final class PermissionUtils {

    public static boolean isLocationServicesEnabled(Context context) {
        LocationManager mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return mLocationManager != null &&
                mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isLocationPermissionGranted(Context context) {
        return ActivityCompat
                .checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkLocationPermission(Activity activity) {
        String[] permission = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (isLocationPermissionGranted(activity)) return true;

        ActivityCompat.requestPermissions(activity, permission, AppConstants.REQUEST_CODE_LOCATION_PERMISSION);
        return false;
    }
}
