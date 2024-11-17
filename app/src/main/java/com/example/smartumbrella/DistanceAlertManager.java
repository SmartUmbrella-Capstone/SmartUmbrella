package com.example.smartumbrella;

import android.content.Context;

public class DistanceAlertManager {

    private Context context;
    private int distanceThreshold; // 임계값 (미터 단위)
    private Listener listener; // 이벤트 콜백

    // RSSI와 거리 변환 상수 (일반적인 BLE 환경 기준)
    private static final double RSSI_AT_1_METER = -59; // 1미터 거리에서의 RSSI
    private static final double ENVIRONMENTAL_FACTOR = 2.0; // 환경 감쇠 계수

    // 인터페이스 정의
    public interface Listener {
        void onDistanceAlertTriggered(double distance);
    }

    // 생성자
    public DistanceAlertManager(Context context, int distanceThreshold, Listener listener) {
        this.context = context;
        this.distanceThreshold = distanceThreshold;
        this.listener = listener;
    }

    // RSSI 값을 거리로 변환
    public double calculateDistance(int rssi) {
        return Math.pow(10, (RSSI_AT_1_METER - rssi) / (10 * ENVIRONMENTAL_FACTOR));
    }

    // RSSI 값으로 거리 확인 및 알림 트리거
    public void checkDistance(int rssi) {
        double distance = calculateDistance(rssi);
        if (distance > distanceThreshold) {
            triggerAlert(distance);
        }
    }

    // 알림 트리거
    private void triggerAlert(double distance) {
        if (listener != null) {
            listener.onDistanceAlertTriggered(distance);
        }
    }
}
