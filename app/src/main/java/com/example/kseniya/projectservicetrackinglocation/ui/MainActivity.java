package com.example.kseniya.projectservicetrackinglocation.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.kseniya.projectservicetrackinglocation.LocationUpdateService;
import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.utils.AppConstants;
import com.example.kseniya.projectservicetrackinglocation.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    private ArrayList<Location> mWalkedList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    Marker mMarker;
    TextView distance;
    Location mStartLocation;
    private long lastPause;
    Chronometer chronometer;
    Button start, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(myToolBar);
        Location location = new Location("");
        location.setLatitude(42.874);
        location.setLongitude(74.5698);
        mStartLocation = location;
        distance = findViewById(R.id.distance);
        chronometer = findViewById(R.id.chronometer);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        initMap();

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.run_image)
                .build();
        new DrawerBuilder().withActivity(this).build();
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("My result")
                .withSelectedColor(getResources().getColor(R.color.material_drawer_dark_background))
                .withTextColor(getResources().getColor(R.color.white))
                .withSelectedTextColor(getResources().getColor(R.color.white));
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("My progress")
                .withSelectedColor(getResources().getColor(R.color.material_drawer_dark_background))
                .withTextColor(getResources().getColor(R.color.white))
                .withSelectedTextColor(getResources().getColor(R.color.white));

        new DrawerBuilder()
                .withActivity(this)
                .withSliderBackgroundColor(getResources().getColor(R.color.material_drawer_dark_background))
                .withToolbar(myToolBar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new SecondaryDrawerItem()
                ).build();
    }


    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void locationRecived(Location location) {
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker());
        mGoogleMap.clear();
        mMarker = mGoogleMap.addMarker(options);
        saveRoad(location);
        countDistance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                startLocationService();
                if (lastPause != 0) {
                    chronometer.setBase(chronometer.getBase() + SystemClock.elapsedRealtime() - lastPause);
                    start.setEnabled(false);

                } else {
                    chronometer.setBase(SystemClock.elapsedRealtime());
                }
                start.setEnabled(false);
                stop.setEnabled(true);
                chronometer.start();

                break;

            case R.id.stop:
                stopService(new Intent(this, LocationUpdateService.class));
                lastPause = SystemClock.elapsedRealtime();
                chronometer.stop();
                chronometer.setEnabled(false);
                chronometer.setEnabled(true);
                start.setEnabled(true);
                stop.setEnabled(false);
                break;
        }
    }


    private void saveRoad(Location location) {
        mWalkedList.add(location);
        drawRoute(mWalkedList);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        enableMyLocation();

    }

    private void startLocationService() {
        startService(new Intent(this, LocationUpdateService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        chekLocationService();
    }

    private void chekLocationService() {
        if (PermissionUtils.isLocationServicesEnabled(this)) {
            enableMyLocation();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstants.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length == 0) {
                enableMyLocation();
            }
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }

    }

    private void enableMyLocation() {
        if (PermissionUtils.checkLocationPermission(this) && mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setMaxZoomPreference(18);
            mGoogleMap.setMinZoomPreference(15);
        }
    }

    private void drawRoute(ArrayList<Location> routeList) {
        ArrayList<LatLng> latLngs = new ArrayList<>();
        for (Location location : routeList) {
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        PolylineOptions options = DirectionConverter.createPolyline(this, latLngs
                , 5, Color.parseColor("#303F9F"));
        mGoogleMap.addPolyline(options);
    }

    private void countDistance() {
        double myDistance = 0;
        for (int i = 0; i < mWalkedList.size() - 1; i++) {
            myDistance += mWalkedList.get(i).distanceTo(mWalkedList.get(i + 1));
        }
        Log.d("5454545", "GET_DISTANCE: " + myDistance);
        distance.setText(String.format("Distance: %.2f", myDistance / 1000));
    }


}
