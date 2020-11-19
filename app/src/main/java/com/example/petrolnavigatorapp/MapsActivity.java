package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;

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
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import android.location.Location;
import android.location.LocationListener;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, NavigationView.OnNavigationItemSelectedListener, TaskListener {

    private GoogleMap mMap;
    private Context context;
    private DrawerLayout drawer;
    private List<Marker> markers;
    private LocationRequest request;
    private Location mLastLocation;
    private LatLng latLng;
    private int radius;
    private FusedLocationProviderClient mFusedProviderClient;
    private float cameraZoom;
    private FirebaseFirestore fireStore;

    private LocationCallback mLocationCallback = new LocationCallback()
    {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            super.onLocationResult(locationResult);
            for(Location location : locationResult.getLocations())
            {
                latLng = new LatLng(location.getLatitude(),location.getLongitude());

                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
            }
            findPetrols();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        context = this;

        fireStore = FirebaseFirestore.getInstance();
        DocumentReference documentReference = fireStore.collection("users")
            .document(FirebaseAuth.getInstance().getCurrentUser().getUid());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    Map <String, Object> map = (Map<String,Object>)documentSnapshot.get("userSettings");
                    radius = Integer.parseInt(map.get("searchRadius").toString())*1000;
                }
            }
        });

        Bundle bundle = getIntent().getExtras();

        //DEPRECATED GET RADIUS
        //radius = bundle.getInt("seekBarValue");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        markers = new LinkedList<>();
        mFusedProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        cameraZoom = mMap.getCameraPosition().zoom;

        request = new LocationRequest();
        //request.setInterval(1000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                mFusedProviderClient.requestLocationUpdates(request, mLocationCallback,
                        Looper.myLooper());
                mMap.setMyLocationEnabled(true);
                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng2) {
                        latLng = latLng2;
                        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        mMap.setMyLocationEnabled(false);
                        mMap.setMyLocationEnabled(true);
                    }
                });
                mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                    @Override
                    public void onCameraMoveStarted(int i) {
                        if (cameraZoom != mMap.getCameraPosition().zoom)
                        {
                            if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            {
                                mFusedProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    if (location != null) {
                                        Toast.makeText(context, "Uaktualniam pozycję...", Toast.LENGTH_SHORT).show();
                                        //latLng = new LatLng(location.getLatitude(),location.getLongitude());
                                        cameraZoom = mMap.getCameraPosition().zoom;
                                        findPetrols();
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

    public void findPetrols()
    {
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        stringBuilder.append("location=" + latLng.latitude + "," + latLng.longitude);
        stringBuilder.append("&radius=" + radius);
        stringBuilder.append("&keyword=" + "petrol");
        stringBuilder.append("&key="+getResources().getString(R.string.google_places_key));

        String url = stringBuilder.toString();

        Object []dataTransfer = new Object[3];
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = this;

        GetNearbyPetrols getNearbyPetrols = new GetNearbyPetrols();
        getNearbyPetrols.execute(dataTransfer);
    }

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                new AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                            }
                        })
                        .create()
                        .show();
            }
            else
            {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
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
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    mFusedProviderClient.requestLocationUpdates(request,mLocationCallback,Looper.myLooper());
                    mMap.setMyLocationEnabled(true);
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Proszę nadać uprawnienia", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId())
        {
            case R.id.db:
                Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                break;
            }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            default:
               break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskFinish(List<Marker> markers) {
        for(Marker marker : markers)
            this.markers.add(marker);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latLng = new LatLng(location.getLatitude(),location.getLongitude());
    }
}