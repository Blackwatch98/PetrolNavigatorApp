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

import java.util.List;

public class GoogleDirectionsService {

    private Petrol nearestPetrol;
    private double minimum;
    private GoogleMap mMap;
    private GeoApiContext geoApiContext;
    private Vehicle currentVehicle;
    private boolean ifAbleToPutMarket = false, isFirstStep = true;

    public GoogleDirectionsService(GoogleMap map, GeoApiContext context, Vehicle currentVehicle) {
        mMap = map;
        geoApiContext = context;
        this.currentVehicle = currentVehicle;
    }

    public void compareDistanceToPetrols(List<Petrol> onRoutePetrols, LatLng start, Petrol petrolBefore) {
        if(onRoutePetrols.size() == 0) {
            if(nearestPetrol == null) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(petrolBefore.getLat(),petrolBefore.getLon())).title(petrolBefore.getName()));
                    }
                });
            }
            else{
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(nearestPetrol.getLat(), nearestPetrol.getLon())).title(nearestPetrol.getName()));
                    }
                });
            }
            return;
        }

        Petrol petrol = onRoutePetrols.get(0);
        onRoutePetrols.remove(0);
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng (petrol.getLat(), petrol.getLon());
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.origin(new com.google.maps.model.LatLng(start.latitude,start.longitude));
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                Distance dis = result.routes[0].legs[0].distance;
                if(checkIfVehicleCanReach(dis.inMeters)) {
                    if(isFirstStep || dis.inMeters < minimum) {
                        minimum = dis.inMeters;
                        nearestPetrol = petrol;
                        isFirstStep = false;
                    }
                }
                compareDistanceToPetrols(onRoutePetrols,start, petrolBefore);
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println("Something gone wrong..." + e);
            }
        });
    }

    public boolean checkIfVehicleCanReach(double disInMeters) {
        double remaningFuel = currentVehicle.getReserveFuelLevel();
        double averageFuelConsumption = currentVehicle.getAverageFuelConsumption();

        if(remaningFuel / (averageFuelConsumption/100000) >= disInMeters)
            return true;
        else
            return false;
    }
}
