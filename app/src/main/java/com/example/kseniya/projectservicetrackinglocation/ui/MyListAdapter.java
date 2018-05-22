package com.example.kseniya.projectservicetrackinglocation.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> implements RealmChangeListener {

    private final RealmResults<InformationModel> model;

    public MyListAdapter(RealmResults<InformationModel> models) {
        model = models;
        model.addChangeListener(this);
    }


    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_adapter, parent, false);
        return new ViewHolder((TextView) view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTextTitle.setText( String.format("%d") +model.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return model.size();
    }

    @Override
    public void onChange(Object o) {
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTextTitle;
        
        public ViewHolder(final TextView textView) {
            super(textView);
            mTextTitle = textView;
        }
    }
}
