package com.example.smartumbrella;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingActivity extends AppCompatActivity {

    private SeekBar seekBarDistance;
    private TextView textViewDistance;
    private Button buttonSave;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

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
        seekBarDistance = findViewById(R.id.seekBarDistance);
        textViewDistance = findViewById(R.id.textViewDistance);
        buttonSave = findViewById(R.id.buttonSave);

        // Initialize SeekBars
        seekBarDistance.setMax(30);

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
    }

    /**
     * Load settings from database and apply to UI components
     */
    private void getSettings() {
        Cursor cursor = dbHelper.getUserSettings(); // Use DatabaseHelper to fetch settings
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    seekBarDistance.setProgress(cursor.getInt(cursor.getColumnIndexOrThrow("distance")));
                    // Add interval processing logic if needed
                }
            } finally {
                cursor.close(); // Ensure the cursor is closed
            }
        }
    }

    /**
     * Save settings from UI components into database
     */
    private void saveSettings() {
        ContentValues values = new ContentValues();
        values.put("distance", seekBarDistance.getProgress());

        dbHelper.saveUserSettings(values); // Use DatabaseHelper to save settings

        Toast.makeText(this, "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
