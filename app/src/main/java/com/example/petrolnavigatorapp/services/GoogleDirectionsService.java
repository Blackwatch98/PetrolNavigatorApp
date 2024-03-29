package com.example.petrolnavigatorapp.services;

import android.os.Handler;
import android.os.Looper;

import com.example.petrolnavigatorapp.utils.Petrol;
import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.Distance;

import java.util.LinkedList;
import java.util.List;

/**
 * Service that holds all actions in Google Directions API.
 */
public class GoogleDirectionsService {

    private List<Petrol> nextPetrolsList;
    private GoogleMap mMap;
    private GeoApiContext geoApiContext;
    private Vehicle currentVehicle;

    public GoogleDirectionsService(GoogleMap map, GeoApiContext geoContext, Vehicle currentVehicle) {
        mMap = map;
        geoApiContext = geoContext;
        this.currentVehicle = currentVehicle;
        nextPetrolsList = new LinkedList<>();
    }

    /**
     * Using recursion marks all petrol stations vehicle is able to reach.
     * These int the rest of journey route are first to be taken into account.
     * If none found it will mark first before reserve fuel point.
     * @param onRoutePetrols List of all petrol stations on the route.
     * @param start The point that route starts from.
     * @param petrolBefore The first petrol station before reserve fuel point.
     */
    public void compareDistanceToPetrols(List<Petrol> onRoutePetrols, LatLng start, Petrol petrolBefore) {
        if (onRoutePetrols.size() == 0) {
            Handler handler = new Handler(Looper.getMainLooper());
            if (nextPetrolsList == null) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(petrolBefore.getLat(), petrolBefore.getLon())).title(petrolBefore.getName()));
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (Petrol petrol : nextPetrolsList)
                            mMap.addMarker(new MarkerOptions().position(new LatLng(petrol.getLat(), petrol.getLon())).title(petrol.getName()));
                    }
                });
            }
            return;
        }

        Petrol petrol = onRoutePetrols.get(0);
        onRoutePetrols.remove(0);
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(petrol.getLat(), petrol.getLon());
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.origin(new com.google.maps.model.LatLng(start.latitude, start.longitude));
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Distance dis = result.routes[0].legs[0].distance;
                if (checkIfVehicleCanReach(dis.inMeters))
                    nextPetrolsList.add(petrol);

                compareDistanceToPetrols(onRoutePetrols, start, petrolBefore);
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println("Something gone wrong... " + e);
            }
        });
    }

    /**
     * Checks if the vehicle is able to reach destinations with current fuel level.
     * @param disInMeters Distance to go by vehicle.
     * @return False if vehicle cannot reach destination, true if is able to.
     */
    public boolean checkIfVehicleCanReach(double disInMeters) {
        double remainingFuel = currentVehicle.getReserveFuelLevel();
        double averageFuelConsumption = currentVehicle.getAverageFuelConsumption();

        if (remainingFuel / (averageFuelConsumption / 100000) >= disInMeters)
            return true;

        return false;
    }
}
