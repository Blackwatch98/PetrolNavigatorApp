package com.example.petrolnavigatorapp.interfaces;

import com.google.android.gms.maps.model.Marker;

import java.util.List;

public interface TaskListener {
    void onTaskFinish(List<Marker> markers);
}
