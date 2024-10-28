package com.example.smartumbrella;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;
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

        // 블루투스 권한 요청
        requestLocationPermission();

        Button connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(view -> {
            // 블루투스가 비활성화된 경우 설정 화면으로 이동
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
                return;
            }

            // 블루투스 설정 화면으로 이동
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_LOCATION);
        });

        // 앱이 시작될 때 Bluetooth 장치가 연결되어 있는지 확인
        checkBluetoothConnection();
    }

    // 위치 권한 요청 메서드
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_CODE_LOCATION);
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Bluetooth 장치 연결 상태 확인 메서드
    private void checkBluetoothConnection() {
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            boolean isTargetDeviceConnected = false;

            // 페어링된 장치 목록에서 타겟 장치 찾기
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                if (TARGET_DEVICE_NAME.equals(device.getName())) {  // 이름 비교
                    isTargetDeviceConnected = true;
                    break;
                }
            }

            if (isTargetDeviceConnected) {
                Toast.makeText(this, "블루투스 장치가 연결되었습니다.", Toast.LENGTH_SHORT).show();
                navigateToMainActivity();  // 다음 화면으로 이동
            } else {
                Toast.makeText(this, "연결된 블루투스 장치가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "블루투스가 비활성화되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // MainActivity로 이동하는 메서드
    private void navigateToMainActivity() {
        Intent intent = new Intent(SubMainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();  // SubMainActivity 종료
    }

    // 블루투스 설정 화면에서 돌아왔을 때 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_LOCATION) {
            checkBluetoothConnection();  // 설정 후 다시 연결 상태 확인
        }
    }
}
