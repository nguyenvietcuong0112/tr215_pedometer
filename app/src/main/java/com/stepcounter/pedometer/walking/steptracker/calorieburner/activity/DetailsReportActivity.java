package com.stepcounter.pedometer.walking.steptracker.calorieburner.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;
import com.stepcounter.pedometer.walking.steptracker.calorieburner.model.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailsReportActivity extends AppCompatActivity {

    private TextView stepsText, kcalText, timeText, distanceText;
    private LineChart chart;
    private DatabaseHelper databaseHelper;

    private Spinner monthSpinner;
    private Spinner statisticSpinner;

    private static final int STATISTIC_STEPS = 0;
    private static final int STATISTIC_DISTANCE = 1;
    private static final int STATISTIC_CALORIES = 2;

    private int currentStatisticType = STATISTIC_STEPS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_report);

        databaseHelper = new DatabaseHelper(this);

        initializeViews();
        setupBackButton();
        setupSpinners();
        loadMonthlyData();


        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH); // 0 for January, 1 for February, etc.
        monthSpinner.setSelection(currentMonth);

        updateMonthlyDataDisplay(currentMonth);
        setupChart();
    }

    private void initializeViews() {
        stepsText = findViewById(R.id.stepsText);
        kcalText = findViewById(R.id.kcalText);
        timeText = findViewById(R.id.timeText);
        distanceText = findViewById(R.id.distanceText);
        chart = findViewById(R.id.chart);

        monthSpinner = findViewById(R.id.monthSpinner);
        statisticSpinner = findViewById(R.id.statisticSpinner);

    }

    private void setupSpinners() {
        // Setup statistic spinner
        ArrayAdapter<CharSequence> statisticAdapter = ArrayAdapter.createFromResource(this,
                R.array.statistic_options, android.R.layout.simple_spinner_item);
        statisticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statisticSpinner.setAdapter(statisticAdapter);

        statisticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentStatisticType = position;
                setupChart(); // Update chart based on selected statistic type
//                updateMonthlyDataDisplay(monthSpinner.getSelectedItemPosition());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Setup month spinner
        ArrayAdapter<CharSequence> monthAdapter = ArrayAdapter.createFromResource(this,
                R.array.month_options, android.R.layout.simple_spinner_item);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setupChart(); // Update chart based on selected month
                updateMonthlyDataDisplay(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void loadMonthlyData() {
        DatabaseHelper.MonthlyStepData monthlyData = databaseHelper.getMonthlyStepData();
        stepsText.setText(getString(R.string.monthly_steps_format, monthlyData.totalSteps));
        kcalText.setText(getString(R.string.monthly_kcal_format, monthlyData.totalCalories));
        timeText.setText(getString(R.string.monthly_time_format, formatTime(monthlyData.totalTime)));
        distanceText.setText(getString(R.string.monthly_distance_format, monthlyData.totalDistance));
    }

    private String formatTime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void setupChart() {
        List<Entry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        // Get selected month from spinner
        int selectedMonth = monthSpinner.getSelectedItemPosition(); // 0 for January, 1 for February, etc.

        // Fetch data for the selected month
        List<DatabaseHelper.DailyStepData> dailyData = databaseHelper.getMonthData(selectedMonth);

        // Create maps to easily look up data by date
        Map<String, Integer> stepsMap = new HashMap<>();
        Map<String, Float> distanceMap = new HashMap<>();
        Map<String, Float> caloriesMap = new HashMap<>();

        for (DatabaseHelper.DailyStepData data : dailyData) {
            stepsMap.put(data.date, data.steps);
            distanceMap.put(data.date, data.distance);
            caloriesMap.put(data.date, data.calories);
        }

        // Generate entries for the selected month
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        cal.set(currentYear, selectedMonth, 1);
        int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int todayIndex = -1;
        for (int i = 0; i < maxDay; i++) {
            String date = sdf.format(cal.getTime());

            // Get value based on selected statistic type
            float value = 0f;
            switch (currentStatisticType) {
                case STATISTIC_STEPS:
                    value = stepsMap.getOrDefault(date, 0);
                    break;
                case STATISTIC_DISTANCE:
                    value = distanceMap.getOrDefault(date, 0f);
                    break;
                case STATISTIC_CALORIES:
                    value = caloriesMap.getOrDefault(date, 0f);
                    break;
            }

            entries.add(new Entry(i, value));
            labels.add(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));

            if (cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)) {
                todayIndex = i; // Save the index of today's date
            }
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Get title for the data series based on selected statistic type
        String datasetLabel;
        int chartColor;
        switch (currentStatisticType) {
            case STATISTIC_DISTANCE:
                datasetLabel = "Daily Distance (km)";
                chartColor = getResources().getColor(R.color.colorAccent); // Use a different color for distance
                break;
            case STATISTIC_CALORIES:
                datasetLabel = "Daily Calories (kcal)";
                chartColor = Color.RED; // Red for calories
                break;
            case STATISTIC_STEPS:
            default:
                datasetLabel = "Daily Steps";
                chartColor = getResources().getColor(R.color.colorPrimary); // Default color for steps
                break;
        }

        LineDataSet dataSet = new LineDataSet(entries, datasetLabel);
        dataSet.setColor(chartColor);
        dataSet.setCircleColor(chartColor);
        dataSet.setDrawCircles(true);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2f);
        dataSet.setMode(LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(0);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);
        leftAxis.setGridLineWidth(0.5f);
        leftAxis.setDrawAxisLine(false);

        // Set appropriate Y-axis maximum based on the data type
        float maxYValue = 50f; // Default
        for (Entry entry : entries) {
            if (entry.getY() > maxYValue) {
                maxYValue = entry.getY();
            }
        }
        // Add 20% margin to the maximum value for better visualization
        leftAxis.setAxisMaximum(maxYValue * 1.2f);

        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true); // Enable legend to show the dataset label
        chart.getDescription().setEnabled(false);

        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setVisibleXRangeMaximum(7); // Show 7 days at a time

        chart.setBackgroundColor(Color.WHITE);
        chart.setDrawGridBackground(false);

        // Center today's data if we're viewing the current month
        if (todayIndex != -1 && selectedMonth == Calendar.getInstance().get(Calendar.MONTH)) {
            chart.moveViewToX(todayIndex - 3); // Adjust to center today
        } else {
            // If not current month, start from the beginning
            chart.moveViewToX(0);
        }

        chart.invalidate();
    }

    private void updateMonthlyDataDisplay(int month) {
        // Use existing method in DatabaseHelper
        DatabaseHelper.MonthlyStepData monthlyData = databaseHelper.getMonthlyStatData(month, currentStatisticType);
        stepsText.setText(getString(R.string.monthly_steps_format, monthlyData.totalSteps));
        kcalText.setText(getString(R.string.monthly_kcal_format, monthlyData.totalCalories));
        timeText.setText(getString(R.string.monthly_time_format, formatTime(monthlyData.totalTime)));
        distanceText.setText(getString(R.string.monthly_distance_format, monthlyData.totalDistance));
    }
}