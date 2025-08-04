package com.v2v.waveletapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private ImageButton playBtn, pauseBtn, stopBtn;
    private TextView timerText;
    private LottieAnimationView animationView;

    private MediaRecorder recorder;
    private boolean isRecording = false;
    private boolean isPaused = false;

    private Handler timerHandler = new Handler();
    private long startTime = 0L;
    private Runnable timerRunnable;

    private String outputFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        playBtn = findViewById(R.id.btnPlay);
        pauseBtn = findViewById(R.id.btnPause);
        stopBtn = findViewById(R.id.btnStop);
        timerText = findViewById(R.id.tv_timer);
        animationView = findViewById(R.id.lottieAnimationView);
        ImageButton btnHistory = findViewById(R.id.btn_history);
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, SavedRecordingsActivity.class);
                startActivity(intent);
            }
        });
        animationView.setAnimation("ic_animation_placeholder.json");

        animationView.setRepeatCount(LottieDrawable.INFINITE);
        animationView.setProgress(0f);
        animationView.pauseAnimation();

        checkPermissions();

        playBtn.setOnClickListener(v -> {
            if (!isRecording) {
                startRecording();
            } else if (isPaused) {
                resumeRecording();
            }
        });

        pauseBtn.setOnClickListener(v -> {
            if (isRecording && !isPaused) {
                pauseRecording();
            }
        });

        stopBtn.setOnClickListener(v -> {
            if (isRecording) {
                stopRecording();
            }
        });

        setupTimer();
    }

    private void setupTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                timerText.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 500);
            }
        };
    }

    private void startRecording() {
        try {
            outputFilePath = getRecordingFilePath();
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // MP4 format
            recorder.setOutputFile(outputFilePath);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.prepare();
            recorder.start();

            isRecording = true;
            isPaused = false;

            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);

            animationView.setProgress(0f);
            animationView.playAnimation();

            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void pauseRecording() {
        try {
            recorder.pause();
            isPaused = true;

            timerHandler.removeCallbacks(timerRunnable);
            animationView.pauseAnimation();

            Toast.makeText(this, "Recording paused", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Pause failed", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resumeRecording() {
        try {
            recorder.resume();
            isPaused = false;

            startTime = System.currentTimeMillis() - getElapsedTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            animationView.resumeAnimation();

            Toast.makeText(this, "Recording resumed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Resume failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;

            isRecording = false;
            isPaused = false;

            timerHandler.removeCallbacks(timerRunnable);
            animationView.pauseAnimation();
            animationView.setProgress(0f);
            timerText.setText("00:00");

            Toast.makeText(this, "Recording saved:\n" + outputFilePath, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Stop failed", Toast.LENGTH_SHORT).show();
        }
    }

    private long getElapsedTimeMillis() {
        String[] parts = timerText.getText().toString().split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return (minutes * 60L + seconds) * 1000L;
    }

    private String getRecordingFilePath() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "Recordings");
        if (!dir.exists()) dir.mkdirs();
        return dir.getAbsolutePath() + "/recording_" + timeStamp + ".mp4";
    }

    private void checkPermissions() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        boolean allGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            ActivityCompat.requestPermissions(this, permissions, 200);
        }
    }
}