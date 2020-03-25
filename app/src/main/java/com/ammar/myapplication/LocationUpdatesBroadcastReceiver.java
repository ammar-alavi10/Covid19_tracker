package com.ammar.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;


public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";

    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
//                    sendLocationToServer(locations);
//                    List<Location> nearbyLocations = requestLocationsFromServer();
                    String danger = distOfLoc(channel, locations.get(locations.size()-1));
                    LocationResultHelper locationResultHelper = new LocationResultHelper(
                            context, locations);
                    // Save the location data to SharedPreferences.
                    locationResultHelper.saveResults();
                    // Show notification with the location data.
                    locationResultHelper.showNotification(danger);
                    Log.i(TAG, LocationResultHelper.getSavedLocationResult(context));
                }
            }
        }
    }

    private String distOfLoc(List<String> channel, final Location mylocation) {
        //code to get Locations from server
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String[] danger = {"None"};
        for (int i=0 ; i < channel.size() ; i++)
        {

            DocumentReference user = db.collection("main_data").document(channel.get(i));
            user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot doc = task.getResult();
                    String latitude = (String) doc.get("Latitude");
                    String longitude = (String) doc.get("Longitude");
                    if( (String)doc.get("Latitude") != "null" && (String)doc.get("Category") != "Normal") {
                        Double lat = Double.parseDouble(latitude);
                        Double lon = Double.parseDouble(longitude);
                        Location location = new Location("New Location");
                        location.setLatitude(lat);
                        location.setLongitude(lon);
                        double distance = mylocation.distanceTo(location);
                        if (distance < 100.0) {
                            danger[0] = (String) doc.get("category");
                        }
                    }
                }
            });


            if(danger[0] != "None")
                return danger[0];
        }

        return danger[0];

    }
//
//    private void sendLocationToServer(List<Location> locations) {
//        //code to send locations to server
//    }
}
