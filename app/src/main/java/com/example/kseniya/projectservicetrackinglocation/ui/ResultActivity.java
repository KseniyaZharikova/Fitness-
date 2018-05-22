package com.example.kseniya.projectservicetrackinglocation.ui;

import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class ResultActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    private Realm mRealm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        recyclerView = findViewById(R.id.recycler_view);
        mRealm = Realm.getDefaultInstance();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
       // recyclerView.setAdapter(new MyListAdapter(mRealm.createObject(InformationModel.class)));




//        myDistance.setText(mRealm.where(InformationModel.class).findFirst().getDistance().toString());
//        myTime.setText(String.format(" %d", mRealm.where(InformationModel.class).findFirst().getTime() + SystemClock.elapsedRealtime()));

    }
}
