package com.example.kseniya.projectservicetrackinglocation.ui;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;


import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ResultActivity extends AppCompatActivity implements CallBackRealm {

    Realm mRealm;
    ResultsAdapter mAdapter;
    RealmResults<InformationModel> results;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_result);
        recyclerView = findViewById(R.id.recyclerView);
        results = Realm.getDefaultInstance().where(InformationModel.class).sort("id").findAll();
        recyclerView.setAdapter(mAdapter = new ResultsAdapter(results, ResultActivity.this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void deleteRealmObject(List<InformationModel> list, final int position) {
        Log.d("delete", "execute: " + position);
       // Toast.makeText( this,position + "",Toast.LENGTH_LONG).show();
        mRealm.beginTransaction();
        mRealm.getDefaultInstance().where(InformationModel.class).equalTo("id",position).findAll().deleteAllFromRealm();
        mRealm.commitTransaction();
        mAdapter.notifyDataSetChanged();
    }

}
