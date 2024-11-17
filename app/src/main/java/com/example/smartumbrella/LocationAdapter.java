package com.example.smartumbrella;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    private final List<LocationLog> locationLogs;
    private final OnItemClickListener listener;

    // 클릭 리스너 인터페이스
    public interface OnItemClickListener {
        void onItemClick(LocationLog locationLog);
    }

    // 생성자
    public LocationAdapter(List<LocationLog> locationLogs, OnItemClickListener listener) {
        this.locationLogs = locationLogs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationLog locationLog = locationLogs.get(position);
        holder.textViewTimestamp.setText(locationLog.getTimestamp());

        // 항목 클릭 이벤트 설정
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(locationLog);
            }
        });
    }

    @Override
    public int getItemCount() {
        return locationLogs.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTimestamp;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
        }
    }
}
