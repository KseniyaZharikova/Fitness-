package com.example.kseniya.projectservicetrackinglocation.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;

import java.util.List;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    List<InformationModel> items;

    public ResultsAdapter(List<InformationModel> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public  ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_one, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InformationModel model = items.get(position);
        holder.tvDistance.setText(model.getDistance());
        holder.tvTime.setText(model.getTime());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance,tvTime;

        public ViewHolder(View itemView) {
            super(itemView);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvTime = itemView.findViewById(R.id.tvTime);

        }
    }

}
