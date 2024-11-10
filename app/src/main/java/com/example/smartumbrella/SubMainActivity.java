package com.example.smartumbrella;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
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
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class SubMainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_BLUETOOTH = 2;
    private static final int REQUEST_CODE_BLUETOOTH_SCAN = 1;
    private static final int REQUEST_CODE_NOTIFICATION = 3;
    private static final String TARGET_DEVICE_NAME = "ESP32_BLE_Test";  // 타겟 장치 이름
    private static final int DISTANCE_THRESHOLD = -30;  // 거리 기준 (RSSI 값)
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private ScanCallback scanCallback;

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

        // 권한 확인 및 요청
        requestPermissionsIfNeeded();

        Button connectButton = findViewById(R.id.connect_button);
        connectButton.setOnClickListener(view -> {
            if (!bluetoothAdapter.isEnabled()) {
                Log.d("BluetoothStatus", "Bluetooth is disabled, opening settings.");
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
                return;
            }
            Log.d("BluetoothStatus", "Opening Bluetooth settings.");
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(intent, REQUEST_CODE_BLUETOOTH);
        });

        checkBluetoothConnection();
    }

    // Bluetooth 권한 요청 메서드
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {  // Android 12 이상
            // Bluetooth 권한 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_CODE_BLUETOOTH);
            }
        } else {  // Android 12 미만
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH},
                        REQUEST_CODE_BLUETOOTH);
            }
        }

        // 알림 권한 요청 (Android 13 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_NOTIFICATION);
            }
        }
    }

    // Bluetooth 연결 상태 확인
    private void checkBluetoothConnection() {
        if (bluetoothAdapter == null) {
            Log.e("BluetoothError", "BluetoothAdapter is null.");
            return;
        }

        // 권한 확인 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.e("PermissionError", "BLUETOOTH_CONNECT 권한이 필요합니다.");
            Toast.makeText(this, "블루투스 연결 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;  // 권한이 없으면 연결을 시도하지 않음
        }

        if (bluetoothAdapter.isEnabled()) {
            boolean isTargetDeviceConnected = false;

            // Bluetooth 장치 연결 상태 확인
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

    private void navigateToMainActivity() {
        Intent intent = new Intent(SubMainActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            checkBluetoothConnection();  // 설정 후 다시 연결 상태 확인
        }
    }

    // 권한 요청 결과 처리
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우 Bluetooth 작업을 실행
                Toast.makeText(this, "Bluetooth 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                // 권한이 거부된 경우
                Toast.makeText(this, "Bluetooth 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_CODE_NOTIFICATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // BLE 스캔 시작 및 거리 확인
    private void startBLEScan() {
        // 권한 확인 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.e("PermissionError", "BLUETOOTH_SCAN 권한이 필요합니다.");
            Toast.makeText(this, "BLE 스캔 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            return;  // 권한이 없으면 스캔을 시작하지 않음
        }

        try {
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                        BluetoothDevice device = result.getDevice();
                        int rssi = result.getRssi();  // RSSI 값

                        if (TARGET_DEVICE_NAME.equals(device.getName())) {
                            if (rssi < DISTANCE_THRESHOLD) {
                                sendDistanceAlert();  // 거리가 멀어지면 알림
                            }
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                        Log.e("BluetoothError", "BLE scan failed with error code: " + errorCode);
                    }
                };

                bluetoothLeScanner.startScan(scanCallback);
            }
        } catch (SecurityException e) {
            Log.e("BluetoothError", "Bluetooth 권한이 부족하여 스캔을 시작할 수 없습니다.", e);
        }
    }

    private void stopBluetoothScan() {
        // 권한 확인 (Android 12 이상)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없다면 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_CODE_BLUETOOTH_SCAN);
                return;  // 권한을 요청하고 나서 다시 호출
            }
        }

        // BluetoothLeScanner 객체가 있는지 확인
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothLeScanner != null) {
                bluetoothLeScanner.stopScan(scanCallback);  // 스캔 중지
            }
        }
    }

    private void sendDistanceAlert() {
        // 알림을 보내는 코드
        Toast.makeText(this, "ESP32 장치와의 거리가 멀어졌습니다!", Toast.LENGTH_SHORT).show();
        // Notification을 사용하여 더 구체적인 알림을 추가할 수 있습니다.
        createNotification();
    }

    private void createNotification() {
        // Notification을 생성하여 사용자에게 알림을 보냅니다.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(android.R.drawable.ic_dialog_info)  // 기본 아이콘 사용
                .setContentTitle("거리 경고")
                .setContentText("ESP32 장치와의 거리가 멀어졌습니다!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // 알림 권한이 허용되어 있는지 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        } else {
            Log.e("PermissionError", "알림 권한이 필요합니다.");
        }
    }
}
