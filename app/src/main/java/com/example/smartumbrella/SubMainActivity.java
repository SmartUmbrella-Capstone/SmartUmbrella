package com.example.smartumbrella;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SubMainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOCATION = 1;
    private static final String TARGET_DEVICE_NAME = "ESP32_BLE_Test";  // 타겟 장치 이름
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submain);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e("BluetoothError", "Bluetooth not supported on this device.");
            Toast.makeText(this, "Bluetooth를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
            finish();  // 기기가 블루투스를 지원하지 않으면 종료
            return;
        }

        // 블루투스 권한 요청
        requestPermissionsIfNeeded();

        Button connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(view -> {
            // 블루투스가 비활성화된 경우 설정 화면으로 이동
            if (!bluetoothAdapter.isEnabled()) {
                Log.d("BluetoothStatus", "Bluetooth is disabled, opening settings.");
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
                return;
            }

            // 블루투스 설정 화면으로 이동
            Log.d("BluetoothStatus", "Opening Bluetooth settings.");
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });
        // Skip button to go directly to MainActivity
        Button skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(view -> {
            Log.d("BluetoothStatus", "Skipping Bluetooth check.");
            navigateToMainActivity();
        });

        // 앱이 시작될 때 Bluetooth 장치가 연결되어 있는지 확인
        checkBluetoothConnection();
    }

    // 위치 및 블루투스 권한 요청 메서드
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12 이상
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION);
            }
        } else {  // Android 12 미만
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_LOCATION);
            }
        }
    }

    // Bluetooth 장치 연결 상태 확인 메서드
    private void checkBluetoothConnection() {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothError", "BluetoothAdapter is null.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.e("PermissionError", "BLUETOOTH_CONNECT 권한이 필요합니다.");
            Toast.makeText(this, "블루투스 연결 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isEnabled()) {
            boolean isTargetDeviceConnected = false;

            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (TARGET_DEVICE_NAME.equals(device.getName())) {
                    isTargetDeviceConnected = true;
                    break;
                }
            }

            if (isTargetDeviceConnected) {
                Log.d("BluetoothStatus", "타겟 장치가 연결됨.");
                Toast.makeText(this, "블루투스 장치가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();
            } else {
                Log.d("BluetoothStatus", "연결된 타겟 장치 없음.");
                Toast.makeText(this, "연결된 블루투스 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.d("BluetoothStatus", "Bluetooth is disabled.");
            Toast.makeText(this, "블루투스가 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // MainActivity로 이동하는 메서드
    private void navigateToMainActivity() {
        Intent intent = new Intent(SubMainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // SubMainActivity 종료
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION) {
            checkBluetoothConnection();  // 설정 후 다시 연결 상태 확인
        }
    }
}