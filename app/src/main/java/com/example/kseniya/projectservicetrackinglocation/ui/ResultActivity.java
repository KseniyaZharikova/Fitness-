package com.example.kseniya.projectservicetrackinglocation.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;
import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;
import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class ResultActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        RecyclerView recyclerView  = findViewById(R.id.recyclerView);

        RealmResults<InformationModel> results = Realm.getDefaultInstance().where(InformationModel.class).findAll();
        recyclerView.setAdapter(new ResultsAdapter(results));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }

}
