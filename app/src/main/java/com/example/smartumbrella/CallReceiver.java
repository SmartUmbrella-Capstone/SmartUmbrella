package com.example.smartumbrella;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private BLEManager bleManager;

    // Constructor to initialize BLEManager
    public CallReceiver(BLEManager bleManager) {
        this.bleManager = bleManager;
    }

    // Zero-argument constructor required for instantiation by the system
    public CallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the incoming intent is related to call state
        if (intent.getAction() != null && intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            // Check the call state (incoming or ended)
            if (state != null) {
                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    // The phone is ringing, so send a call alert to the BLE device
                    String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                    Log.d(TAG, "Incoming call from: " + incomingNumber);

                    // 데이터베이스에서 체크박스 상태를 읽어옴
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    Cursor cursor = dbHelper.getUserSettings();
                    boolean isChecked = false;

                    if (cursor != null) {
                        try {
                            if (cursor.moveToFirst()) {
                                isChecked = cursor.getInt(cursor.getColumnIndexOrThrow("check_box_state")) == 1;
                            }
                        } finally {
                            cursor.close(); // 반드시 커서를 닫아야 합니다.
                        }
                    }

                    // 메시지를 체크박스 상태에 맞게 설정
                    String callAlertMessage = isChecked ? "Call1" : "Call"; // 체크박스가 1일 때 "Call1", 0일 때 "Call"

                    // Send the call alert message to the BLE device
                    if (bleManager != null) {
                        bleManager.sendCallAlert(callAlertMessage); // Send the call alert message
                    } else {
                        Log.e(TAG, "BLEManager is not initialized.");
                    }
                }
            }
        }
    }


}
