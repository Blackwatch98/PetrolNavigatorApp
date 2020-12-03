package com.example.petrolnavigatorapp.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.petrolnavigatorapp.Petrol;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyCluster implements ClusterItem {

    private final LatLng position;
    private final int imageIcon;
    private final float price;
    private String title;
    private String snippet;
    private Petrol popedPetrol;

    MyCluster(LatLng latLng, int imageIcon, float price)
    {
        this.position = latLng;
        this. imageIcon = imageIcon;
        this.price = price;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public int getImageIcon() {
        return imageIcon;
    }

    public float getPrice() {
        return price;
    }
}
