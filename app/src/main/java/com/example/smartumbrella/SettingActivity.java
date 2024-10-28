package com.example.smartumbrella;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import android.widget.ArrayAdapter;

public class SettingActivity extends AppCompatActivity {

    private CheckBox checkBoxDarkMode;
    private CheckBox checkBoxUpdateAlert;
    private CheckBox checkBoxPriority; // 우산 찾기 알림 체크박스
    private CheckBox checkBoxLocationLog; // 위치 기록 체크박스
    private SeekBar seekBarDistance; // 거리 설정 SeekBar
    private SeekBar seekBarVolume; // 음량 설정 SeekBar
    private TextView textViewDistance; // 거리 설정 텍스트 뷰
    private TextView textViewVolume; // 음량 설정 텍스트 뷰
    private SharedPreferences sharedPreferences;
    private Spinner spinnerInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼 활성화

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("UserSettings", MODE_PRIVATE);

        checkBoxDarkMode = findViewById(R.id.checkBoxDarkMode);
        checkBoxUpdateAlert = findViewById(R.id.checkBoxUpdateAlert);
        checkBoxPriority = findViewById(R.id.checkBoxPriority); // 우산 찾기 알림 체크박스 초기화
        checkBoxLocationLog = findViewById(R.id.checkBoxLocationLog); // 위치 기록 체크박스 초기화
        spinnerInterval = findViewById(R.id.spinnerInterval);

        // SeekBar 초기화
        seekBarDistance = findViewById(R.id.seekBarDistance);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        textViewDistance = findViewById(R.id.textViewDistance); // 거리 설정 텍스트 뷰
        textViewVolume = findViewById(R.id.textViewVolume); // 음량 설정 텍스트 뷰

        // 다크 모드 설정 불러오기
        boolean isDarkModeEnabled = sharedPreferences.getBoolean("DarkMode", false);
        checkBoxDarkMode.setChecked(isDarkModeEnabled);
        applyDarkMode(isDarkModeEnabled);

        // 업데이트 알림 상태 불러오기
        boolean isUpdateAlertEnabled = sharedPreferences.getBoolean("UpdateAlert", false);
        checkBoxUpdateAlert.setChecked(isUpdateAlertEnabled);

        // 우산 찾기 알림 상태 불러오기
        boolean isPriorityEnabled = sharedPreferences.getBoolean("PriorityAlert", false);
        checkBoxPriority.setChecked(isPriorityEnabled);

        // 위치 기록 상태 불러오기
        boolean isLocationLogEnabled = sharedPreferences.getBoolean("LocationLog", false);
        checkBoxLocationLog.setChecked(isLocationLogEnabled);

        // 거리 SeekBar 설정
        seekBarDistance.setMax(30); // 최대 30미터
        seekBarDistance.setProgress(sharedPreferences.getInt("DistanceSetting", 0)); // 초기값 설정
        textViewDistance.setText("알림 거리: " + seekBarDistance.getProgress() + "m");

        // 음량 SeekBar 설정
        seekBarVolume.setMax(100); // 최대 100
        seekBarVolume.setProgress(sharedPreferences.getInt("VolumeSetting", 50)); // 초기값 설정
        textViewVolume.setText("알림 음량: " + seekBarVolume.getProgress());

        // SeekBar 거리 변경 리스너
        seekBarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewDistance.setText("알림 거리: " + progress + "m");
                // 거리 설정 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("DistanceSetting", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // SeekBar 음량 변경 리스너
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textViewVolume.setText("알림 음량: " + progress);
                // 음량 설정 저장
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("VolumeSetting", progress);
                editor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        // Spinner에 알람 주기 설정 값 추가하기
        String[] alarmIntervals = getResources().getStringArray(R.array.alarm_intervals);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, alarmIntervals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInterval.setAdapter(adapter);

        // 체크박스 상태 변경 시 다크 모드 설정 적용
        checkBoxDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveDarkModeSetting(isChecked);
            applyDarkMode(isChecked);
        });

        // 업데이트 알림 체크박스 상태 변경 시 동작
        checkBoxUpdateAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("UpdateAlert", isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(SettingActivity.this, "업데이트 알림이 활성화되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, "업데이트 알림이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 우산 찾기 알림 체크박스 상태 변경 시 동작
        checkBoxPriority.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("PriorityAlert", isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(SettingActivity.this, "우산 찾기 알림이 활성화되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, "우산 찾기 알림이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 위치 기록 체크박스 상태 변경 시 동작
        checkBoxLocationLog.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("LocationLog", isChecked);
            editor.apply();

            if (isChecked) {
                Toast.makeText(SettingActivity.this, "위치 기록이 활성화되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingActivity.this, "위치 기록이 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDarkModeSetting(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("DarkMode", isEnabled);
        editor.apply();
    }

    private void applyDarkMode(boolean isEnabled) {
        int mode = isEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(mode);
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // 뒤로가기 버튼 클릭 시 동작
        return true;
    }
}