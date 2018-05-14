package com.example.kseniya.projectservicetrackinglocation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private LatLng mStartLocation = new LatLng(46, 73);
    private LatLng endLocation = new LatLng(42.86, 74.60);
    float fraction;
    private ArrayList<LatLng> mRouteList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    private LatLng startLocation;
    Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMap();
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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon));
        mMarker = mGoogleMap.addMarker(options);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        enableMyLocation();
        getRouteList();

    }

    private void startLocationService() {
        startService(new Intent(this, LocationServices.class));
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
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }

    }

    private void enableMyLocation() {
        if (PermissionUtils.checkLocationPermission(this) && mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
            startLocationService();

        }
    }
    private void drawRoute(ArrayList<LatLng> routeList) {
        PolylineOptions options = DirectionConverter.createPolyline(this, routeList
                , 5, Color.parseColor("#303F9F"));
        mGoogleMap.addPolyline(options);
        for (int i = 0; i < 2; i++) {
            MarkerOptions mMarkerOptions = new MarkerOptions()
                    .position(i == 0 ? mStartLocation : endLocation)
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            i == 0 ? BitmapDescriptorFactory.HUE_GREEN :
                                    BitmapDescriptorFactory.HUE_RED
                    ));
            mGoogleMap.addMarker(mMarkerOptions);


        }
    }

    private void getRouteList() {
        GoogleDirection.withServerKey(getString(R.string.google_api_key))
                .from(startLocation)
                .to(endLocation)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()) {
                            mRouteList = direction
                                    .getRouteList()
                                    .get(0)
                                    .getLegList()
                                    .get(0)
                                    .getDirectionPoint();
                            drawRoute(mRouteList);

                        } else {
                            AndroidUtils.showShortTost(MainActivity.this, "direction not ok" + direction.getStatus());
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        AndroidUtils.showShortTost(MainActivity.this, t.getMessage());

                    }
                });
    }
}