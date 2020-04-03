package com.ammar.myapplication;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        if(/*remoteMessage.getData().get("ACTION").equals("BLUETOOTH")*/ false){
            Intent intent = new Intent(this,MyBluetoothService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        else {
            SharedPreferences myPrefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
            boolean track = myPrefs.getBoolean("TRACKER", false);
            if (track) {
                Intent intent = new Intent(this, MyLocationService.class);
                intent.setAction(ACTION_PROCESS_UPDATES);
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
