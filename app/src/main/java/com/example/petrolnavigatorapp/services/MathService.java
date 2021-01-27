package com.example.petrolnavigatorapp.services;

import android.location.Location;

/**
 * Class for mathematical operations.
 */
public class MathService {

    public MathService() {
    }

    /**
     * Calculate distance between two locations on map.
     * @param lat1 Latitude of the first point.
     * @param lon1 Longitude of the first point.
     * @param lat2 Latitude of the second point.
     * @param lon2 Longitude of the second point.
     * @return Distance in meters.
     */
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
