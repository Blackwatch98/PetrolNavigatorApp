package com.example.petrolnavigatorapp.services;

import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.LinkedList;

/**
 * Service for operations on routes.
 */
public class PolylineService {

    final private Vehicle currentVehicle;
    final private Polyline currentRoute;

    public PolylineService(Vehicle vehicle, Polyline polyline) {
        this.currentVehicle = vehicle;
        this.currentRoute = polyline;
    }

    /**
     * Gets all points on route where fuel reached reserve level.
     * @return List of reserve fuel points.
     */
    public LinkedList<LatLng> getFuelReservePointOnRoute() {
        double distanceTillFuelReserve = getFuelReserveDistance();
        double route = 0;
        LinkedList<LatLng> allReservePoints = new LinkedList();
        MathService mathService = new MathService();
        LatLng point1 = currentRoute.getPoints().get(0);
        for (LatLng point2 : currentRoute.getPoints()) {
            route += mathService.getDistanceBetweenTwoPoints(point1.latitude, point1.longitude, point2.latitude, point2.longitude);
            if (route >= distanceTillFuelReserve) {
                allReservePoints.add(point2);
                currentVehicle.setCurrentFuelLevel(currentVehicle.getTankCapacity());
                distanceTillFuelReserve = getFuelReserveDistance();
                route = 0;
            }
            point1 = point2;
        }

        return allReservePoints;
    }

    /**
     * Calculate route length from reserve till no more fuel left.
     * @return
     */
    private double getFuelReserveDistance() {
        double currentFuelLevel = currentVehicle.getCurrentFuelLevel();
        double averageFuelConsumption = currentVehicle.getAverageFuelConsumption();
        double reserveFuelLevel = currentVehicle.getReserveFuelLevel();

        return (currentFuelLevel - reserveFuelLevel) / averageFuelConsumption * 100 * 1000;
    }
}
