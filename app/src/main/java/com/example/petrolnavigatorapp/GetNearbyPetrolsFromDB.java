package com.example.petrolnavigatorapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GetNearbyPetrolsFromDB {

    private LatLng userLocalization;
    final private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    private List<Petrol> petrolsList;

    GetNearbyPetrolsFromDB(LatLng userLocalization)
    {
        this.userLocalization = userLocalization;
    }

    public void findNearbyPetrols(final float radius)
    {
        petrolsList = new LinkedList<>();
        CollectionReference mRef = fireStore.collection("petrol_stations");
        mRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots != null)
                {
                    for(QueryDocumentSnapshot query : queryDocumentSnapshots)
                    {
                        double lat = Double.parseDouble(query.get("lat").toString());
                        double lon = Double.parseDouble(query.get("lon").toString());
                        int R = 6371;    //meters
                        double distance = Math.sqrt(Math.pow(userLocalization.latitude-lat,2) + Math.pow(userLocalization.longitude-lon,2));
                        System.out.println("rad "+radius);
                        System.out.println("dis " +distance*R);
                        if(distance*R<= radius)
                        {
                            petrolsList.add(SnapshotToPetrol(query));
                        }
                    }
                    for(Petrol petrol : petrolsList)
                    {
                        petrol.displayPetrolData();
                    }
                }
            }
        });
    }

    //NEEDS DATABASE RESTRUCTURE
    private Petrol SnapshotToPetrol(QueryDocumentSnapshot query)
    {
        Petrol petrol = new Petrol(
                query.get("name").toString(),
                Double.parseDouble(query.get("lat").toString()),
                Double.parseDouble(query.get("lat").toString()),
                query.get("address").toString()
        );
        HashMap<String, Boolean> map = (HashMap<String, Boolean>)query.get("availableFuels");
        petrol.setAvailableFuels(map);

        HashMap<Integer, Fuel> mapka = (HashMap<Integer, Fuel>) query.get("fuels");
        List<Fuel> fuels = new LinkedList<>();

        for (Integer num: mapka.keySet()){
            fuels.add(mapka.get(num));
            //System.out.println(key + " " + value);
        }
        petrol.setFuels(fuels);

        return petrol;
    }

    public List<Petrol> getPetrolsList() {
        return petrolsList;
    }
}
