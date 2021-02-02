package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Fragment that finds routes to selected target on map
 * and marks all points where fuel level of current vehicle is going to be low.
 * Next it search for all petrol stations that user is able to reach.
 * It cooperates with Google Directions API.
 * Connected with VehiclesListFragment.
 */
public class PlanRouteFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnPolylineClickListener {

    private GoogleMap mMap;
    private ArrayList<Vehicle> userVehicles;
    private Vehicle currentVehicle;
    private FusedLocationProviderClient mFusedProviderClient;
    private LatLng currentLocation;
    private Location mLastLocation;
    private LocationRequest request;
    private SearchView searchView;
    private Spinner carSpinner;
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
        searchView = getActivity().findViewById(R.id.searchView);
        carSpinner = getActivity().findViewById(R.id.carSpinner);

        userVehicles = (ArrayList<Vehicle>) getArguments().getSerializable("userVehicles");
        if(userVehicles != null) {
            ArrayList<String> names = new ArrayList<>();
            for (Vehicle vehicle : userVehicles)
                names.add(vehicle.getName());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, names);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            carSpinner.setAdapter(adapter);
        }
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

        carSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
                currentVehicle = userVehicles.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.white));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mMap.clear();
                String location = searchView.getQuery().toString();
                List<Address> addressList = null;
                if(!location.equals("")) {
                    Toast.makeText(getContext(), "Lokacja nieznana! Podaj prawidłowy cel!", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if(userVehicles == null) {
                    Toast.makeText(getContext(), "Musisz najpierw wybrać samochód!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Geocoder geocoder = new Geocoder(view.getContext());
                try {
                    addressList = geocoder.getFromLocationName(location, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Address targetAddress = addressList.get(0);
                LatLng latLng = new LatLng(targetAddress.getLatitude(), targetAddress.getLongitude());
                final Marker targetMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(location));

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if(!marker.getTitle().equals("Rezerwa paliwa") && !marker.getTitle().equals(targetMarker.getTitle())) {
                            Intent intent = new Intent(getContext(), PetrolPopUpActivity.class);
                            intent.putExtra("latitude", marker.getPosition().latitude);
                            intent.putExtra("longitude", marker.getPosition().longitude);
                            getContext().startActivity(intent);
                        }
                        return false;
                    }
                });

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
                calculateDirections(targetMarker);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
    }

    /**
     * Find all possible routes to selected and get data about them.
     * @param marker selected target marker
     */
    private void calculateDirections(Marker marker) {
        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng (marker.getPosition().latitude, marker.getPosition().longitude);
        DirectionsApiRequest directions = new DirectionsApiRequest(geoApiContext);
        directions.alternatives(true);
        directions.origin(new com.google.maps.model.LatLng(currentLocation.latitude,currentLocation.longitude));
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                addPolyLines(result);
            }

            @Override
            public void onFailure(Throwable e) {
                e.getMessage();
            }
        });
    }

    /**
     * Using collected data about potential routes it marks them on the map.
     * @param result result of directions search needed to build polyline
     */
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

    /**
     * Clicked polyline launches search algorithm.
     * @param polyline selected route
     */
    @Override
    public void onPolylineClick(Polyline polyline) {
        for(PolylineData data : polylineDataList) {
            if(polyline.getId().equals(data.getPolyline().getId())) {
                data.getPolyline().setColor(ContextCompat.getColor(getActivity(),R.color.light_blue));
                data.getPolyline().setZIndex(1);

                PolylineService service = new PolylineService(currentVehicle, data.getPolyline());
                LinkedList<LatLng> allReservePoints = service.getFuelReservePointOnRoute();

                FirestorePetrolsDB petrolsDB = new FirestorePetrolsDB(
                        mMap, getContext(), getActivity(), currentVehicle);

                for(LatLng point : allReservePoints) {
                    mMap.addMarker(new MarkerOptions().position(point).title("Rezerwa paliwa"));

                    petrolsDB.getPetrolsOnRoute(data.getPolyline().getPoints(), point,
                            data.getPolyline().getPoints().get(0),
                            data.getPolyline().getPoints().get(data.getPolyline().getPoints().size() - 1),
                            geoApiContext);
                }
            }
            else {
                data.getPolyline().setColor(ContextCompat.getColor(getActivity(),R.color.dark_grey));
                data.getPolyline().setZIndex(0);
            }
        }
    }
}