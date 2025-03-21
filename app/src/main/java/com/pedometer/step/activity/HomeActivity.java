package com.pedometer.step.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.pedometer.step.R;
import com.pedometer.step.model.DatabaseHelper;

import java.util.Calendar;

public class HomeActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45;

    private static final int DEFAULT_GOAL = 6000;
    private static final double KCAL_PER_STEP = 0.04;
    private static final double KM_PER_STEP = 0.0008;
    private static final float STEP_THRESHOLD = 5.0f;
    private static final int STEP_DELAY_MS = 250;
    private static final int PEAK_COUNT = 4;
    private static final float DIRECTION_THRESHOLD = 1.0f;

    private TextView stepCountText, targetText, remainingText;
    private TextView kcalText, timeText, distanceText;
    private Button startStopButton;
    private ProgressBar progressBar;
    private ImageButton settingsButton;
    private TextView settingDailyStep;

    private boolean isTracking = false;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private int stepCount = 0;
    private long startTime;
    private long elapsedTime = 0;
    private DatabaseHelper databaseHelper;
    private Handler handler = new Handler();
    private Runnable timeUpdater;

    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    private long lastStepTime = 0;

    private float[] lastValues = new float[PEAK_COUNT];
    private int valueIndex = 0;
    private int stepGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        initializeViews();
        setupSensor();
        setupClickListeners();
        loadTodayData();
        setupTimeUpdater();
        checkAndRequestPermissions();
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
        settingDailyStep = findViewById(R.id.settingsDailyStep);

        stepGoal = getStepGoalForToday();
        progressBar.setMax(stepGoal);
        targetText.setText(getString(R.string.target_steps_format, stepGoal));
        databaseHelper = new DatabaseHelper(this);
    }

    private int getStepGoalForToday() {
        Calendar calendar = Calendar.getInstance();
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String today = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        SharedPreferences prefs = getSharedPreferences("StepGoals", MODE_PRIVATE);
        return prefs.getInt(today, DEFAULT_GOAL);
    }

    private void setupSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                showSensorNotAvailableDialog();
            }
        } else {
            showSensorNotAvailableDialog();
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

        settingDailyStep.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StepGoalActivity.class);
            startActivity(intent);
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
        if (accelerometer != null) {
            startTime = System.currentTimeMillis() - elapsedTime;
            sensorManager.registerListener(stepListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            handler.post(timeUpdater);
        } else {
            showSensorNotAvailableDialog();
        }
    }

    private void stopStepTracking() {
        if (accelerometer != null) {
            sensorManager.unregisterListener(stepListener);
            handler.removeCallbacks(timeUpdater);
            saveCurrentData();
        }
    }

    private final SensorEventListener stepListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                detectStep(event);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };

    private void detectStep(SensorEvent event) {
        final float alpha = 0.8f;

        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        float acceleration = (float) Math.sqrt(
                linear_acceleration[0] * linear_acceleration[0] +
                        linear_acceleration[1] * linear_acceleration[1] +
                        linear_acceleration[2] * linear_acceleration[2]
        );

        lastValues[valueIndex] = acceleration;
        valueIndex = (valueIndex + 1) % PEAK_COUNT;

        long currentTime = System.currentTimeMillis();

        if (isStepPattern() && (currentTime - lastStepTime) > STEP_DELAY_MS) {
            float verticalRatio = Math.abs(linear_acceleration[1]) /
                    (Math.abs(linear_acceleration[0]) + Math.abs(linear_acceleration[2]) + 0.1f);

            if (verticalRatio > DIRECTION_THRESHOLD) {
                stepCount++;
                lastStepTime = currentTime;
                updateUI();
            }
        }
    }

    private boolean isStepPattern() {
        if (valueIndex < 3) return false;

        float sum = 0;
        for (float value : lastValues) {
            sum += value;
        }
        float avg = sum / PEAK_COUNT;

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;

        for (float value : lastValues) {
            if (value > max) max = value;
            if (value < min) min = value;
        }

        float amplitude = max - min;

        return amplitude > STEP_THRESHOLD;
    }

    private void updateUI() {
        runOnUiThread(() -> {
            stepCountText.setText(String.valueOf(stepCount));
            progressBar.setProgress(stepCount);

            int remainingSteps = Math.max(0, stepGoal - stepCount);
            remainingText.setText(getString(R.string.remaining_steps_format, remainingSteps));

            double kcal = stepCount * KCAL_PER_STEP;
            double distance = stepCount * KM_PER_STEP;

            kcalText.setText(String.format("%.2f", kcal));
            distanceText.setText(String.format("%.2f", distance));

            updateTimeDisplay();
        });
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
        stepGoal = getStepGoalForToday();
        progressBar.setMax(stepGoal);
        targetText.setText(getString(R.string.target_steps_format, stepGoal));
        updateUI();
    }

    private void saveCurrentData() {
        databaseHelper.saveStepData(
                stepCount,
                stepGoal,
                stepCount * KCAL_PER_STEP,
                stepCount * KM_PER_STEP,
                elapsedTime
        );
    }

    private void showSensorNotAvailableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sensor Not Available")
                .setMessage("Accelerometer is not available on this device.")
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

    private void checkAndRequestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        PERMISSION_REQUEST_ACTIVITY_RECOGNITION);
            } else {
                setupSensor();
            }
        } else {
            setupSensor();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupSensor();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền theo dõi hoạt động để đếm bước chân",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}