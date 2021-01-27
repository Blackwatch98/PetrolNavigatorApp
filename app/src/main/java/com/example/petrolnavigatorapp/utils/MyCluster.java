package com.example.petrolnavigatorapp.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Class that represents clusters - custom markers that contains data
 * about station logo and preferred price.
 */
public class MyCluster implements ClusterItem {

    private final LatLng position;
    private final Bitmap imageIcon;
    private final String price;
    private String title;
    private String snippet;
    private Petrol petrol;

    public MyCluster(LatLng latLng, String title, String snippet, Bitmap imageIcon, String price, Petrol petrol)
    {
        this.position = latLng;
        this.title = title;
        this.snippet = snippet;
        this. imageIcon = imageIcon;
        this.price = price;
        this.petrol = petrol;
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

    public Bitmap getImageIcon() {
        return imageIcon;
    }

    public String getPrice() {
        return price;
    }


}
