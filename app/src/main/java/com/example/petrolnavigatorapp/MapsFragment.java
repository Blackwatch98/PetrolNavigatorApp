package com.example.petrolnavigatorapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.petrolnavigatorapp.firebase_utils.FirestorePetrolsDB;
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

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, TaskListener {

    private GoogleMap mMap;
    private List<Marker> markers;
    private LocationRequest request;
    private Location mLastLocation;
    private LatLng currentLocation;
    private int radius;
    private FusedLocationProviderClient mFusedProviderClient;
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
            findNearbyPetrols();
        }
    };

    public void findNearbyPetrols() {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + currentLocation.latitude + "," + currentLocation.longitude);
        stringBuilder.append("&radius=" + radius);
        stringBuilder.append("&keyword=" + "petrol");
        stringBuilder.append("&key=" + getResources().getString(R.string.google_places_key));

        String url = stringBuilder.toString();

        Object[] dataTransfer = new Object[4];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = getActivity();
        dataTransfer[3] = this;

//        GetNearbyPetrols2 getNearbyPetrols = new GetNearbyPetrols2();
//        getNearbyPetrols.execute(dataTransfer);


        FirestorePetrolsDB firestorePetrolsDB = new FirestorePetrolsDB(currentLocation, mMap, getContext(), getActivity());
        firestorePetrolsDB.findNearbyPetrols(radius);

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                Intent intent = new Intent(getActivity(), PetrolPopUpActivity.class);
//                intent.putExtra("latitude", marker.getPosition().latitude);
//                intent.putExtra("longitude", marker.getPosition().longitude);
//                getActivity().startActivity(intent);
//                return false;
//            }
//        });

//        GetNearbyPetrols2 getNearbyPetrols = new GetNearbyPetrols2();
//        getNearbyPetrols.execute(dataTransfer);
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

        markers = new LinkedList<>();
        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fireStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listener = (UserLocalizationListener)getContext();

        userDocument = fireStore.collection("users")
                .document(mAuth.getCurrentUser().getUid());

        userDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) documentSnapshot.get("userSettings");
                    radius = Integer.parseInt(map.get("searchRadius").toString()) * 1000;
                }
            }
        });

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        cameraZoom = mMap.getCameraPosition().zoom;

        request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedProviderClient.requestLocationUpdates(request, mLocationCallback,
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
                        if (cameraZoom != mMap.getCameraPosition().zoom) {
                            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mFusedProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            Toast.makeText(getContext(), "Uaktualniam pozycję...", Toast.LENGTH_SHORT).show();
//                                            //currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
//                                            LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
//                                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                                                    ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                                                // TODO: Consider calling
//                                                //    ActivityCompat#requestPermissions
//                                                // here to request the missing permissions, and then overriding
//                                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                //                                          int[] grantResults)
//                                                // to handle the case where the user grants the permission. See the documentation
//                                                // for ActivityCompat#requestPermissions for more details.
//                                                return;
//                                            }
//                                            Location location1 = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                                            double longitude = location1.getLongitude();
//                                            double latitude = location1.getLatitude();
//                                            //Odkomentować na koniec testów
//                                            //currentLocation = new LatLng(latitude,longitude);
                                            cameraZoom = mMap.getCameraPosition().zoom;
                                            mMap.clear();
                                            mMap.addMarker(new MarkerOptions().position(currentLocation).title("User"));
                                            findNearbyPetrols();
                                        }
                                    }
                                });
                            }

                        }
                    }
                });
            }
            else
            {
                checkLocationPermission();
            }
        }
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(getContext())
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                        .create()
                        .show();
            }
            else
            {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    mFusedProviderClient.requestLocationUpdates(request,mLocationCallback,Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            }
            else
            {
                Toast.makeText(getContext(), "Proszę nadać uprawnienia", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onTaskFinish(List<Marker> markers) {
        for(Marker marker : markers)
            this.markers.add(marker);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = new LatLng(location.getLatitude(),location.getLongitude());
    }

    public interface UserLocalizationListener {
        void getUserLocalization(LatLng latLng);
    }
}