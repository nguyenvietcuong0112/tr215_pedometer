package com.pedometer.step.activity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pedometer.step.R;
import com.pedometer.step.util.CircularProgressBar;

public class HomeActivity extends AppCompatActivity implements SensorEventListener {
    private TextView stepsCountTextView, caloriesTextView, timeTextView, distanceTextView;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount = 0;
    private long startTime;
    private boolean isTracking = false;
    private CircularProgressBar progressBar;
    private static final int DAILY_GOAL = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initializeViews();
        setupSensor();
    }

    private void initializeViews() {
        stepsCountTextView = findViewById(R.id.stepsTextView);
        caloriesTextView = findViewById(R.id.caloriesValue);
        timeTextView = findViewById(R.id.timeValue);
        distanceTextView = findViewById(R.id.distanceValue);
        progressBar = findViewById(R.id.progressBar);

        progressBar.setMaxProgress(DAILY_GOAL);

        findViewById(R.id.startButton).setOnClickListener(v -> {
            if (!isTracking) {
                startTracking();
            }
        });
    }

    private void setupSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepSensor == null) {
                Toast.makeText(this, "No step sensor found on this device", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTracking() {
        isTracking = true;
        startTime = System.currentTimeMillis();
        stepCount = 0;
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        updateUI();
        startTimeUpdates();
    }

    private void updateUI() {
        stepsCountTextView.setText(String.valueOf(stepCount));
        progressBar.setProgress(stepCount);

        // Calculate calories (approximate)
        double calories = stepCount * 0.04;
        caloriesTextView.setText(String.format("%.2f", calories));

        // Calculate distance (approximate)
        double distance = stepCount * 0.0008; // Assuming average step length of 0.8m
        distanceTextView.setText(String.format("%.2f", distance));
    }

    private void startTimeUpdates() {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    long currentTime = System.currentTimeMillis();
                    long elapsedTime = currentTime - startTime;
                    updateTimeDisplay(elapsedTime);
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    private void updateTimeDisplay(long elapsedTimeMillis) {
        int seconds = (int) (elapsedTimeMillis / 1000) % 60;
        int minutes = (int) ((elapsedTimeMillis / (1000 * 60)) % 60);
        int hours = (int) ((elapsedTimeMillis / (1000 * 60 * 60)) % 24);
        timeTextView.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++;
            updateUI();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for step detector
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        isTracking = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && stepSensor != null && isTracking) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }
}