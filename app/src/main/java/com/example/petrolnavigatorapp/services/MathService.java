package com.example.petrolnavigatorapp.services;

import android.location.Location;

public class MathService {

    public MathService(){
    }

    public double getDistanceBetweenTwoPoints(double lat1, double lon1, double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);

        return location1.distanceTo(location2);
    }
}
