package com.ammar.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TableTrackerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
        updatetable();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= 26) {
            stopForeground(true);
        }
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        if (Build.VERSION.SDK_INT >= 26) {
//            String CHANNEL_ID = "my_channel_01";
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
//                    "Channel human readable title",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//
//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
//
//            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                    .setContentTitle("")
//                    .setContentText("").build();
//
//            startForeground(1, notification);
//        }

    }

    private void updatetable() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        final SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        String token = myPrefs.getString("TOKEN","KUCH NHI");

        ApiCalls locationApi = retrofit.create(ApiCalls.class);
        Call<Coordinates> call = locationApi.getLocationNearby("Token "+token);
        call.enqueue(new CallBackWithRetry<Coordinates>("UpdateLocation", 10) {
            @Override
            public void onResponse(Call<Coordinates> call, Response<Coordinates> response) {

                if(!response.isSuccessful() || response.body() == null){
                    return;
                }

                Coordinates coordinates = response.body();
                List<Global_plotted_coordinates> global_plotted_coordinates = coordinates.getGlobal_plotted_coordinates();
                User_plotted_data user_plotted_data = coordinates.getUser_plotted_data();
                int i;
                for(i = 0; i < global_plotted_coordinates.size(); i++)
                {
                    try{
                        int channelid = global_plotted_coordinates.get(i).getChannel_id();
                        int status = global_plotted_coordinates.get(i).getStatus();
                        Log.d("Channelid",String.valueOf(channelid));
                        Log.d("Status",String.valueOf(status));
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putInt("CHANNELID"+ i, channelid);
                        editor.putInt("CATEGORY"+ i, status);
                        editor.apply();
                        Float lat = global_plotted_coordinates.get(i).getLatitude();
                        Float lon = global_plotted_coordinates.get(i).getLongitude();
                        Log.d("Latitude",String.valueOf(lat));
                        Log.d("Longitude",String.valueOf(lon));
                        editor.putFloat("Channel"+i+"latitude",lat);
                        editor.putFloat("Channel"+i+"longitude",lon);
                        editor.apply();

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                int mychannel = user_plotted_data.getChannel_id();
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putInt("TotalChannels",i);
            }

            @Override
            public void onFailure(Call<Coordinates> call, Throwable t) {
                t.printStackTrace();
                super.onFailure(call, t);
            }
        });
    }
}
