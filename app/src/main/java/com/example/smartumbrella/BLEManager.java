package com.example.smartumbrella;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;

import java.util.UUID;

public class BLEManager {
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner scanner;
    private BluetoothDevice selectedDevice;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;

    // UUIDs for ESP32 and BLE service/characteristic
    private static final String TARGET_DEVICE_NAME = "SmartUmbrella";
    private static final String SERVICE_UUID = "37C4E592-77F4-2C36-8BE2-6E5456E6E2CA";
    private static final String CHARACTERISTIC_UUID = "00001111-0000-1000-8000-00805f9b34fb";

    public BLEManager(Context context) {
        this.context = context;
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
        this.scanner = bluetoothAdapter != null ? bluetoothAdapter.getBluetoothLeScanner() : null;

        if (bluetoothAdapter == null) {
            throw new RuntimeException("Bluetooth is not supported on this device");
        }
    }

    // Check permissions
    public boolean haveAllPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED;
        } else {
            return context.checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    // Start BLE scan
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

    // BLE scan callback
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

    // Connect to device
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

    // Disconnect from device
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

    // GATT callback
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("BLEManager", "Bluetooth 연결 성공");
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "Bluetooth 연결 성공", Toast.LENGTH_SHORT).show()
                    );
                    gatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d("BLEManager", "Bluetooth 연결 해제");
                }
            } else {
                Log.e("BLEManager", "Bluetooth 연결 실패, 상태 코드: " + status);
                gatt.close();
                BLEManager.this.gatt = null;
            }
        }

        // Called when services are discovered
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                }
            } else {
                Log.e("BLEManager", "서비스 검색 실패");
            }
        }
    };

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
}
