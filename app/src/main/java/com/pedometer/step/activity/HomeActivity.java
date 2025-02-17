package com.pedometer.step.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.pedometer.step.R;
import com.pedometer.step.model.DatabaseHelper;

public class HomeActivity extends AppCompatActivity {
    private static final int DEFAULT_GOAL = 6000;
    private static final double KCAL_PER_STEP = 0.04;
    private static final double KM_PER_STEP = 0.0008;

    private TextView stepCountText, targetText, remainingText;
    private TextView kcalText, timeText, distanceText;
    private Button startStopButton;
    private ProgressBar progressBar;
    private ImageButton settingsButton;

    private boolean isTracking = false;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int stepCount = 0;
    private long startTime;
    private long elapsedTime = 0;
    private DatabaseHelper databaseHelper;
    private Handler handler = new Handler();
    private Runnable timeUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initializeViews();
        setupSensor();
        setupClickListeners();
        loadTodayData();
        setupTimeUpdater();
    }

    private void initializeViews() {
        stepCountText = findViewById(R.id.stepCountText);
        targetText = findViewById(R.id.targetText);
        remainingText = findViewById(R.id.remainingText);
        kcalText = findViewById(R.id.kcalText);
        timeText = findViewById(R.id.timeText);
        distanceText = findViewById(R.id.distanceText);
        startStopButton = findViewById(R.id.startStopButton);
        progressBar = findViewById(R.id.progressBar);
        settingsButton = findViewById(R.id.settingsButton);

        progressBar.setMax(DEFAULT_GOAL);

        databaseHelper = new DatabaseHelper(this);
    }

    private void setupSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
            if (stepSensor == null) {
                showSensorNotAvailableDialog();
            }
        }
    }

    private void setupClickListeners() {
        startStopButton.setOnClickListener(v -> {
            isTracking = !isTracking;
            if (isTracking) {
                startStepTracking();
                startStopButton.setText(R.string.stop);
            } else {
                stopStepTracking();
                startStopButton.setText(R.string.continue_text);
            }
        });

        settingsButton.setOnClickListener(v -> {
            // TODO: Implement settings
        });
    }

    private void setupTimeUpdater() {
        timeUpdater = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    elapsedTime = System.currentTimeMillis() - startTime;
                    updateTimeDisplay();
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    private void startStepTracking() {
        if (stepSensor != null) {
            startTime = System.currentTimeMillis() - elapsedTime;
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL);
            handler.post(timeUpdater);
        }
    }

    private void stopStepTracking() {
        if (stepSensor != null) {
            sensorManager.unregisterListener(stepListener);
            handler.removeCallbacks(timeUpdater);
            saveCurrentData();
        }
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            stepCount++;
            updateUI();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void updateUI() {
        stepCountText.setText(String.valueOf(stepCount));
        progressBar.setProgress(stepCount);

        targetText.setText(getString(R.string.steps_goal_format, stepCount, DEFAULT_GOAL));

        int remainingSteps = DEFAULT_GOAL - stepCount;
        remainingText.setText(getString(R.string.remaining_steps_format,
                Math.max(0, remainingSteps)));

        double kcal = stepCount * KCAL_PER_STEP;
        double distance = stepCount * KM_PER_STEP;

        kcalText.setText(String.format("%.2f", kcal));
        distanceText.setText(String.format("%.2f", distance));

        updateTimeDisplay();
    }

    private void updateTimeDisplay() {
        timeText.setText(String.format("%02d:%02d:%02d",
                elapsedTime / 3600000,
                (elapsedTime / 60000) % 60,
                (elapsedTime / 1000) % 60));
    }

    private void loadTodayData() {
        DatabaseHelper.StepData todayData = databaseHelper.getTodayStepData();
        stepCount = todayData.steps;
        elapsedTime = todayData.time;
        updateUI();
    }

    private void saveCurrentData() {
        databaseHelper.saveStepData(
                stepCount,
                DEFAULT_GOAL,
                stepCount * KCAL_PER_STEP,
                stepCount * KM_PER_STEP,
                elapsedTime
        );
    }

    private void showSensorNotAvailableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sensor Not Available")
                .setMessage("Step sensor is not available on this device.")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTracking) {
            stopStepTracking();
        }
        saveCurrentData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTodayData();
        if (isTracking) {
            startStepTracking();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(timeUpdater);
    }
}