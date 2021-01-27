package com.example.petrolnavigatorapp.firebase_utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import com.example.petrolnavigatorapp.interfaces.FindPetrolsListener;
import com.example.petrolnavigatorapp.MyClusterManagerRenderer;
import com.example.petrolnavigatorapp.PetrolPopUpActivity;
import com.example.petrolnavigatorapp.services.GoogleDirectionsService;
import com.example.petrolnavigatorapp.services.MathService;
import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.MyCluster;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.maps.GeoApiContext;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * This class biggest operations with database.
 * 1. Placing custom markers with petrol station logo and fuel price.
 * 2. Finding all the petrol on the specified route.
 */
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
    private Vehicle currentVehicle;

    public FirestorePetrolsDB() {

    }

    public FirestorePetrolsDB(LatLng userLocalization, GoogleMap mMap, Context context, Activity activity) {
        this.userLocalization = userLocalization;
        this.mMap = mMap;
        this.context = context;
        this.activity = activity;
        listener = (FindPetrolsListener) context;
    }

    public FirestorePetrolsDB(GoogleMap mMap, Context context, Activity activity, Vehicle currentVehicle) {
        this.mMap = mMap;
        this.context = context;
        this.activity = activity;
        this.currentVehicle = currentVehicle;
    }

    /**
     * This huge function is responsible for preparing markers on the map.
     * There are two types of markers: with and without price.
     * Markers are clusters which are custom type of markers.
     * Because it uses many common values it was hard to split into smaller functions.
     *
     * @param radius Radius of searching petrol stations that user has specified earlier.
     */
    public void findNearbyPetrols(final float radius) {
        if (context == null) {
            return;
        }

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
                        if (queryDocumentSnapshots == null) {
                            System.out.println("Query was empty...");
                            return;
                        }

                        //PREPARING TWO CLUSTER TYPES WITH AND WITHOUT PRICE
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
                            MathService mathService = new MathService();
                            double distance = mathService.getDistanceBetweenTwoPoints(lat, lon, userLocalization.latitude, userLocalization.longitude);

                            if (distance <= radius) {
                                final Petrol petrol = SnapshotToPetrol(query);
                                petrolsList.add(petrol);

                                final Fuel finalFuel = getPreferredFuel(typePref, fuelPref, petrol);
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
                                                    intent.putExtra("userLat", userLocalization.latitude);
                                                    intent.putExtra("userLon", userLocalization.longitude);
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
                                                    intent.putExtra("userLat", userLocalization.latitude);
                                                    intent.putExtra("userLon", userLocalization.longitude);
                                                    intent.putExtra("latitude", item.getPosition().latitude);
                                                    intent.putExtra("longitude", item.getPosition().longitude);
                                                    activity.startActivity(intent);
                                                    return true;
                                                }
                                            });
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
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.getMessage();
            }
        });
    }

    /**
     * Gets preferred fuel from HashMap.
     *
     * @param typePref Type of preferred fuel.
     * @param fuelPref Name of preferred fuel.
     * @param petrol   Petrol that is checked right now.
     * @return Found fuel object.
     */
    private Fuel getPreferredFuel(String typePref, String fuelPref, Petrol petrol) {
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
        return fuel;
    }

    /**
     * This function search for all petrol stations that are on route found by Google Directions API.
     *
     * @param routePoints      All coordinate points which route is made of.
     * @param reserveFuelPoint Location of reserve fuel point.
     * @param startLocation    Location where route starts from.
     * @param destination      Location where route ends.
     * @param geoApiContext    Context to GeoApi.
     */
    public void getPetrolsOnRoute(List<LatLng> routePoints, LatLng reserveFuelPoint, LatLng startLocation, LatLng destination, GeoApiContext geoApiContext) {
        if (context != null)
            fireStore.collection("petrol_stations").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    List<Petrol> petrolsOnRoute = new LinkedList<>();
                    MathService mathService = new MathService();
                    for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                        final double lat = Double.parseDouble(query.get("lat").toString());
                        final double lon = Double.parseDouble(query.get("lon").toString());
                        for (LatLng latLng : routePoints) {
                            double distance = mathService.getDistanceBetweenTwoPoints(lat, lon, latLng.latitude, latLng.longitude);
                            if (distance <= 500)
                                petrolsOnRoute.add(SnapshotToPetrol(query));
                        }

                    }

                    Petrol closestToStart = petrolsOnRoute.get(0);
                    List<Petrol> closestToEnd = new LinkedList<>();
                    double min = mathService.getDistanceBetweenTwoPoints(reserveFuelPoint.latitude, reserveFuelPoint.longitude, startLocation.latitude, startLocation.longitude);
                    double min2 = mathService.getDistanceBetweenTwoPoints(reserveFuelPoint.latitude, reserveFuelPoint.longitude, destination.latitude, destination.longitude);
                    double fuelReserveFromStart = min;
                    double fuelReserveFromEnd = min2;

                    for (Petrol petrol : petrolsOnRoute) {
                        double distanceFromStart = mathService.getDistanceBetweenTwoPoints(petrol.getLat(), petrol.getLon(), startLocation.latitude, startLocation.longitude);
                        double distanceFromEnd = mathService.getDistanceBetweenTwoPoints(petrol.getLat(), petrol.getLon(), destination.latitude, destination.longitude);
                        double distanceFromFuelReserve = mathService.getDistanceBetweenTwoPoints(reserveFuelPoint.latitude, reserveFuelPoint.longitude, petrol.getLat(), petrol.getLon());
                        if (distanceFromStart < fuelReserveFromStart) {
                            if (distanceFromFuelReserve < min) {
                                closestToStart = petrol;
                                min = distanceFromFuelReserve;
                            }
                        }
                        if (distanceFromEnd < fuelReserveFromEnd) {
                            if (distanceFromFuelReserve < min2) {
                                if (closestToEnd.size() > 2)
                                    continue;
                                closestToEnd.add(petrol);
                                min2 = distanceFromFuelReserve;
                            }
                        }
                    }

                    GoogleDirectionsService service = new GoogleDirectionsService(mMap, geoApiContext, currentVehicle);
                    service.compareDistanceToPetrols(closestToEnd, reserveFuelPoint, closestToStart);
                }
            });
    }

    /**
     * Converts QueryDocumentSnapshot object to Petrol object.
     *
     * @param query QueryDocumentSnapshot object.
     * @return Petrol object.
     */
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
            String lastReportDate = null;

            if (item.get("lastReportDate") != null) {
                lastReportDate = item.get("lastReportDate").toString();
            }

            fuels.add(new Fuel(
                    Integer.parseInt(item.get("icon").toString()),
                    item.get("price").toString(),
                    item.get("name").toString(),
                    item.get("type").toString(),
                    lastReportDate
            ));
        }
        petrol.setFuels(fuels);

        return petrol;
    }
}
