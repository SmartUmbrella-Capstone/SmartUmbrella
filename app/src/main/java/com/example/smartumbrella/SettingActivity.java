package com.example.smartumbrella;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingActivity extends AppCompatActivity {

    private CheckBox checkBoxUpdateAlert, checkBoxPriority, checkBoxLocationEnable;
    private SeekBar seekBarDistance, seekBarVolume;
    private TextView textViewDistance, textViewVolume;
    private Spinner spinnerInterval;
    private Button buttonSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize DatabaseHelper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        initializeUI();

        // Load settings into UI
        getSettings();

        // Save button listener to save settings to SQLite and close activity
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                finish();
            }
        });
    }

    /**
     * Initialize UI components and set listeners
     */
    private void initializeUI() {
        checkBoxUpdateAlert = findViewById(R.id.checkBoxUpdateAlert);
        checkBoxPriority = findViewById(R.id.checkBoxPriority);
        checkBoxLocationEnable = findViewById(R.id.checkBoxLocationEnable);
        seekBarDistance = findViewById(R.id.seekBarDistance);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        textViewDistance = findViewById(R.id.textViewDistance);
        textViewVolume = findViewById(R.id.textViewVolume);
        spinnerInterval = findViewById(R.id.spinnerInterval);
        buttonSave = findViewById(R.id.buttonSave);

        // Initialize SeekBars
        seekBarDistance.setMax(30);
        seekBarVolume.setMax(100);

        // Set Spinner adapter for intervals
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.alarm_intervals, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterval.setAdapter(adapter);

        // SeekBar listeners
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewDistance.setText("알림 거리: " + progress + "m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewVolume.setText("알림 음량: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Load settings from database and apply to UI components
     */
    private void getSettings() {
        Cursor cursor = dbHelper.getUserSettings(); // Use DatabaseHelper to fetch settings
        if (cursor.moveToFirst()) {
            checkBoxUpdateAlert.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow("update_alert")) == 1);
            checkBoxPriority.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow("priority_alert")) == 1);
            checkBoxLocationEnable.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow("location_enable")) == 1);
            seekBarDistance.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow("distance")));
            seekBarVolume.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow("volume")));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow("interval"));
            spinnerInterval.setSelection(((ArrayAdapter<String>) spinnerInterval.getAdapter()).getPosition(interval));
        }
        cursor.close();
    }

    /**
     * Save settings from UI components into database
     */
    private void saveSettings() {
        ContentValues values = new ContentValues();
        values.put("update_alert", checkBoxUpdateAlert.isChecked() ? 1 : 0);
        values.put("priority_alert", checkBoxPriority.isChecked() ? 1 : 0);
        values.put("location_enable", checkBoxLocationEnable.isChecked() ? 1 : 0);
        values.put("distance", seekBarDistance.getProgress());
        values.put("volume", seekBarVolume.getProgress());
        values.put("interval", spinnerInterval.getSelectedItem().toString());

        dbHelper.saveUserSettings(values); // Use DatabaseHelper to save settings

        Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
