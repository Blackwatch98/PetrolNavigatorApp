package com.example.petrolnavigatorapp.firebase_utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;

import androidx.annotation.NonNull;

import com.example.petrolnavigatorapp.FindPetrolsListener;
import com.example.petrolnavigatorapp.MyClusterManagerRenderer;
import com.example.petrolnavigatorapp.PetrolPopUpActivity;
import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.MyCluster;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FirestorePetrolsDB {

    private LatLng userLocalization;
    private GoogleMap mMap;
    private Context context;
    final private FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
    final private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private MyFirebaseStorage sRef;
    private List<Petrol> petrolsList;
    private Activity activity;
    private FindPetrolsListener listener;

    public FirestorePetrolsDB() {

    }

    public FirestorePetrolsDB(LatLng userLocalization, GoogleMap mMap, Context context, Activity activity) {
        this.userLocalization = userLocalization;
        this.mMap = mMap;
        this.context = context;
        this.activity = activity;
        listener = (FindPetrolsListener)context;
    }

    public void findNearbyPetrols(final float radius) {
        fireStore.collection("users").document(mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (!documentSnapshot.exists())
                    return;
                HashMap<String, Object> data = (HashMap<String, Object>) documentSnapshot.get("userSettings");
                final String typePref, fuelPref;
                typePref = data.get("prefFuelType").toString();
                fuelPref = data.get("prefFuel").toString();
                listener.getUserPrefs(typePref, fuelPref);

                petrolsList = new LinkedList<>();
                final CollectionReference mRef = fireStore.collection("petrol_stations");
                mRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots != null) {
                            final MarkerManager markerManager = new MarkerManager(mMap);
                            final ClusterManager<MyCluster> manager = new ClusterManager<MyCluster>(context, mMap, markerManager);
                            final ClusterManager<MyCluster> manager_note_text = new ClusterManager<MyCluster>(context, mMap, markerManager);
                            MyClusterManagerRenderer managerRenderer = new MyClusterManagerRenderer(
                                    context,
                                    mMap,
                                    manager
                            );
                            MyClusterManagerRenderer managerRenderer2 = new MyClusterManagerRenderer(
                                    context,
                                    mMap,
                                    manager_note_text,
                                    true
                            );
                            manager.setRenderer(managerRenderer);
                            manager_note_text.setRenderer(managerRenderer2);

                            for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                                final double lat = Double.parseDouble(query.get("lat").toString());
                                final double lon = Double.parseDouble(query.get("lon").toString());
                                Location location1 = new Location("");
                                location1.setLatitude(lat);
                                location1.setLongitude(lon);
                                Location location2 = new Location("");
                                location2.setLatitude(userLocalization.latitude);
                                location2.setLongitude(userLocalization.longitude);
                                double distance = location1.distanceTo(location2);

                                if (distance <= radius) {
                                    final Petrol petrol = SnapshotToPetrol(query);
                                    petrolsList.add(petrol);

                                    Fuel fuel = null;
                                    if (fuelPref.equals("Wszystko")) {
                                        if (typePref.equals("Wszystko")) {
                                            for (String name : petrol.getAvailableFuels().keySet()) {
                                                Boolean value = petrol.getAvailableFuels().get(name);
                                                if (value) {
                                                    for (Fuel f : petrol.getFuels()) {
                                                        if (f.getType().equals(name)) {
                                                            fuel = f;
                                                            break;
                                                        }
                                                    }
                                                    break;
                                                }
                                            }
                                        } else {
                                            for (Fuel f : petrol.getFuels()) {
                                                if (f.getType().equals(typePref)) {
                                                    fuel = f;
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        for (Fuel f : petrol.getFuels()) {
                                            if (f.getName().equals(fuelPref)) {
                                                fuel = f;
                                                break;
                                            }
                                        }
                                    }
                                    final Fuel finalFuel = fuel;
                                    sRef = new MyFirebaseStorage();
                                    try {
                                        final File localFile = File.createTempFile("petrol_icon", "png");
                                        sRef.getPetrolIconRef(petrol.getName()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                final MyCluster newClusterMarker;
                                                if (finalFuel == null || finalFuel.getPrice().equals("0.00")) {
                                                    newClusterMarker = new MyCluster(
                                                            new LatLng(lat, lon),
                                                            "",
                                                            "",
                                                            bitmap,
                                                            null,
                                                            petrol
                                                    );
                                                    manager_note_text.addItem(newClusterMarker);
                                                } else {
                                                    newClusterMarker = new MyCluster(
                                                            new LatLng(lat, lon),
                                                            "",
                                                            "",
                                                            bitmap,
                                                            finalFuel.getPrice(),
                                                            petrol
                                                    );
                                                    manager.addItem(newClusterMarker);
                                                }
                                                mMap.setOnMarkerClickListener(markerManager);
                                                manager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyCluster>() {
                                                    @Override
                                                    public boolean onClusterItemClick(MyCluster item) {
                                                        Intent intent = new Intent(activity, PetrolPopUpActivity.class);
                                                        intent.putExtra("latitude", item.getPosition().latitude);
                                                        intent.putExtra("longitude", item.getPosition().longitude);
                                                        activity.startActivity(intent);
                                                        return true;
                                                    }
                                                });
                                                manager_note_text.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyCluster>() {
                                                    @Override
                                                    public boolean onClusterItemClick(MyCluster item) {
                                                        Intent intent = new Intent(activity, PetrolPopUpActivity.class);
                                                        intent.putExtra("latitude", item.getPosition().latitude);
                                                        intent.putExtra("longitude", item.getPosition().longitude);
                                                        activity.startActivity(intent);
                                                        return true;
                                                    }
                                                });
                                                MarkerManager.Collection collection = manager.getMarkerManager().newCollection();
//                                                markerManager.newCollection().setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//                                                    @Override
//                                                    public boolean onMarkerClick(Marker marker) {
//                                                        Intent intent = new Intent(activity, PetrolPopUpActivity.class);
//                                                        intent.putExtra("latitude", marker.getPosition().latitude);
//                                                        intent.putExtra("longitude", marker.getPosition().longitude);
//                                                        activity.startActivity(intent);
//                                                        return true;
//                                                    }
//                                                });
                                                manager.cluster();
                                                manager_note_text.cluster();
                                            }
                                        });
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                            listener.getPetrolsList(petrolsList);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getMessage();
            }
        });
    }

    public Petrol SnapshotToPetrol(QueryDocumentSnapshot query) {
        Petrol petrol = new Petrol(
                query.get("name").toString(),
                Double.parseDouble(query.get("lat").toString()),
                Double.parseDouble(query.get("lon").toString()),
                query.get("address").toString()
        );
        HashMap<String, Boolean> map = (HashMap<String, Boolean>) query.get("availableFuels");
        petrol.setAvailableFuels(map);

        List<HashMap<String, Object>> lista = (List<HashMap<String, Object>>) query.get("fuels");
        List<Fuel> fuels = new LinkedList<>();

        for (HashMap<String, Object> item : lista) {
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
