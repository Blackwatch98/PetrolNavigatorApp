package com.example.petrolnavigatorapp;

import java.io.Serializable;

public class Fuel {
    private int icon;
    private String name;
    private String type;
    private String price;

    Fuel(int i, String price, String name, String type)
    {
        this.icon = i;
        this.name = name;
        this.price = price;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
