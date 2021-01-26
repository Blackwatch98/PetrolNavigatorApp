package com.example.petrolnavigatorapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.petrolnavigatorapp.firebase_utils.FirestorePetrolsDB;
import com.example.petrolnavigatorapp.interfaces.TaskListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Main fragment in the application. Here all nearby petrol stations are searched for.
 * Right now it is in test mode so by holding finger on another location user can change his location.
 */

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, TaskListener {

    private List<Marker> newPetrolsMarkers; //used only by admin
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private FusedLocationProviderClient mFusedProviderClient;
    private LatLng currentLocation;
    private int searchRadius;
    private float cameraZoom;

    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;
    private DocumentReference userDocument;
    private UserLocalizationListener listener;

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                listener.getUserLocalization(currentLocation);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
            FirestorePetrolsDB firestorePetrolsDB = new FirestorePetrolsDB(currentLocation, mMap, getContext(), getActivity());
            firestorePetrolsDB.findNearbyPetrols(searchRadius);
        }
    };

    /**
     * Creates HTTP request for Google Places API. It needs information about location, search radius, keyword and api key.
     * This function is used only by admin.
     */
    private void requestNearbyPetrols() {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLocation.latitude + "," + currentLocation.longitude);
        stringBuilder.append("&radius=" + searchRadius);
        stringBuilder.append("&keyword=" + "petrol");
        stringBuilder.append("&key=" + getResources().getString(R.string.google_places_key));

        String url = stringBuilder.toString();

        Object[] dataTransfer = new Object[4];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = getActivity();
        dataTransfer[3] = this;

        GetNearbyPetrols getNearbyPetrols = new GetNearbyPetrols();
        getNearbyPetrols.execute(dataTransfer);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent intent = new Intent(getActivity(), PetrolPopUpActivity.class);
                intent.putExtra("latitude", marker.getPosition().latitude);
                intent.putExtra("longitude", marker.getPosition().longitude);
                getActivity().startActivity(intent);
                return false;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        newPetrolsMarkers = new LinkedList<>();
        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listener = (UserLocalizationListener) getContext();

        userDocument = fireStore.collection("users").document(mAuth.getCurrentUser().getUid());

        userDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("userSettings");
                    searchRadius = Integer.parseInt(map.get("searchRadius").toString()) * 1000;
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        cameraZoom = mMap.getCameraPosition().zoom;

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedProviderClient.requestLocationUpdates(locationRequest, mLocationCallback,
                        Looper.myLooper());

                mMap.setMyLocationEnabled(true);
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng2) {
                        currentLocation = latLng2;
                        listener.getUserLocalization(currentLocation);
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            mMap.setMyLocationEnabled(false);
                        mMap.setMyLocationEnabled(true);
                    }
                });

                mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        refreshMap();
                    }
                });
            } else {
                checkLocationPermission();
            }
        }
    }

    /**
     * Refreshes map and all markers' data whenever user zoom camera over map.
     * Right now activity is in test mode so it is basing on marker location not real location.
     */
    private void refreshMap() {
        if (cameraZoom != mMap.getCameraPosition().zoom) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            System.out.println("Location unknown!");
                            return;
                        }

                        //UNCOMMENT BELOW IF APP STOPPED BEING IN TEST MODE
                        /*
                        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        Location location1 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double longitude = location1.getLongitude();
                        double latitude = location1.getLatitude();
                        currentLocation = new LatLng(latitude,longitude);
                        */

                        Toast.makeText(getContext(), "Aktualizuję dane...", Toast.LENGTH_SHORT).show();
                        cameraZoom = mMap.getCameraPosition().zoom;
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(currentLocation).title("User Location"));
                        FirestorePetrolsDB firestorePetrolsDB = new FirestorePetrolsDB(currentLocation, mMap, getContext(), getActivity());
                        firestorePetrolsDB.findNearbyPetrols(searchRadius);
                    }
                });
            }
        }
    }

    /**
     * Check if user has given permission to use location data that is required.
     */
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Give permission")
                        .setMessage("Give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                Toast.makeText(getContext(), "Proszę nadać uprawnienia do korzystania z lokalizacji!", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * It returns markers found by GetNearbyPetrols class to set OnClickListener for them
     * It is used only by admin.
     * @param markers Markers of new petrol stations that has been found
     */
    @Override
    public void onTaskFinish(List<Marker> markers) {
        for (Marker marker : markers)
            this.newPetrolsMarkers.add(marker);
    }

    /**
     * Updates user's location whenever it changes
     * @param location data about user's current location
     */
    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * Interface used to pass data about user's current location to PetrolsListFragment
     */
    public interface UserLocalizationListener {
        void getUserLocalization(LatLng latLng);
    }
}