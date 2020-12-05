package com.example.petrolnavigatorapp.utils;

import com.example.petrolnavigatorapp.R;
import com.example.petrolnavigatorapp.utils.Fuel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Petrol implements Serializable {
    private String name;
    private double lat, lon;
    private String address;
    private HashMap<String,Boolean> availableFuels;
    private List<Fuel> fuels;

    Petrol(){}

    public Petrol(String name, double lat, double lon, String address)
    {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.availableFuels = new HashMap<>();
        String[] fuelTypes = {"Benzyna", "Diesel", "LPG", "Etanol", "Elektryczny", "CNG"};

        for(String fuelName : fuelTypes)
            this.availableFuels.put(fuelName,false);

        this.fuels = new LinkedList<>();

        String [] names = {"Pb98", "Pb95", "ON", "ON_Ultimate", "LPG", "CNG", "Elektryczny", "Etanol"};
        String[] types = {"Benzyna", "Benzyna", "Diesel", "Diesel", "LPG", "CNG","Elektryczny", "Etanol"};
        int [] icons = {R.drawable.pb98, R.drawable.pb95,R.drawable.on, R.drawable.on_ult,R.drawable.lpg2, R.drawable.cng2,R.drawable.ener,R.drawable.e85};

        for(int i = 0; i < names.length; i++)
            fuels.add(new Fuel(icons[i],"0.00", names[i], types[i]));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public void displayPetrolData()
    {
        System.out.println("Name: " + this.name);
        System.out.println("Coor: " + this.lat + " " + this.lon);
        System.out.println("Address: " + this.address);
        System.out.println("AvailableFuels:");
        for (String name: this.availableFuels.keySet()){
            String key = name.toString();
            String value = availableFuels.get(name).toString();
            System.out.println(key + " " + value);
        }
        System.out.println("Fuels:");
        for(Fuel fuel : fuels)
        {
            System.out.println(fuel.getName());
            System.out.println(fuel.getPrice());
        }
        System.out.println("---------------------------------");
    }
}
