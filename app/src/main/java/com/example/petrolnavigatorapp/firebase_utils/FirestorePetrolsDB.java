package com.example.petrolnavigatorapp.firebase_utils;

import android.location.Location;

import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FirestorePetrolsDB {

    private LatLng userLocalization;
    private GoogleMap mMap;
    final private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    private List<Petrol> petrolsList;

    public FirestorePetrolsDB()
    {

    }

    public FirestorePetrolsDB(LatLng userLocalization, GoogleMap mMap)
    {
        this.userLocalization = userLocalization;
        this.mMap = mMap;
    }

    public void findNearbyPetrols(final float radius)
    {
        petrolsList = new LinkedList<>();
        final CollectionReference mRef = fireStore.collection("petrol_stations");
        mRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null)
                {
                    for(QueryDocumentSnapshot query : queryDocumentSnapshots)
                    {
                        double lat = Double.parseDouble(query.get("lat").toString());
                        double lon = Double.parseDouble(query.get("lon").toString());
                        //int R = 6371;    //meters
                       // double distance = Math.sqrt(Math.pow(userLocalization.latitude-lat,2) + Math.pow(userLocalization.longitude-lon,2));
                        //System.out.println("rad "+radius);
                        //System.out.println("dis " +distance*R);
                        Location location1 = new Location("");
                        location1.setLatitude(lat);
                        location1.setLongitude(lon);
                        Location location2 = new Location("");
                        location2.setLatitude(userLocalization.latitude);
                        location2.setLongitude(userLocalization.longitude);
                        double distance = location1.distanceTo(location2);

                        System.out.println("Radius: " + radius + "dis: " + distance);
                        if(distance <= radius*2)
                        {
                            Petrol petrol = SnapshotToPetrol(query);
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.title(petrol.getName()+"," + petrol.getAddress());
                            markerOptions.position(new LatLng(petrol.getLat(),petrol.getLon()));

                            mMap.addMarker(markerOptions);
                        }
                    }
                }
            }
        });
    }

    //NEEDS DATABASE RESTRUCTURE
    public Petrol SnapshotToPetrol(QueryDocumentSnapshot query)
    {
        Petrol petrol = new Petrol(
                query.get("name").toString(),
                Double.parseDouble(query.get("lat").toString()),
                Double.parseDouble(query.get("lon").toString()),
                query.get("address").toString()
        );
        HashMap<String, Boolean> map = (HashMap<String, Boolean>)query.get("availableFuels");
        petrol.setAvailableFuels(map);

        List<HashMap<String, Object>> lista = (List<HashMap<String, Object>>) query.get("fuels");
        List<Fuel> fuels = new LinkedList<>();

        for(HashMap<String, Object> item : lista) {
            fuels.add(new Fuel(
                    Integer.parseInt(item.get("icon").toString()),
                    item.get("price").toString(),
                    item.get("name").toString(),
                    item.get("type").toString(),
                    Integer.parseInt(item.get("reportCounter").toString()),
                    item.get("lastReportDate").toString()
            ));
        }
        petrol.setFuels(fuels);

        return petrol;
    }
}
