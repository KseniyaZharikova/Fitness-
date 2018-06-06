package com.example.kseniya.projectservicetrackinglocation.ui;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kseniya.projectservicetrackinglocation.R;
import com.example.kseniya.projectservicetrackinglocation.models.InformationModel;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {
    RealmResults<InformationModel> mResults= Realm.getDefaultInstance().where(InformationModel.class).findAll();
    List<InformationModel> items;
    CallBackRealm mCallBackRealm;

    public ResultsAdapter(List<InformationModel> items, CallBackRealm callBackRealm) {
        this.items = items;
        this.mCallBackRealm =  callBackRealm;
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
        holder.tvDate.setText(model.getCurrentTimeDate());
        holder.btnDelete.setTag(model.getId());
        holder.tvPulse.setText(String.valueOf(model.getRate()));
        Log.d("po", "onBindViewHolder: " + model.getId());


    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDistance,tvTime,tvDate,tvPulse;
        Button btnDelete;


        public ViewHolder(final View itemView) {
            super(itemView);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPulse = itemView.findViewById(R.id.tvPulse);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnDelete.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Log.d("pos", "onLongClick: " + view.getTag());
                    mCallBackRealm.deleteRealmObject(items, (int)view.getTag());
                    return false;
                }
            });
        }
    }

}
