package com.example.petrolnavigatorapp.utils;

import java.io.Serializable;

public class Vehicle implements Serializable {

    private String name;
    private double tankCapacity; //in liters
    private double averageFuelConsumption;
    private int fuelTypeId;
    private double currentFuelLevel;
    private double reserveFuelLevel;

    public Vehicle(String name, double tankCapacity, double averageFuelConsumption, int fuelTypeId, double currentFuelLevel, double reserveFuelLevel) {
        this.name = name;
        this.tankCapacity = tankCapacity;
        this.averageFuelConsumption = averageFuelConsumption;
        this.fuelTypeId = fuelTypeId;
        this.currentFuelLevel = currentFuelLevel;
        this.reserveFuelLevel = reserveFuelLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTankCapacity() {
        return tankCapacity;
    }

    public void setTankCapacity(double tankCapacity) {
        this.tankCapacity = tankCapacity;
    }

    public double getAverageFuelConsumption() {
        return averageFuelConsumption;
    }

    public void setAverageFuelConsumption(double averageFuelConsumption) {
        this.averageFuelConsumption = averageFuelConsumption;
    }

    public int getFuelTypeId() {
        return fuelTypeId;
    }

    public void setFuelTypeId(int fuelTypeId) {
        this.fuelTypeId = fuelTypeId;
    }

    public double getCurrentFuelLevel() {
        return currentFuelLevel;
    }

    public void setCurrentFuelLevel(double currentFuelLevel) {
        this.currentFuelLevel = currentFuelLevel;
    }

    public double getReserveFuelLevel() {
        return reserveFuelLevel;
    }

    public void setReserveFuelLevel(double reserveFuelLevel) {
        this.reserveFuelLevel = reserveFuelLevel;
    }
}
