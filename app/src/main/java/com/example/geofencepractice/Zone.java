package com.example.geofencepractice;

import com.google.android.gms.maps.model.LatLng;

public class Zone {
    String name;
    LatLng loc;

    public Zone(String name, LatLng loc) {
        this.name = name;
        this.loc = loc;
    }
}
