package com.stepcounter.pedometer.walking.steptracker.calorieburner.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.stepcounter.pedometer.walking.steptracker.calorieburner.R;

public class StepGoalActivity extends AppCompatActivity {

    private EditText mondayGoal, tuesdayGoal, wednesdayGoal, thursdayGoal, fridayGoal, saturdayGoal, sundayGoal;
    private Button saveButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_goal);

        prefs = getSharedPreferences("StepGoals", MODE_PRIVATE);

        initializeViews();
        loadGoals();
        setupSaveButton();
    }

    private void initializeViews() {
        mondayGoal = findViewById(R.id.mondayGoal);
        tuesdayGoal = findViewById(R.id.tuesdayGoal);
        wednesdayGoal = findViewById(R.id.wednesdayGoal);
        thursdayGoal = findViewById(R.id.thursdayGoal);
        fridayGoal = findViewById(R.id.fridayGoal);
        saturdayGoal = findViewById(R.id.saturdayGoal);
        sundayGoal = findViewById(R.id.sundayGoal);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadGoals() {
        mondayGoal.setText(String.valueOf(prefs.getInt("Monday", 6000)));
        tuesdayGoal.setText(String.valueOf(prefs.getInt("Tuesday", 6000)));
        wednesdayGoal.setText(String.valueOf(prefs.getInt("Wednesday", 6000)));
        thursdayGoal.setText(String.valueOf(prefs.getInt("Thursday", 6000)));
        fridayGoal.setText(String.valueOf(prefs.getInt("Friday", 6000)));
        saturdayGoal.setText(String.valueOf(prefs.getInt("Saturday", 6000)));
        sundayGoal.setText(String.valueOf(prefs.getInt("Sunday", 6000)));
    }

    private void setupSaveButton() {
        saveButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("Monday", Integer.parseInt(mondayGoal.getText().toString()));
            editor.putInt("Tuesday", Integer.parseInt(tuesdayGoal.getText().toString()));
            editor.putInt("Wednesday", Integer.parseInt(wednesdayGoal.getText().toString()));
            editor.putInt("Thursday", Integer.parseInt(thursdayGoal.getText().toString()));
            editor.putInt("Friday", Integer.parseInt(fridayGoal.getText().toString()));
            editor.putInt("Saturday", Integer.parseInt(saturdayGoal.getText().toString()));
            editor.putInt("Sunday", Integer.parseInt(sundayGoal.getText().toString()));
            editor.apply();

            Toast.makeText(this, "Goals saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}