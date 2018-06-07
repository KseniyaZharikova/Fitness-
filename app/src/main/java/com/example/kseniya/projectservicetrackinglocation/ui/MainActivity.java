package com.example.kseniya.projectservicetrackinglocation.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
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
import com.example.kseniya.projectservicetrackinglocation.CoordinateModel;
import com.example.kseniya.projectservicetrackinglocation.LocationUpdateService;
import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.bluetooth.BluetoothActivity;
import com.example.kseniya.projectservicetrackinglocation.bluetooth.DataHandler;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;

import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;
import java.util.UUID;

import io.realm.Realm;
import okhttp3.internal.Util;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener ,Observer {
    private ArrayList<CoordinateModel> mWalkedList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    Marker mMarker;
    TextView distance, pulse,avg;
    private long lastPause;
    Chronometer chronometer;
    Button start, reset, stop, save;
    private Realm mRealm;
    double myDistance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolBar = findViewById(R.id.myToolBar);
        setSupportActionBar(myToolBar);
        mRealm = Realm.getDefaultInstance();
        distance = findViewById(R.id.distance);
        pulse = findViewById(R.id.pulse);
        avg = findViewById(R.id.avg);
        chronometer = findViewById(R.id.chronometer);
        start = findViewById(R.id.start);
        reset = findViewById(R.id.reset);
        save = findViewById(R.id.save);
        save.setEnabled(false);
        stop = findViewById(R.id.stop);
        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        save.setOnClickListener(this);
        reset.setOnClickListener(this);
        initMap();
        avg.setText("Sensor");
        avg.setOnClickListener(this);

        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.run_image)
                .build();
        new DrawerBuilder().withActivity(this).build();
        final PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("My result")
                .withSelectedColor(getResources().getColor(R.color.material_drawer_dark_background))
                .withTextColor(getResources().getColor(R.color.white))
                .withSelectedTextColor(getResources().getColor(R.color.white));
        new DrawerBuilder()
                .withActivity(this)
                .withSliderBackgroundColor(getResources().getColor(R.color.material_drawer_dark_background))
                .withToolbar(myToolBar)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch ((int) drawerItem.getIdentifier()) {
                            case 1:
                                Intent resultActivity = new Intent(MainActivity.this, ResultActivity.class);
                                startActivity(resultActivity);
                                break;
                        }

                        return false;
                    }
                })
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
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
    public void update(Observable observable, Object o) {
        receiveData();
    }
    public void receiveData() {
        runOnUiThread(new Runnable() {
            public void run() {
                pulse.setText(DataHandler.getInstance().getLastValue());
                avg.setText(DataHandler.getInstance().getMin() + "   "+
                        DataHandler.getInstance().getAvg() + "   "+
                        DataHandler.getInstance().getMax());
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                start.setText("start");
                save.setEnabled(false);
                mService.startLocationUpdates();
                DataHandler.getInstance().addObserver(this);
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
            case R.id.reset:
                mService.stopLocationUpdates();
                save.setEnabled(false);
                start.setText("start");
                myDistance = 0;
                mWalkedList.clear();
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                lastPause = 0;
                distance.setText("Distance:  0,00");
                start.setEnabled(true);
                stop.setEnabled(false);
                break;
            case R.id.avg:
                Intent bluetoothActivity = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(bluetoothActivity);
                break;
            case R.id.stop:
                mService.stopLocationUpdates();
                lastPause = SystemClock.elapsedRealtime();
                chronometer.stop();
                mWalkedList.get(mWalkedList.size() - 1).setStop(true);

                chronometer.setEnabled(false);
                chronometer.setEnabled(true);
                start.setEnabled(true);
                stop.setEnabled(false);
                save.setEnabled(true);
                start.setText("continue");
                break;
            case R.id.save:
                int nextId;
                if (Realm.getDefaultInstance().where(InformationModel.class).max("id") != null) {
                    Number id = Realm.getDefaultInstance().where(InformationModel.class).max("id");
                    nextId = id.intValue() + 1;
                } else {
                    nextId = 0;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
                String currentDateTime = sdf.format(new Date());
                mRealm.beginTransaction();
                InformationModel model = mRealm.createObject(InformationModel.class, nextId);
                Log.d("id is", "onClick: " + nextId);
                model.setDistance(distance.getText().toString());
                model.setTime(chronometer.getText().toString());
                model.setCurrentTimeDate(currentDateTime.toString());
                model.setRate(avg.getText().toString());

                start.setText("start");
                mRealm.commitTransaction();
                mService.stopLocationUpdates();
                Toast.makeText(this, "Save results ", Toast.LENGTH_LONG).show();
                save.setEnabled(false);
                mService.stopLocationUpdates();
                myDistance = 0;
                mWalkedList.clear();
                chronometer.stop();
                chronometer.setBase(SystemClock.elapsedRealtime());
                lastPause = 0;
                distance.setText("Distance:  0,00");
                break;
        }

    }

    private void saveRoad(Location location) {
        mWalkedList.add((new CoordinateModel(location)));
        drawRoute(mWalkedList);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        startLocationService();
        enableMyLocation();


    }

    LocationUpdateService mService;

    private void startLocationService() {

        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((LocationUpdateService.LocalBinder) iBinder).getService();
                mService.getLastLocation(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng
                                (task.getResult().getLatitude(), task.getResult().getLongitude())));
                    }
                });

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mService = null;
            }

        };
        bindService(new Intent(this, LocationUpdateService.class), serviceConnection, BIND_AUTO_CREATE);

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
            if (grantResults == null) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                    enableMyLocation();
                }
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

    private void drawRoute(ArrayList<CoordinateModel> routeList) {
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
            if (!mWalkedList.get(i).isStop()) {
                myDistance += mWalkedList.get(i).distanceTo(mWalkedList.get(i + 1));
            }
        }
        Log.d("5454545", "GET_DISTANCE: " + myDistance / 1000);
        distance.setText(String.format("Distance:   %.2f", myDistance / 1000));
    }



    protected void onDestroy() {
        super.onDestroy();
        DataHandler.getInstance().deleteObserver(this);
    }
}
