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

    Petrol(){}

    public Petrol(String name, LatLng coordinates)
    {
        this.name = name;
        this.coordinates = coordinates;
        this.availableFuels = new HashMap<>();
        String[] fuelTypes = {"Benzyna", "Diesel", "LPG", "Etanol", "Elektryczny", "CNG"};

        for(String fuelName : fuelTypes)
            this.availableFuels.put(fuelName,false);

        this.fuels = new LinkedList<>();

        String [] names = {"Pb98", "Pb95", "ON", "LPG", "CNG", "Elektryczny", "Etanol"};
        String[] fuelStates = {"fluid", "fluid", "fluid", "gas", "gas","unconv", "unconv"};
        int [] icons = {R.drawable.benz, R.drawable.benz, R.drawable.diesel,
                R.drawable.lpg, R.drawable.cng, R.drawable.elektr,  R.drawable.etan};

        for(int i = 0; i < names.length; i++)
            fuels.add(new Fuel(icons[i],"0.00", names[i], fuelStates[i]));
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
