package com.example.smartumbrella;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    // Zero-argument constructor required for instantiation by the system
    public SmsReceiver() {
    }
    private static final String TAG = "SmsReceiver";
    private BLEManager bleManager;


    // Constructor to initialize BLEManager
    public SmsReceiver(BLEManager bleManager) {
        this.bleManager = bleManager;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            if (messages != null && messages.length > 0) {
                // Concatenate multi-part message if needed
                StringBuilder messageBody = new StringBuilder();
                for (SmsMessage message : messages) {
                    messageBody.append(message.getMessageBody());
                }

                String messageText = messageBody.toString();
                String sender = messages[0].getOriginatingAddress(); // The sender's phone number

                // Log SMS details
                Log.d(TAG, "SMS received from: " + sender + ", Content: " + messageText);

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
                String smsAlertMessage = isChecked ? "OK1" : "OK"; // 체크박스가 1일 때 "OK1", 0일 때 "OK"

                // Send the SMS alert message to the BLE device
                if (bleManager != null) {
                    bleManager.sendSmsAlert(smsAlertMessage); // Send the SMS alert message
                } else {
                    Log.e(TAG, "BLEManager is not initialized.");
                }
            } else {
                Log.e(TAG, "No SMS messages received or parsing error occurred.");
            }
        }
    }


}