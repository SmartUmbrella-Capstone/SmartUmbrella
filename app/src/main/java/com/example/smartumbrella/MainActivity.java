package com.example.smartumbrella;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private BLEManager bleManager;
    private static final int REQUEST_CODE_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BLEManager 초기화
        bleManager = new BLEManager(this);

        // 버튼 초기화
        Button buttonMap = findViewById(R.id.button1);
        Button buttonSettings = findViewById(R.id.button2);
        Button buttonCancel = findViewById(R.id.buttonCancel); // 연결 취소 버튼

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

        // 연결 취소 버튼 클릭 리스너
        buttonCancel.setOnClickListener(v -> showDisconnectDialog()); // 연결 해제 확인 다이얼로그 표시

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
                startBLEScan();
            } else {
                // 권한이 거부된 경우 사용자에게 알림
                Toast.makeText(this, "Bluetooth 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // BLE 스캔 시작 메서드
    @SuppressLint("MissingPermission")
    private void startBLEScan() {
        if (bleManager.haveAllPermissions()) {
            bleManager.startScanning();
        } else {
            Toast.makeText(this, "필수 권한이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // 연결 해제 확인 다이얼로그
    private void showDisconnectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("연결 해제")
                .setMessage("정말 연결을 해제하시겠습니까?")
                .setPositiveButton("예", (dialog, which) -> {
                    // SubMainActivity로 이동
                    Intent intent = new Intent(MainActivity.this, SubMainActivity.class);
                    startActivity(intent);
                    finish(); // MainActivity 종료
                })
                .setNegativeButton("아니오", null) // 액션 없이 다이얼로그 닫기
                .show();
    }
}
