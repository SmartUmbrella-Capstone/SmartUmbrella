package com.example.smartumbrella;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private BLEManager bleManager;
    private SmsReceiver smsReceiver;
    private static final int REQUEST_CODE_BLUETOOTH = 1;
    private static final int  REQUEST_CODE_SMS_PERMISSION= 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_SMS_PERMISSION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
        }

        // BLEManager 초기화
        bleManager = new BLEManager(this);
        // SmsReceiver를 초기화하고 BLEManager 전달
        smsReceiver = new SmsReceiver(bleManager);
        // BroadcastReceiver 등록 (SMS 수신)
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        registerReceiver(smsReceiver, filter);
        // Create an instance of CallReceiver
        CallReceiver callReceiver = new CallReceiver(bleManager);
        IntentFilter callFilter = new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        registerReceiver(callReceiver, callFilter);


        // 버튼 초기화
        Button buttonMap = findViewById(R.id.button1);
        Button buttonSettings = findViewById(R.id.button2);
        Button buttonStartBLE = findViewById(R.id.buttonStartBLE); // Start BLE 버튼
        Button buttonStopBLE = findViewById(R.id.buttonStopBLE); // Stop BLE 버튼

        // 지도 버튼 클릭 리스너
        buttonMap.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // 사용자 설정 버튼 클릭 리스너
        buttonSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });
//        // Start BLE 버튼 클릭 리스너
//        buttonStartBLE.setOnClickListener(v -> startBLEScan());
//
//        // Stop BLE 버튼 클릭 리스너
//        buttonStopBLE.setOnClickListener(v -> stopBLEScan());



        // BLE 권한 요청
        requestBluetoothPermissions();
    }

    // BLE 권한 요청 메서드
    private void requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 이상 권한 요청
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, REQUEST_CODE_BLUETOOTH);
        } else {
            // Android 11 이하 권한 요청
            requestPermissions(new String[]{
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_CODE_BLUETOOTH);
        }
    }

    // 권한 요청 결과 처리
    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // 모든 권한이 허용된 경우 BLE 스캔 시작
                bleManager.startScanning();

            } else {
                // 권한이 거부된 경우 사용자에게 알림
                Toast.makeText(this, "Bluetooth 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

//    // BLE 스캔 시작 메서드
//    @SuppressLint("MissingPermission")
//    private void startBLEScan() {
//        if (bleManager.haveAllPermissions()) {
//            bleManager.startScanning();
//        } else {
//            Toast.makeText(this, "필수 권한이 없습니다.", Toast.LENGTH_SHORT).show();
//        }
//    }
//    // BLE 스캔 중지 메서드
//    private void stopBLEScan() {
//        bleManager.disconnect(); // Assuming stopScanning method exists in BLEManager class
//        Toast.makeText(this, "BLE 스캔이 중지되었습니다.", Toast.LENGTH_SHORT).show();
//    }


}
