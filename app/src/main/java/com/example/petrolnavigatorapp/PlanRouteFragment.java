package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.petrolnavigatorapp.firebase_utils.FirestorePetrolsDB;
import com.example.petrolnavigatorapp.services.PolylineService;
import com.example.petrolnavigatorapp.utils.PolylineData;
import com.example.petrolnavigatorapp.utils.Vehicle;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlanRouteFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedProviderClient;
    protected LocationManager locationManager;
    private LatLng currentLocation;
    private Location mLastLocation;
    private LocationRequest request;
    private SearchView searchView;
    private GeoApiContext geoApiContext;
    private List<PolylineData> polylineDataList = new ArrayList<>();

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            super.onLocationResult(locationResult);
            for (Location location : locationResult.getLocations()) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnPolylineClickListener(this);
        request = new LocationRequest();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedProviderClient.requestLocationUpdates(request, mLocationCallback,
                        Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_plan_route, container, false);
        searchView = view.findViewById(R.id.searchView);
        System.out.println(searchView);
        searchView = getActivity().findViewById(R.id.searchView);
        System.out.println(searchView);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if (geoApiContext == null) {
            geoApiContext =  new GeoApiContext.Builder()
                    .apiKey(getString(R.string.google_maps_key))
                    .build();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if(location != null && location != "") {
                    Geocoder geocoder = new Geocoder(view.getContext());
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                    System.out.println("DEBUGGER");
                    calculateDirections(marker);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
    }

    private void calculateDirections(Marker marker) {
        System.out.println("marker " + marker.getPosition().latitude + " " + marker.getPosition().longitude);
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng (marker.getPosition().latitude, marker.getPosition().longitude);
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(true);
        directions.origin(new com.google.maps.model.LatLng(currentLocation.latitude,currentLocation.longitude));
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                //result data
                System.out.println("dane");
                System.out.println(result.routes.length + " " + result.routes[0].legs.length);
                System.out.println(result.routes[0].legs[0].distance+"km");
                addPolyLines(result);
            }

            @Override
            public void onFailure(Throwable e) {
                System.out.println("NIE UDAŁO SIE" + e);
            }
        });
    }

    private void addPolyLines(final DirectionsResult result) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(polylineDataList.size() > 0) {
                    for(PolylineData data: polylineDataList)
                        data.getPolyline().remove();
                    polylineDataList.clear();
                    polylineDataList = new ArrayList<>();
                }
                System.out.println("routes = " + result.routes.length);
                for(DirectionsRoute route : result.routes) {
                    List <com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());
                    List<LatLng> newDecodedPath = new ArrayList<>();
                    for(com.google.maps.model.LatLng latLng : decodedPath) {
                        newDecodedPath.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    polyline.setColor(ContextCompat.getColor(getActivity(),R.color.dark_grey));
                    polyline.setClickable(true);
                    polylineDataList.add(new PolylineData(polyline, route.legs[0]));
                }
            }
        });
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        for(PolylineData data : polylineDataList) {
            if(polyline.getId().equals(data.getPolyline().getId())) {
                data.getPolyline().setColor(ContextCompat.getColor(getActivity(),R.color.light_blue));
                data.getPolyline().setZIndex(1);

                /////WYSZUKAJ PUNKT REZERWY PALIWA
                Vehicle testVehicle = new Vehicle("BMW", 50, 7, "Diesel", 10);
                PolylineService service = new PolylineService(testVehicle, data.getPolyline());
                LatLng firstPoint = service.getFuelReservePointOnRoute();
                mMap.addMarker(new MarkerOptions().position(firstPoint).title("Brak paliwa"));
                FirestorePetrolsDB petrolsDB = new FirestorePetrolsDB(firstPoint, mMap, getContext(), getActivity());
                petrolsDB.findNearbyPetrols(2);
            }
            else {
                data.getPolyline().setColor(ContextCompat.getColor(getActivity(),R.color.dark_grey));
                data.getPolyline().setZIndex(0);
            }
        }

    }
}