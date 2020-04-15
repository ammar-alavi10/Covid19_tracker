package com.ammar.myapplication;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);

        if(remoteMessage.getData().get("message").equals("Bluetooth")){
            Intent intent = new Intent(this,MyBluetoothService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        else if(remoteMessage.getData().get("message").equals("Tracking your Location")){
            boolean track = myPrefs.getBoolean("TRACKER", false);
            if (track)
            {
                Intent intent = new Intent(this, MyLocationService.class);
                if (isMyServiceRunning(MyLocationService.class)) {
                    stopService(intent);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        }
        else if(remoteMessage.getData().get("message").equals("Table")){
            boolean track = myPrefs.getBoolean("TRACKER", false);
            if(track)
            {
                Intent intent = new Intent(this, TableTrackerService.class);
                if (isMyServiceRunning(TableTrackerService.class)) {
                    stopService(intent);
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }

        }
        Log.d("YESSS","Notification aaya");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
