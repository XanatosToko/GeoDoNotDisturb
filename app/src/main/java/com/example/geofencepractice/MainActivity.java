package com.example.geofencepractice;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    private static final int PERMISSIONS_REQUEST_LOCATION = 1527;
    private static final int PERMISSIONS_REQUEST_BACKGROUND = 1528;

    private static final float GEOFENCE_RADIUS = 100;

    private Boolean geoActive;

    private GeofencingClient geofencingClient;
    private List<String> geofenceIds;
    private List<Zone> geofenceZones;
    private GeofenceHelper geofenceHelper;


    GoogleMap mMap;

    public static final String EXTRA_QUESTION = "question";
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_LAT = "latitude";
    public static final String EXTRA_LONG = "longitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
//            }
//        });

        //Reload any saved data, handles defaults if they haven't been set
        loadData();
        saveData();

        Switch onOff = findViewById(R.id.switch1);
        onOff.setChecked(geoActive);
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    Toast.makeText(getApplicationContext(), "Turning ON Geofencing", Toast.LENGTH_SHORT).show();
                    geoActive = true;
                    activateGeoFencingStuff();
                } else {
                    Toast.makeText(getApplicationContext(), "Turning OFF Geofencing", Toast.LENGTH_SHORT).show();
                    geoActive = false;
                    if(!geofenceIds.isEmpty()){
                        geofencingClient.removeGeofences(geofenceIds);
                    }
                    NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                    if(mNotificationManager != null)
                        if(mNotificationManager.isNotificationPolicyAccessGranted())
                            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                }
                saveData();
            }
        });

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment_id);
        mapFragment.getMapAsync(this);
    }

    private void activateGeoFencingStuff(){
        //Clear any old geofences
        if(!geofenceIds.isEmpty()){
            geofencingClient.removeGeofences(geofenceIds);
        }

        //For background location access, required for SDK 29 and higher
        if(Build.VERSION.SDK_INT >= 29) {
            //Need background permission
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                for (Zone zone : geofenceZones) addGeofence(zone.name, zone.loc);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Please give permission", Toast.LENGTH_LONG);
                    toast.show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSIONS_REQUEST_BACKGROUND);
                } else {
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        PERMISSIONS_REQUEST_BACKGROUND);
                }
            }
        } else {
            for (Zone zone : geofenceZones) addGeofence(zone.name, zone.loc);
        }
    }

    private void addGeofence(String id, LatLng latLng) {
        Geofence geofence = geofenceHelper.getGeofence(id, latLng, GEOFENCE_RADIUS,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Geofences added
                        Log.d(TAG, "Geofence successfully added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add geofences
                        Log.d(TAG, "Geofence failed to add");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the related task you need to do.
                mMap.setMyLocationEnabled(true);
            } else {
                // permission denied, boo! Disable the functionality that depends on this permission.
            }
            return;
        }

        if(requestCode == PERMISSIONS_REQUEST_BACKGROUND) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "You can now add geofences", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Background location access required before you can add geofences", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_defaults) {
            geofencingClient.removeGeofences(geofenceIds);
            geofenceIdsInit();
            geofenceZonesInit();
            saveData();
            //Reset and draw on map
            mMap.clear();
            drawMarkers();
            //Reset do-not-disturb and geofences to current list if active
            resetDndMode();
            if(geoActive) activateGeoFencingStuff();
        }

        if (id == R.id.action_permission) {
            startActivityForResult(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS), 0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        LatLng center = new LatLng(40.244190, -111.643299);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));

        drawMarkers();

        enableUserLocation();

        map.setOnMarkerClickListener(this);
        map.setOnMapLongClickListener(this);
    }

    private void enableUserLocation() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Show dialog why permission is needed
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    private void drawMarkers() {
        for(Zone zone : geofenceZones) {
            mMap.addMarker(new MarkerOptions().position(zone.loc).title(zone.name));
            mMap.addCircle(new CircleOptions().center(zone.loc).radius(GEOFENCE_RADIUS).fillColor(0x40FF0000).strokeColor(0x405e5e5e));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(geofenceIds.contains(marker.getTitle())) {
            Intent i = new Intent(getApplicationContext(), PopActivity.class);
            i.putExtra(EXTRA_QUESTION, getString(R.string.removing_question));
            i.putExtra(EXTRA_NAME, marker.getTitle());
            startActivityForResult(i, R.string.removing_question);
        }

        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Intent i = new Intent(getApplicationContext(), PopActivity.class);
        i.putExtra(EXTRA_QUESTION, getString(R.string.adding_question));
        i.putExtra(EXTRA_LAT, latLng.latitude);
        i.putExtra(EXTRA_LONG, latLng.longitude);
        startActivityForResult(i, R.string.adding_question);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == R.string.removing_question) {
            if (resultCode == RESULT_OK) {
                String zoneToRemove = data.getStringExtra(EXTRA_NAME);
                for(Zone zone : geofenceZones) {
                    if(zone.name.equals(zoneToRemove)) {
                        geofenceIds.remove(zone.name);
                        geofenceZones.remove(zone);
                        Toast.makeText(this, "Successfully removed zone", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                saveData();
                //Reset and draw on map
                mMap.clear();
                drawMarkers();
                //Make sure geofence is removed if active
                if(geoActive) {
                    List<String> removalZone = new ArrayList<>();
                    removalZone.add(zoneToRemove);
                    geofencingClient.removeGeofences(removalZone);
                    //Reset do-not-disturb and geofences to current list if active
                    resetDndMode();
                    activateGeoFencingStuff();
                }
            }
        }

        if (requestCode == R.string.adding_question) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Adding a zone", Toast.LENGTH_SHORT).show();
                String zoneName = data.getStringExtra(EXTRA_NAME);
                double zoneLat = data.getDoubleExtra(EXTRA_LAT, 0);
                double zoneLong = data.getDoubleExtra(EXTRA_LONG, 0);
                geofenceZones.add(new Zone(zoneName, new LatLng(zoneLat, zoneLong)));
                geofenceIds.add(zoneName);
                saveData();
                //Reset and draw on map
                mMap.clear();
                drawMarkers();
                //Reset do-not-disturb and geofences to current list if active
                resetDndMode();
                if(geoActive) activateGeoFencingStuff();
            }
        }
    }

    private void resetDndMode() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager != null)
            if(mNotificationManager.isNotificationPolicyAccessGranted())
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(geofenceZones);
        String json2 = gson.toJson(geofenceIds);
        String json3 = gson.toJson(geoActive);
        editor.putString("zone list", json);
        editor.putString("id list", json2);
        editor.putString("active bool", json3);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("zone list", null);
        String json2 = sharedPreferences.getString("id list", null);
        String json3 = sharedPreferences.getString("active bool", gson.toJson(false));
        Type type = new TypeToken<ArrayList<Zone>>() {}.getType();
        Type type2 = new TypeToken<ArrayList<String>>() {}.getType();
        Type type3 = new TypeToken<Boolean>() {}.getType();
        geofenceZones = gson.fromJson(json, type);
        geofenceIds = gson.fromJson(json2, type2);
        geoActive = gson.fromJson(json3, type3);

        //Initialize defaults if necessary
        if (geofenceZones == null) {
            geofenceZonesInit();
        }
        if (geofenceIds == null) {
            geofenceIdsInit();
        }
    }

    private void geofenceZonesInit() {
        geofenceZones = new ArrayList<>();
        geofenceZones.add(new Zone("Classroom", new LatLng(40.249441, -111.653590)));
        geofenceZones.add(new Zone("Home", new LatLng(40.244190, -111.643299)));
    }

    private void geofenceIdsInit() {
        geofenceIds = new ArrayList<>();
        geofenceIds.add("Classroom");
        geofenceIds.add("Home");
    }
}
