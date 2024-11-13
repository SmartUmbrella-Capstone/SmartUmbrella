package com.example.smartumbrella;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingActivity extends AppCompatActivity {

    private CheckBox checkBoxUpdateAlert, checkBoxPriority, checkBoxLocationLog;
    private SeekBar seekBarDistance, seekBarVolume;
    private TextView textViewDistance, textViewVolume;
    private Spinner spinnerInterval;
    private Button buttonSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new DatabaseHelper(this);
        initializeUI();
        loadSettingsFromDatabase();

        // Save button listener to save settings to SQLite and close activity
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettingsToDatabase();
                Toast.makeText(SettingActivity.this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void initializeUI() {
        checkBoxUpdateAlert = findViewById(R.id.checkBoxUpdateAlert);
        checkBoxPriority = findViewById(R.id.checkBoxPriority);
        checkBoxLocationLog = findViewById(R.id.checkBoxLocationLog);
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
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewVolume.setText("알림 음량: " + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void loadSettingsFromDatabase() {
        Cursor cursor = dbHelper.getSettings();
        if (cursor.moveToFirst()) {
            checkBoxUpdateAlert.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UPDATE_ALERT)) == 1);
            checkBoxPriority.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRIORITY_ALERT)) == 1);
            checkBoxLocationLog.setChecked(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION_LOG)) == 1);
            seekBarDistance.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DISTANCE)));
            seekBarVolume.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_VOLUME)));
            String interval = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INTERVAL));
            spinnerInterval.setSelection(((ArrayAdapter<String>) spinnerInterval.getAdapter()).getPosition(interval));
        }
        cursor.close();
    }

    private void saveSettingsToDatabase() {
        boolean updateAlert = checkBoxUpdateAlert.isChecked();
        boolean priorityAlert = checkBoxPriority.isChecked();
        boolean locationLog = checkBoxLocationLog.isChecked();
        int distance = seekBarDistance.getProgress();
        int volume = seekBarVolume.getProgress();
        String interval = spinnerInterval.getSelectedItem().toString();
        dbHelper.saveSettings(updateAlert, priorityAlert, locationLog, distance, volume, interval);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
