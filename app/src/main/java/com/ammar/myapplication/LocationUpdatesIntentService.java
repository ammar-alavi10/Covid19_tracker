package com.ammar.myapplication;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Handles incoming location updates and displays a notification with the location data.
 *
 * For apps targeting API level 25 ("Nougat") or lower, location updates may be requested
 * using {@link android.app.PendingIntent#getService(Context, int, Intent, int)} or
 * {@link android.app.PendingIntent#getBroadcast(Context, int, Intent, int)}. For apps targeting
 * API level O, only {@code getBroadcast} should be used.
 *
 *  Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 *  less frequently than the interval specified in the
 *  {@link com.google.android.gms.location.LocationRequest} when the app is no longer in the
 *  foreground.
 */
public class LocationUpdatesIntentService extends IntentService {

    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";
    private static final String TAG = LocationUpdatesIntentService.class.getSimpleName();


    SharedPreferences myPrefs;
    public LocationUpdatesIntentService() {
        // Name the worker thread.
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = this;
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    FirebaseApp.initializeApp(context);

                    sendLocationToServer(context, locations);

//                    List<Location> nearbyLocations = requestLocationsFromServer();
                    String danger = distOfLoc(context, locations.get(locations.size()-1));
                    LocationResultHelper locationResultHelper = new LocationResultHelper(this,
                            locations);
                    // Save the location data to SharedPreferences.
                    locationResultHelper.saveResults();
                    // Show notification with the location data.
                    locationResultHelper.showNotification(danger);
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(this));
                }
            }
        }
    }
    private String distOfLoc(Context context, final Location mylocation) {
        //code to get Locations from server
        Log.d("Firebase","Firestore ke andar jaane ki koshish");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        myPrefs = context.getSharedPreferences("myPrefs", MODE_PRIVATE);
        String mychannel = String.valueOf(myPrefs.getInt("MYCHANNELID",1000));
        final String[] danger = {"None"};
        int no_of_channel = myPrefs.getInt("TotalChannels",0);
        for (int i = 0 ; i < no_of_channel ; i++) {


            String channelname = String.valueOf(myPrefs.getInt("CHANNELID" + String.valueOf(i), 1000));
            final String category = String.valueOf(myPrefs.getInt("CATEGORY" + String.valueOf(i), 1000));
            Log.d("Channel name","channel"+channelname);
            if (channelname != mychannel && channelname != "1000") {

                DocumentReference user = db.collection("main_data").document("channel"+channelname);
                user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "onSuccess: LIST");
                            Log.d("Firestore ke andar", "aaya");
                            DocumentSnapshot doc = documentSnapshot;
                            String latitude = (String) doc.get("latitude");
                            String longitude = (String) doc.get("longitude");
                            if (doc.get("latitude") != null && !category.equals("5")) {
                                Double lat = Double.parseDouble(latitude);
                                Double lon = Double.parseDouble(longitude);
                                Location location = new Location("New Location");
                                location.setLatitude(lat);
                                location.setLongitude(lon);
                                double distance = mylocation.distanceTo(location);
                                Log.d("DISTANCE", String.valueOf(distance));
                                if (distance < 100.0) {
                                    Log.d("DANGEROUS","DANGER HAI");
                                    if (category.equals("1")) {
                                        //danger[0] = "Covid-19 POSITIVE";
                                        SharedPreferences.Editor editor = myPrefs.edit();
                                        editor.putString("DANGER", "Covid-19 POSITIVE");
                                        editor.apply();
                                    } else {
                                        //danger[0] = "dangerous";
                                        SharedPreferences.Editor editor = myPrefs.edit();
                                        editor.putString("DANGER", "Dangerous");
                                        editor.apply();
                                    }
                                }
                            }
                        }
                        else{
                            Log.d(TAG, "onSuccess: LIST Empty");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.toString());
                    }
                });

                Log.d("Return kr","bhai");
                if (myPrefs.getString("DANGER","None")!="None")
                    return myPrefs.getString("DANGER","None");
            }
        }

        return myPrefs.getString("DANGER","None");

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