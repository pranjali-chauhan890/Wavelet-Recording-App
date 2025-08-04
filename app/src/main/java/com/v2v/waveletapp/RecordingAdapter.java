package com.v2v.waveletapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {

    private ArrayList<File> recordingList;
    private OnRecordingClickListener listener;

    public interface OnRecordingClickListener {
        void onRecordingClick(File file);
    }

    public RecordingAdapter(ArrayList<File> recordingList, OnRecordingClickListener listener) {
        this.recordingList = recordingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File recording = recordingList.get(position);
        holder.recordingName.setText(recording.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecordingClick(recording);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordingName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingName = itemView.findViewById(R.id.tvAudioName);
        }
    }
}
