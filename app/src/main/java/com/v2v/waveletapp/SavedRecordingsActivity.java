package com.v2v.waveletapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import java.io.FileOutputStream;
public class SavedRecordingsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<File> recordingList;
    private RecordingAdapter adapter;
    private MediaPlayer mediaPlayer;
    private File currentPlayingFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_recordings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recordingsRecyclerView);
        recordingList = fetchRecordings();
        adapter = new RecordingAdapter(recordingList, this::showPlaybackDialog);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void shareAudioFile(File audioFile) {
        if (audioFile.exists()) {
            Uri uri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    audioFile
            );
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("audio/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(shareIntent, "Share Recording"));
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ShareError", "Sharing failed: " + e.getMessage());
                Toast.makeText(this, "Failed to share recording.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Audio file not found!", Toast.LENGTH_LONG).show();
        }
    }

    private ArrayList<File> fetchRecordings() {
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings");
        File[] files = dir.exists() ? dir.listFiles() : new File[0];
        ArrayList<File> recordings = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName().toLowerCase();
                if (name.endsWith(".m4a") || name.endsWith(".3gp") || name.endsWith(".mp3") || name.endsWith(".mp4")) {
                    recordings.add(file);
                }
            }
        }
        Log.d("Recordings", "Found " + recordings.size() + " recordings");
        for (File f : recordings) {
            Log.d("RecordingFile", "Path: " + dir.getAbsolutePath());
        }
        return recordings;
    }

    private void showPlaybackDialog(File audioFile) {
        currentPlayingFile = audioFile;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_playing, null);

        TextView audioName = dialogView.findViewById(R.id.tvSelectedAudioName);
        LottieAnimationView animation = dialogView.findViewById(R.id.lottieAnimation);
        ImageButton btnDelete = dialogView.findViewById(R.id.btnDelete);
        ImageButton btnShare = dialogView.findViewById(R.id.btnShare);
        ImageButton btnRename = dialogView.findViewById(R.id.btnRename);

        audioName.setText(audioFile.getName());

        animation.setAnimation(R.raw.playing_animation);
        animation.playAnimation();

        mediaPlayer = MediaPlayer.create(this, Uri.fromFile(audioFile));
        mediaPlayer.start();

        btnDelete.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
            if (audioFile.delete()) {
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                recordingList.remove(audioFile);
                adapter.notifyDataSetChanged();
            }
        });

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("audio/*");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(audioFile));
            startActivity(Intent.createChooser(shareIntent, "Share Recording"));
        });
        btnShare.setOnClickListener(view -> {
            shareAudioFile(audioFile);
        });
        btnRename.setOnClickListener(v -> showRenameDialog(audioFile, audioName));
        btnShare.setOnClickListener(view ->{
            shareAudioFile(audioFile);
        });

        builder.setView(dialogView);
        builder.setOnDismissListener(dialog -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        });

        builder.show();
    }

    private void showRenameDialog(File oldFile, TextView audioNameText) {
        EditText input = new EditText(this);
        input.setHint("Enter new name");

        new AlertDialog.Builder(this)
                .setTitle("Rename Recording")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.endsWith(".mp3")) newName += ".mp3";
                    File newFile = new File(oldFile.getParent(), newName);

                    if (oldFile.renameTo(newFile)) {
                        Toast.makeText(this, "Renamed", Toast.LENGTH_SHORT).show();
                        audioNameText.setText(newName);
                        recordingList.set(recordingList.indexOf(oldFile), newFile);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Rename Failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}