package com.example.smartumbrella;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class BLEManager {
    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private BluetoothDevice selectedDevice;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic batteryLevelCharacteristic;
    private static final String TARGET_DEVICE_NAME = "SmartUmbrella";
    private static final String SERVICE_UUID = "37C4E592-77F4-2C36-8BE2-6E5456E6E2CA";
    private static final String CHARACTERISTIC_UUID = "00001111-0000-1000-8000-00805f9b34fb";
    private static final String BATTERY_LEVEL_UUID = "00002A19-0000-1000-8000-00805f9b34fb";
    private int getRssiThreshold() {
        // 데이터베이스에서 설정된 거리 값을 가져옴
        int userDistanceThreshold = dbHelper.getDistanceSetting();

        // 사용자 설정에 맞게 RSSI 임계값으로 변환
        int rssiThreshold = convertDistanceToRssi(userDistanceThreshold);

        // 설정된 RSSI 임계값을 로그로 출력
        Log.d("BLEManager", "RSSI 임계값: " + rssiThreshold);

        return rssiThreshold;
    }

    private int convertDistanceToRssi(int distance) {
        if (distance <= 1) {
            return -30;  // 1미터 이하일 때 -30 dBm
        } else if (distance <= 3) {
            return -45;  // 3미터 이하일 때 -45 dBm
        } else if (distance <= 5) {
            return -55;  // 5미터 이하일 때 -55 dBm
        } else if (distance <= 10) {
            return -65;  // 10미터 이하일 때 -65 dBm
        } else if (distance <= 20) {
            return -75;  // 20미터 이하일 때 -75 dBm
        } else if (distance <= 30) {
            return -85;  // 30미터 이하일 때 -85 dBm
        } else {
            return -85;  // 30미터 초과일 때 기본값 -85 dBm
        }
    }
    private Handler rssiHandler = new Handler(Looper.getMainLooper());
    private Runnable rssiRunnable;
    private DatabaseHelper dbHelper;



    public BLEManager(Context context) {
        dbHelper = new DatabaseHelper(context);  // DatabaseHelper 인스턴스 생성
        this.context = context;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new RuntimeException("Bluetooth is not supported on this device");
        }
        this.bluetoothAdapter = bluetoothAdapter;
        this.scanner = bluetoothAdapter.getBluetoothLeScanner();
        createNotificationChannel();  // 알림 채널을 생성
    }

    // 알림 채널을 생성하는 메서드
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BLE Notification Channel";  // 채널 이름
            String description = "Channel for BLE notifications";  // 채널 설명
            int importance = NotificationManager.IMPORTANCE_HIGH;  // 중요도 설정

            NotificationChannel channel = new NotificationChannel("BLE_NOTIFICATION", name, importance);
            channel.setDescription(description);

            // 시스템에 채널 등록
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    public boolean haveAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    public void startScanning() {
        if (!haveAllPermissions()) {
            Log.e("BLEManager", "Required Bluetooth permissions are not granted.");
            Toast.makeText(context, "BLE 권한이 부족합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bluetoothAdapter.isEnabled() && scanner != null) {
            Log.d("BLEManager", "BLE 스캔 시작");
            scanner.startScan(scanCallback);
            Toast.makeText(context, "BLE 스캔 시작", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("BLEManager", "Bluetooth가 비활성화되어 있거나 스캐너를 사용할 수 없습니다.");
            Toast.makeText(context, "Bluetooth가 꺼져 있거나 스캐너 사용 불가.", Toast.LENGTH_SHORT).show();
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            if (result.getDevice().getName() != null && result.getDevice().getName().equals(TARGET_DEVICE_NAME)) {
                Log.d("BLEManager", "기기 발견: " + TARGET_DEVICE_NAME);
                selectedDevice = result.getDevice();
                scanner.stopScan(this);
                Toast.makeText(context, "기기 발견: " + TARGET_DEVICE_NAME, Toast.LENGTH_SHORT).show();
                connect();
            } else {
                Log.d("BLEManager", "기기 발견 실패 또는 이름 불일치");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("BLEManager", "BLE 스캔 실패: 에러 코드 " + errorCode);
            Toast.makeText(context, "스캔 실패: 에러 코드 " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect() {
        if (selectedDevice == null) {
            Log.e("BLEManager", "선택된 기기가 없습니다. 스캔을 완료했는지 확인하십시오.");
            throw new IllegalStateException("Device not found. Ensure scanning is completed before connecting.");
        }

        gatt = selectedDevice.connectGatt(context, false, gattCallback);
        Log.d("BLEManager", "기기 연결 시도 중...");
        Toast.makeText(context, "기기 연결 시도 중...", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (gatt != null) {
            Log.d("BLEManager", "기기 연결 끊기 시도...");
            gatt.disconnect();
            gatt.close();
            gatt = null;
            Toast.makeText(context, "기기와의 연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
        } else {
            Log.e("BLEManager", "연결된 기기가 없습니다.");
            Toast.makeText(context, "연결된 기기가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("BLEManager", "Bluetooth 연결 성공");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Bluetooth 연결 성공", Toast.LENGTH_SHORT).show());
                    gatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d("BLEManager", "Bluetooth 연결 해제");
                    stopRssiReading();  // 연결 해제 시, RSSI 읽기 중지
                }
            } else {
                Log.e("BLEManager", "Bluetooth 연결 실패, 상태 코드: " + status);
                gatt.close();
                BLEManager.this.gatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                    batteryLevelCharacteristic = service.getCharacteristic(UUID.fromString(BATTERY_LEVEL_UUID));

                    readBatteryLevel();
                    startRssiReading();  // 연결이 완료되면 RSSI 주기적 읽기 시작
                }
            } else {
                Log.e("BLEManager", "서비스 검색 실패");
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            int rssiThreshold = getRssiThreshold();
            Log.d("BLEManager", "현재 RSSI: " + rssi);

            if (rssi < rssiThreshold) {
                showNotification("기기와 멀어졌습니다!", "기기가 임계값 이상 멀어졌습니다.");
                saveLocationOnAlert(); // 위치 저장
            }
        }
    };

        @SuppressLint("MissingPermission")
    public void readBatteryLevel() {
        if (batteryLevelCharacteristic != null) {
            gatt.readCharacteristic(batteryLevelCharacteristic);
        } else {
            Log.e("BLEManager", "배터리 레벨 특성이 없습니다.");
        }
    }

    // RSSI 값을 주기적으로 3초마다 읽기
    private void startRssiReading() {
        if (gatt != null) {
            // Bluetooth 권한 확인
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                            == PackageManager.PERMISSION_GRANTED) {

                // 권한이 있으면 RSSI 값을 주기적으로 읽기
                rssiRunnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            gatt.readRemoteRssi();  // RSSI 값 읽기
                        } catch (SecurityException e) {
                            Log.e("BLEManager", "권한이 없어 RSSI를 읽을 수 없습니다: " + e.getMessage());
                            Toast.makeText(context, "권한이 부족합니다.", Toast.LENGTH_SHORT).show();
                        }

                        // 3초 후 다시 실행
                        rssiHandler.postDelayed(this, 3000);
                    }
                };
                rssiHandler.post(rssiRunnable);  // 첫 실행
            } else {
                // Bluetooth 권한이 부족한 경우
                Log.e("BLEManager", "Bluetooth 권한이 부족합니다.");
                Toast.makeText(context, "Bluetooth 권한이 부족합니다.", Toast.LENGTH_SHORT).show();

                // 사용자에게 권한을 요청
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                        1);  // 권한 요청
            }
        } else {
            Log.e("BLEManager", "연결된 GATT 서버가 없습니다.");
            Toast.makeText(context, "연결된 GATT 서버가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // RSSI 값을 읽는 주기를 중지
    private void stopRssiReading() {
        if (rssiHandler != null && rssiRunnable != null) {
            rssiHandler.removeCallbacks(rssiRunnable);  // 더 이상 실행되지 않도록 호출
        }
    }

    public void showNotification(String title, String message) {
        try {
            // 알림을 생성하는 빌더
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "BLE_NOTIFICATION")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // 권한이 있는지 확인
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                // 권한이 있는 경우, 알림을 표시
                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(1, builder.build());
            } else {
                // 권한이 없으면 권한 요청
                Log.e("BLEManager", "알림 권한이 부족합니다.");
                Toast.makeText(context, "알림 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1);  // 권한 요청
            }
        } catch (SecurityException e) {
            Log.e("BLEManager", "알림 표시 중 오류 발생: " + e.getMessage());
            Toast.makeText(context, "알림 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    @SuppressLint("MissingPermission")
    public void sendDistanceAlert(String message) {
        if (characteristic != null) {
            characteristic.setValue(message);  // 메시지를 특성에 설정
            boolean success = gatt.writeCharacteristic(characteristic); // BLE로 전송
            if (success) {
                Log.d("BLEManager", "거리 경고 전송 성공: " + message);
                Toast.makeText(context, "거리 경고 전송 성공: " + message, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("BLEManager", "거리 경고 전송 실패");
                Toast.makeText(context, "거리 경고 전송 실패", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("BLEManager", "전송할 특성이 없습니다.");
            Toast.makeText(context, "전송할 특성이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }



    // Send SMS alert via BLE
    @SuppressLint("MissingPermission")
    public void sendSmsAlert(String message) {
        if (characteristic != null) {
            characteristic.setValue(message);  // 메시지를 특성에 설정
            boolean success = gatt.writeCharacteristic(characteristic); // 특성을 ESP32로 전송
            if (success) {
                Log.d("BLEManager", "SMS 경고 전송 성공: " + message);
                Toast.makeText(context, "SMS 경고 전송 성공: " + message, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("BLEManager", "SMS 경고 전송 실패");
                Toast.makeText(context, "SMS 경고 전송 실패", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("BLEManager", "전송할 특성이 없습니다.");
            Toast.makeText(context, "전송할 특성이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    // Send SMS alert via BLE
    @SuppressLint("MissingPermission")
    public void sendCallAlert(String message) {
        if (characteristic != null) {
            characteristic.setValue(message);  // 메시지를 특성에 설정
            boolean success = gatt.writeCharacteristic(characteristic); // 특성을 ESP32로 전송
            if (success) {
                Log.d("BLEManager", "SMS 경고 전송 성공: " + message);
                Toast.makeText(context, "SMS 경고 전송 성공: " + message, Toast.LENGTH_SHORT).show();
            } else {
                Log.e("BLEManager", "SMS 경고 전송 실패");
                Toast.makeText(context, "SMS 경고 전송 실패", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("BLEManager", "전송할 특성이 없습니다.");
            Toast.makeText(context, "전송할 특성이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasLocationPermission() { //GPS 위치 권한 확인
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public void saveLocationOnAlert() { // 위치 권한 확인
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.e("BLEManager", "위치 권한 없음");
            return;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        }

        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // 위치 정보 저장
            try {
                dbHelper.insertLocationLog(timestamp, latitude, longitude);
                Log.d("BLEManager", "위치 저장 성공: " + timestamp + ", " + latitude + ", " + longitude);
            } catch (Exception e) {
                Log.e("BLEManager", "위치 저장 실패: " + e.getMessage());
            }
        } else {
            Log.e("BLEManager", "위치 데이터 없음");
        }
    }

}
