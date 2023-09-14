package com.example.safetyapp1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_SMS_CALL = 1;
    private EditText emergencyNumberEditText;
    private EditText emergencyMessageEditText;
    private boolean isVolumeButtonDown = false;
    private Handler longPressHandler = new Handler(Looper.getMainLooper());
    private boolean isAppStopped = false; // Track the app's state

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emergencyNumberEditText = findViewById(R.id.emergencyNumber);
        emergencyMessageEditText = findViewById(R.id.emergencyMessage);

        requestPermissions();
    }

    private void requestPermissions() {
        String[] permissions = {Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE};
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_SMS_CALL);
                return;
            }
        }
    }

    public void startSafetyApp(View view) {
        if (isAppStopped) { // Check if the app is stopped
            Toast.makeText(this, "App is stopped. Press 'Start Safety App' to enable.", Toast.LENGTH_SHORT).show();
            return; // Do not send messages or make calls if the app is stopped
        }

        String phoneNumber = emergencyNumberEditText.getText().toString().trim();
        String message = emergencyMessageEditText.getText().toString().trim();

        if (!phoneNumber.isEmpty() && !message.isEmpty()) {
            sendSMS(phoneNumber, message);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopSafetyApp(View view) {
        isAppStopped = true; // Set the app's state to "stopped"
        Toast.makeText(this, "App is stopped. Press 'Start Safety App' to enable.", Toast.LENGTH_SHORT).show();
    }
    public void startApp(View view) {
        isAppStopped = false; // Set the app's state to "stopped"
        Toast.makeText(this, "App is Started.", Toast.LENGTH_SHORT).show();
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_SMS_CALL) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                Toast.makeText(this, "Permissions are required to use this app.", Toast.LENGTH_SHORT).show();
            }
        }

        // Call the superclass implementation
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            // Check if the volume button is already down
            if (!isVolumeButtonDown) {
                isVolumeButtonDown = true;

                // Post a delayed action to handle the long-press
                longPressHandler.postDelayed(() -> {
                    if (isVolumeButtonDown) {
                        String phoneNumber = emergencyNumberEditText.getText().toString().trim();
                        if (!phoneNumber.isEmpty()) {
                            if (!isAppStopped) { // Check if the app is not stopped
                                makeCall(phoneNumber);
                                Toast.makeText(this, "Calling", Toast.LENGTH_SHORT).show();
                                sendSMS(phoneNumber, emergencyMessageEditText.getText().toString().trim());
                                Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "App is stopped. Press 'Start Safety App' to enable.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        isVolumeButtonDown = false;
                    }
                }, 5000); // 5000 milliseconds (5 seconds) delay for long-press
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            isVolumeButtonDown = false; // Reset the flag on key release
        }
        return super.onKeyUp(keyCode, event);
    }

    private void makeCall(String phoneNumber) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to initiate the call", Toast.LENGTH_SHORT).show();
        }
    }
}
