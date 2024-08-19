package com.jason.moment.util.db;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jason.moment.util.MyActivity;

import java.util.List;
import com.jason.moment.R;

public class MyLocAdapter extends RecyclerView.Adapter<MyLocAdapter.ViewHolder> {
    private List<MyActivity> myActivities;

    public MyLocAdapter(List<MyActivity> myActivities) {
        this.myActivities = myActivities;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_myloc, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyActivity activity = myActivities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return myActivities.size();
    }

    public void updateData(List<MyActivity> newActivities) {
        myActivities = newActivities;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView latitudeText, longitudeText, dateText, timeText;

        ViewHolder(View itemView) {
            super(itemView);
            latitudeText = itemView.findViewById(R.id.latitudeText);
            longitudeText = itemView.findViewById(R.id.longitudeText);
            dateText = itemView.findViewById(R.id.dateText);
            timeText = itemView.findViewById(R.id.timeText);
        }

        void bind(MyActivity activity) {
            latitudeText.setText(String.valueOf(activity.getLatitude()));
            longitudeText.setText(String.valueOf(activity.getLongitude()));
            dateText.setText(activity.getCrdate());
            timeText.setText(activity.getCrtime());
        }
    }
}