package com.example.petrolnavigatorapp.services;

import android.location.Location;

import com.example.petrolnavigatorapp.firebase_utils.FirestorePetrolsDB;
import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class PolylineService {

    private Vehicle currentVehicle;
    private Polyline currentRoute;

    public PolylineService (Vehicle vehicle, Polyline polyline) {
        this.currentVehicle = vehicle;
        this.currentRoute = polyline;
    }

    public LinkedList<LatLng> getFuelReservePointOnRoute() {
        double distanceTillFuelReserve = getFuelReserveDistance();
        double route = 0;
        LinkedList<LatLng> allReservePoints = new LinkedList();

        LatLng point1 = currentRoute.getPoints().get(0);
        for(LatLng point2 : currentRoute.getPoints()) {
            route += getDistance(point1.latitude, point1.longitude, point2.latitude, point2.longitude);
            if(route >= distanceTillFuelReserve) {
                allReservePoints.add(point2);
                currentVehicle.setCurrentFuelLevel(currentVehicle.getTankCapacity());
                distanceTillFuelReserve = getFuelReserveDistance();
                route = 0;
            }
            point1 = point2;
        }

        return allReservePoints;
    }

    private double getFuelReserveDistance() {
        double currentFuelLevel = currentVehicle.getCurrentFuelLevel();
        double averageFuelConsumption = currentVehicle.getAverageFuelConsumption();
        double reserveFuelLevel = currentVehicle.getReserveFuelLevel();

        return (currentFuelLevel - reserveFuelLevel)/averageFuelConsumption * 100 * 1000;
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2)
    {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        double distance = location1.distanceTo(location2);

        return distance;
    }
}
