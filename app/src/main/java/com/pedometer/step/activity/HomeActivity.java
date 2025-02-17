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
    private static final float STEP_THRESHOLD = 12.0f; // Tăng ngưỡng lên
    private static final int STEP_DELAY_MS = 300; // Tăng độ trễ
    private static final int PEAK_COUNT = 4; // Số mẫu để xác định đỉnh
    private static final float DIRECTION_THRESHOLD = 2.0f; // Ngưỡng thay đổi hướng

    private TextView stepCountText, targetText, remainingText;
    private TextView kcalText, timeText, distanceText;
    private Button startStopButton;
    private ProgressBar progressBar;
    private ImageButton settingsButton;

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
    private boolean isPotentialStep = false;

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
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
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
        if (accelerometer != null) {
            startTime = System.currentTimeMillis() - elapsedTime;
            sensorManager.registerListener(stepListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            handler.post(timeUpdater);
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

        // Tách trọng lực như cũ
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        // Tính gia tốc tuyến tính
        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        // Tính độ lớn gia tốc
        float acceleration = (float) Math.sqrt(
                linear_acceleration[0] * linear_acceleration[0] +
                        linear_acceleration[1] * linear_acceleration[1] +
                        linear_acceleration[2] * linear_acceleration[2]
        );

        // Lưu giá trị vào buffer
        lastValues[valueIndex] = acceleration;
        valueIndex = (valueIndex + 1) % PEAK_COUNT;

        long currentTime = System.currentTimeMillis();

        // Kiểm tra mẫu dao động
        if (isStepPattern() && (currentTime - lastStepTime) > STEP_DELAY_MS) {
            // Kiểm tra hướng chuyển động chủ yếu là theo trục dọc (y)
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
        if (valueIndex < PEAK_COUNT - 1) return false;

        float sum = 0;
        for (float value : lastValues) {
            sum += value;
        }
        float avg = sum / PEAK_COUNT;

        boolean hasPositivePeak = false;
        boolean hasNegativePeak = false;

        for (float value : lastValues) {
            if (value > avg + STEP_THRESHOLD) {
                hasPositivePeak = true;
            }
            if (value < avg - STEP_THRESHOLD/2) {
                hasNegativePeak = true;
            }
        }

        return hasPositivePeak && hasNegativePeak;
    }

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
}