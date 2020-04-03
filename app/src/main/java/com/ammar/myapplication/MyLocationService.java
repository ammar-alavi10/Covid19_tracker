package com.ammar.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyLocationService extends Service {

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final String TAG = MyLocationService.class.getSimpleName();


    SharedPreferences myPrefs;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this;
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


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = this;
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000*20);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                else {
                    Log.d("APAN CHANGE","Location Found");
                    Location loc = locationResult.getLastLocation();
                    List<Location> locations = locationResult.getLocations();

                    sendLocationToServer(context, locations);

//                    List<Location> nearbyLocations = requestLocationsFromServer();
                    distOfLoc(context, locations.get(locations.size()-1),locations);
                    LocationResultHelper locationResultHelper = new LocationResultHelper(context,
                            locations);
                    // Save the location data to SharedPreferences.
                    locationResultHelper.saveResults();
                    // Show notification with the location data.
                    //locationResultHelper.showNotification(danger);
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));

                }
            }
        };
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        if(mFusedLocationClient!=null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        super.onDestroy();
    }

    private void distOfLoc(Context context, final Location mylocation, List<Location> locations) {
        //code to get Locations from server
        Log.d("FirebaseService","Firestore ke andar jaane ki koshish");

        myPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
        int mychannel = myPrefs.getInt("MYCHANNELID",1000);
        int i=0;
        distfun(context,locations,i,mychannel,mylocation);


    }

    private void distfun(Context context, List<Location> locations, int i, int mychannel, Location mylocation)
    {

        if(i < myPrefs.getInt("TotalChannels",0)) {
            int channelname = myPrefs.getInt("CHANNELID" + i, 1000);
            final String category = String.valueOf(myPrefs.getInt("CATEGORY" + i, 1000));
            Log.d("Channel name", "channel" + channelname);
            String token = myPrefs.getString("TOKEN", "");
            if (channelname != mychannel && channelname != 1000) {

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(100, TimeUnit.SECONDS)
                        .readTimeout(100, TimeUnit.SECONDS)
                        .build();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(ApiCalls.BASE_URL)
                        .client(client)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiCalls UserLocationApi = retrofit.create(ApiCalls.class);
                Call<UserLocation> call = UserLocationApi.getLocationUser("Token " + token, channelname);
                call.enqueue(new Callback<UserLocation>() {
                    @Override
                    public void onResponse(Call<UserLocation> call, Response<UserLocation> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            Log.d( "Response Empty",response.toString());

                            distfun(context, locations, i + 1, mychannel, mylocation);
                        } else {
                            Log.d(TAG, "onSuccess: LIST");
                            Log.d("Call ke andar", response.body().toString());
                            UserLocation userLocation = response.body();
                            Double lat = (double) userLocation.getLatitude();
                            Double lon = (double) userLocation.getLongitude();
                            if (!category.equals("5")) {
                                Location location = new Location("New Location");
                                location.setLatitude(lat);
                                location.setLongitude(lon);
                                double distance = mylocation.distanceTo(location);
                                Log.d("DISTANCESERVICE", String.valueOf(distance));
                                if (distance < 100.0) {
                                    Log.d("DANGEROUS", "DANGER HAI");
                                    if (category.equals("1")) {
                                        SharedPreferences.Editor editor = myPrefs.edit();
                                        editor.putString("DANGER", "Covid-19 POSITIVE");
                                        editor.apply();
                                        LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);
                                        locationResultHelper.showNotification("Covid-19 Postive");
                                    } else {
                                        SharedPreferences.Editor editor = myPrefs.edit();
                                        editor.putString("DANGER", "Dangerous");
                                        editor.apply();
                                        LocationResultHelper locationResultHelper = new LocationResultHelper(context, locations);
                                        locationResultHelper.showNotification("Danger");
                                    }
                                } else {
                                    distfun(context, locations, i + 1, mychannel, mylocation);
                                }
                            } else {
                                distfun(context, locations, i + 1, mychannel, mylocation);
                            }
                        }


                    }

                    @Override
                    public void onFailure(Call<UserLocation> call, Throwable t) {
                        t.printStackTrace();
                        distfun(context, locations, i + 1, mychannel, mylocation);
                    }
                });
            }
            else {
                distfun(context, locations, i + 1, mychannel, mylocation);
            }
        }
    }

    private void sendLocationToServer(Context context, List<Location> locations) {
        //code to send locations to server

        okhttp3.OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(100, TimeUnit.SECONDS)
                .readTimeout(100, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiCalls.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        myPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
        String token = myPrefs.getString("TOKEN","");

        for(int i = 0; i < locations.size() && i < 10 ; i++) {

            float latitude = (float) locations.get(i).getLatitude();
            float longitude = (float) locations.get(i).getLongitude();

            ApiCalls locationApi = retrofit.create(ApiCalls.class);
            Call<Object> call = locationApi.locationUpdater("Token "+token, latitude, longitude);
            call.enqueue(new CallBackWithRetry<Object>("UpdateLocation", 10) {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if(!response.isSuccessful() && response.body() != null){
                        Log.d("LOCATION UPDATE","NO RESPONSE");
                        return;
                    }
                    Log.d(TAG,response.toString());
                    Log.d(TAG,"location updated");
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    super.onFailure(call, t);
                }
            });
        }

    }

}
