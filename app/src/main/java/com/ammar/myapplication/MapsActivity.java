package com.ammar.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private static final long UPDATE_INTERVAL = 10 * 1000;
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Button mRequestUpdatesButton;
    Marker marker = null;

    private static final String TAG = MapsActivity.class.getSimpleName();

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    List<LatLng> loclist = new ArrayList<LatLng>();
    int mark = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor1 = myPrefs.edit();
        editor1.putInt("MARKER",0);
        editor1.apply();

        FirebaseApp.initializeApp(this);

        mRequestUpdatesButton = findViewById(R.id.tracking_button);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//         Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        } else {
            createLocationRequest();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        }


    }


    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    protected void onResume() {

        super.onResume();
        updateButtonsState(LocationRequestHelper.getRequesting(this));

    }

    @Override
    protected void onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }



    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(UPDATE_INTERVAL);

        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        // The main entry point for interacting with the location settings-enabler APIs.
        SettingsClient client = LocationServices.getSettingsClient(this);

        // Check whether the current location settings are satisfied.When the Task completes, your app can check
        // the location settings by looking at the status code from the LocationSettingsResponse object.
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        // Adds a listener that is called if the Task completes successfully.
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                try {
                    mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                            }
                        }
                    });
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        // Adds a listener that is called if the Task fails.
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                if (statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        //Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MapsActivity.this,
                                101);
                    } catch (IntentSender.SendIntentException sendEx) {
                        sendEx.printStackTrace();
                    }
                }
            }
        });
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_map),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted. Kick off the process of building and connecting to Location Request
                createLocationRequest();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_map),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }



    @SuppressLint("MissingPermission")
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(LocationResultHelper.KEY_LOCATION_UPDATES_RESULT)) {
            //mLocationUpdatesResultView.setText(LocationResultHelper.getSavedLocationResult(this));
        } else if (s.equals(LocationRequestHelper.KEY_LOCATION_UPDATES_REQUESTED)) {
            updateButtonsState(LocationRequestHelper.getRequesting(this));
        }
    }





    public void requestLocationUpdates(View view) {

        if((mRequestUpdatesButton.getText().toString()).equals("Start Tracking")) {
            try {
                Log.i(TAG, "Starting location updates");
                LocationRequestHelper.setRequesting(this, true);
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
            } catch (SecurityException e) {
                LocationRequestHelper.setRequesting(this, false);
                e.printStackTrace();
            }
            mRequestUpdatesButton.setText("Stop Tracking");
        }
        else
        {
            removeLocationUpdates(view);
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates(View view) {
        Log.i(TAG, "Removing location updates");
        LocationRequestHelper.setRequesting(this, false);
        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
        mRequestUpdatesButton.setText("Start Tracking");
    }

    /**
     * Ensures that only one button is enabled at any time. The Start Updates button is enabled
     * if the user is not requesting location updates. The Stop Updates button is enabled if the
     * user is requesting location updates.
     */
    private void updateButtonsState(boolean requestingLocationUpdates) {
        if (requestingLocationUpdates) {
            mRequestUpdatesButton.setText("Stop Tracking");
        } else {
            mRequestUpdatesButton.setText("Start Tracking");
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(mark == 0)
                {
                    mMap.clear();
                    LatLng myloc = new LatLng(location.getLatitude(), location.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(myloc)
                            .title("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,15));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,13));
                }
                else
                {
                    marker.remove();
                    mark++;
                    LatLng myloc = new LatLng(location.getLatitude(), location.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(myloc)
                            .title("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                }
                LatLng myloc = new LatLng(location.getLatitude(), location.getLongitude());
                marker = mMap.addMarker(new MarkerOptions()
                        .position(myloc)
                        .title("")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,15));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myloc,13));
                SharedPreferences myprefs = MapsActivity.this.getSharedPreferences("myPrefs",MODE_PRIVATE);
                //getandsavechannelnames();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if(!checkPermissions())
        {
            requestPermissions();
        }
        else
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000*6*5,100,locationListener);
        getandsavechannelnames();
        Log.d("Haa","Iske just upar");
    }

    private void getandsavechannelnames() {

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

        int i;
        ApiCalls locationApi = retrofit.create(ApiCalls.class);
//        Call<Coordinates> call = locationApi.getLocationNearby("Token "+token);

        Call<Coordinates> call = locationApi.getLocationNearby("Token "+token);
        call.enqueue(new CallBackWithRetry<Coordinates>("UpdateLocation", 10) {
            @Override
            public void onResponse(Call<Coordinates> call, Response<Coordinates> response) {

                if(!response.isSuccessful() || response.body() == null){
                    Toast.makeText(getApplicationContext(),"No nearby Patients", Toast.LENGTH_LONG).show();
                    return;
                }

                Coordinates coordinates = response.body();
                List<Global_plotted_coordinates> global_plotted_coordinates = coordinates.getGlobal_plotted_coordinates();
                User_plotted_data user_plotted_data = coordinates.getUser_plotted_data();
                int i;
                if(mMap!=null)
                {
                    mMap.clear();
                }
                for(i = 0; i < global_plotted_coordinates.size(); i++)
                {
                    try{
                        int channelid = global_plotted_coordinates.get(i).getChannel_id();
                        int status = global_plotted_coordinates.get(i).getStatus();
                        Log.d("Channelid",String.valueOf(channelid));
                        Log.d("Status",String.valueOf(status));
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putInt("CHANNELID"+ String.valueOf(i), channelid);
                        editor.putInt("CATEGORY"+ String.valueOf(i), status);
                        editor.apply();
                        Double lat = (double) global_plotted_coordinates.get(i).getLatitude();
                        Double lon = (double) global_plotted_coordinates.get(i).getLongitude();
                        Log.d("Latitude",String.valueOf(lat));
                        Log.d("Longitude",String.valueOf(lon));
                        LatLng loc = new LatLng(lat,lon);
                        loclist.add(loc);
                        int m = myPrefs.getInt("MARKER",0);
                        if(m == 0) {
                            if (mMap != null && ((user_plotted_data.getChannel_id()) != global_plotted_coordinates.get(i).getChannel_id())) {
                                if (status != 1) {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(loc)
                                            .title("")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                } else {
                                    mMap.addMarker(new MarkerOptions()
                                            .position(loc)
                                            .title("")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }


                }
                SharedPreferences.Editor editor1 = myPrefs.edit();
                editor1.putInt("MARKER",1);
                editor1.apply();

                if(mMap!=null)
                {
                    LatLng lng = new LatLng(user_plotted_data.getLatitude(),user_plotted_data.getLongitude());
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(lng)
                            .title("")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    if(mark == 0){
                        mark++;
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lng,13));
                    }
                }
                int mychannel = user_plotted_data.getChannel_id();
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putInt("TotalChannels",i);
                editor.putInt("MYCHANNELID", mychannel);
                editor.apply();
                /*try {

                    JSONObject obj = new JSONObject(response.body().toString());
                    JSONArray array = obj.getJSONArray("global_plotted_coordinates");
                    int i;
                    for (i =0 ;i < array.length(); i++)
                    {
                        JSONObject jsonObject = array.getJSONObject(i);
                        if(jsonObject.isNull("channel_id") && jsonObject.isNull("status"))
                        {

                            continue;
                        }
                        int channel = jsonObject.getInt("channel_id");
                        int category = jsonObject.getInt("status");
                        SharedPreferences.Editor editor = myPrefs.edit();
                        editor.putInt("CHANNELID"+ String.valueOf(i), channel);
                        editor.putInt("CATEGORY"+ String.valueOf(i), category);
                        editor.apply();
                        Double lat = jsonObject.getDouble("latitude");
                        Double lon = jsonObject.getDouble("longitude");
                        LatLng loc = new LatLng(lat,lon);
                        loclist.add(loc);
                    }
                    JSONObject myobj = obj.getJSONObject("user_plotted_data");
                    int mychannel = myobj.getInt("channel_id");
                    SharedPreferences.Editor editor = myPrefs.edit();
                    editor.putInt("TotalChannels",i);
                    editor.putInt("MYCHANNELID", mychannel);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/

            }

            @Override
            public void onFailure(Call<Coordinates> call, Throwable t) {
                t.printStackTrace();
                super.onFailure(call, t);
            }
        });
    }


}