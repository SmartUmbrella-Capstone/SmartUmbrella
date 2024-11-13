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

    // ESP32와 맞춰야 할 UUID
    private static final String TARGET_DEVICE_NAME = "SmartUmbrella";
    private static final String SERVICE_UUID = "37C4E592-77F4-2C36-8BE2-6E5456E6E2CA";
    private static final String CHARACTERISTIC_UUID = "00001111-0000-1000-8000-00805f9b34fb";

    public BLEManager(Context context) {
        this.context = context;
        this.bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            throw new RuntimeException("Bluetooth is not supported on this device");
        }
        this.scanner = bluetoothAdapter.getBluetoothLeScanner();
    }

    // 권한 체크 함수
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

    // BLE 스캔 시작
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

    // BLE 스캔 콜백
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
                connect(); // 기기 찾으면 연결
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

    // 기기 연결 함수
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public void connect() {
        if (selectedDevice == null) {
            Log.e("BLEManager", "선택된 기기가 없습니다. 스캔을 완료했는지 확인하십시오.");
            throw new IllegalStateException("Device not found. Ensure scanning is completed before connecting.");
        }

        try {
            gatt = selectedDevice.connectGatt(context, false, gattCallback);
            Log.d("BLEManager", "기기 연결 시도 중...");
            Toast.makeText(context, "기기 연결 시도 중...", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Log.e("BLEManager", "권한 부족으로 연결 실패", e);
            Toast.makeText(context, "권한 부족으로 연결 실패", Toast.LENGTH_SHORT).show();
        }
    }
    // 기기 연결 끊기 함수
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

    // GATT 콜백
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    Log.d("BLEManager", "Bluetooth 연결 성공");

                    // 메인 스레드에서 Toast 표시
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context, "기기와 연결되었습니다.", Toast.LENGTH_SHORT).show());
                    }

                    // 연결 성공 후 서비스 검색 시작
                    gatt.discoverServices();
                } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d("BLEManager", "Bluetooth 연결 해제");

                    // 메인 스레드에서 Toast 표시
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() ->
                                Toast.makeText(context, "기기와의 연결이 해제되었습니다.", Toast.LENGTH_SHORT).show());
                    }
                }
            } else {
                Log.e("BLEManager", "Bluetooth 연결 실패, 상태 코드: " + status);

                // 메인 스레드에서 Toast 표시
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "기기와 연결할 수 없습니다.", Toast.LENGTH_SHORT).show());
                }

                // 연결 실패 시 gatt 객체를 정리
                gatt.close();
                BLEManager.this.gatt = null;
            }
        }

        // 서비스 검색 완료 시 호출
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLEManager", "서비스 검색 완료");

                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_UUID));
                    if (characteristic != null) {
                        writeCharacteristic(characteristic, "OK"); // "OK" 메시지 전송 예시
                    } else {
                        Log.e("BLEManager", "특성 UUID 불일치: " + CHARACTERISTIC_UUID);
                    }
                } else {
                    Log.e("BLEManager", "서비스 UUID 불일치: " + SERVICE_UUID);
                }
            } else {
                Log.e("BLEManager", "서비스 검색 실패");
            }
        }
    };


    // 데이터 쓰기 함수
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, String value) {
        characteristic.setValue(value);
        @SuppressLint("MissingPermission") boolean success = gatt.writeCharacteristic(characteristic);
        if (!success) {
            Log.e("BLEManager", "특성에 데이터 쓰기 실패");
            Toast.makeText(context, "데이터 전송 실패", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("BLEManager", "데이터 전송 성공: " + value);
            Toast.makeText(context, "데이터 전송 성공: " + value, Toast.LENGTH_SHORT).show();
        }
    }
}
