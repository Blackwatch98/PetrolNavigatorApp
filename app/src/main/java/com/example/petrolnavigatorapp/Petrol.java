package com.example.petrolnavigatorapp;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Petrol implements Serializable {
    private String name;
    private LatLng coordinates;
    private HashMap<String,Boolean> availableFuels;
    private List<Fuel> fuels;

    public Petrol()
    {

    }

    public Petrol(String name, LatLng coordinates)
    {
        this.name = name;
        this.coordinates = coordinates;
        this.availableFuels = new HashMap<>();
        availableFuels.put("Benzyna",false);
        availableFuels.put("Diesel",false);
        availableFuels.put("LPG",true);
        availableFuels.put("Etanol",false);
        availableFuels.put("Elektryczny",false);
        availableFuels.put("CNG",true);
        this.fuels = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public List<Fuel> getFuels() {
        return fuels;
    }

    public void setFuels(List<Fuel> fuels) {
        this.fuels = fuels;
    }

    public HashMap<String, Boolean> getAvailableFuels() {
        return availableFuels;
    }

    public void setAvailableFuels(HashMap<String, Boolean> availableFuels) {
        this.availableFuels = availableFuels;
    }
}
