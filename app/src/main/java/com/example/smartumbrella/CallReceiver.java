package com.example.smartumbrella;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

                    // Read checkbox state from SharedPreferences
                    SharedPreferences sharedPreferences = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE);
                    boolean isChecked = sharedPreferences.getBoolean("isChecked", false); // 기본값은 false

                    // Set message based on checkbox state
                    String callAlertMessage = isChecked ? "Cal1l" : "Call0"; // 체크박스가 1일 때 "Call", 0일 때 "Call1"

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
