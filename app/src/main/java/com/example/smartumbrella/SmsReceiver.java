package com.example.smartumbrella;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

                // Send a fixed "OK" message to the BLE device
                if (bleManager != null) {
                    String okMessage = "OK"; // Send "OK" regardless of the SMS content
                    bleManager.sendSmsAlert(okMessage); // Send the "OK" message
                } else {
                    Log.e(TAG, "BLEManager is not initialized.");
                }
            } else {
                Log.e(TAG, "No SMS messages received or parsing error occurred.");
            }
        }
    }
}