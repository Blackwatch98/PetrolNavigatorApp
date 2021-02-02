//package com.example.petrolnavigatorapp.old_data_backup;
//
//import android.os.Bundle;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.animation.Animation;
//import android.view.animation.AnimationUtils;
//import android.widget.Button;
//import android.widget.Switch;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.petrolnavigatorapp.R;
//import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
//import com.google.android.gms.maps.OnMapReadyCallback;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//public class ChangeFuelTypesActivity extends AppCompatActivity implements OnMapReadyCallback {
//
//    private HashMap<String, Boolean> fuelTypes;
//    private List<Switch> switches;
//    private Switch benzSwitch, dieselSwitch, lpgSwitch, etaSwitch, elecSwitch, cngSwitch;
//    private Button confirmBtn, reportNoExist, cancelBtn;
//    private MapView mapView;
//    private TextView editNameView;
//    private Animation scale_up, scale_down;
//
//    private String petrolName;
//    private LatLng coor;
//
//    private DatabaseReference reff;
//    private DataSnapshot myDs;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_change_fuel_types);
//
//        benzSwitch = findViewById(R.id.benzSwitch);
//        dieselSwitch = findViewById(R.id.dieselSwitch);
//        lpgSwitch = findViewById(R.id.lpgSwitch);
//        etaSwitch = findViewById(R.id.etaSwitch);
//        elecSwitch = findViewById(R.id.elecSwitch);
//        cngSwitch = findViewById(R.id.cngSwitch);
//        confirmBtn = findViewById(R.id.confirmBtn2);
//        editNameView = findViewById(R.id.editTextPetrolName);
//        reportNoExist = findViewById(R.id.notExistBtn);
//
//        mapView = findViewById(R.id.petrolMapView);
//        mapView.getMapAsync(this);
//        mapView.onCreate(savedInstanceState);
//
//
//        switches = new LinkedList<>();
//        switches.add(benzSwitch);
//        switches.add(dieselSwitch);
//        switches.add(lpgSwitch);
//        switches.add(etaSwitch);
//        switches.add(elecSwitch);
//        switches.add(cngSwitch);
//
//        reff = FirebaseDatabase.getInstance().getReference().child("Petrols");
//
//        Bundle bundle = getIntent().getExtras();
//        double lat = bundle.getDouble("latitude");
//        double lon = bundle.getDouble("longitude");
//        petrolName = bundle.getString("petrolName");
//        editNameView.setText(petrolName);
//        this.coor = new LatLng(lat, lon);
//
//        reff.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()) {
//                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
//                        HashMap<String, Double> test = new HashMap<>();
//                        test.put("latitude", coor.latitude);
//                        test.put("longitude", coor.longitude);
//
//                        if (ds.child("coordinates").child("latitude").getValue().equals(test.get("latitude")) &&
//                                ds.child("coordinates").child("longitude").getValue().equals(test.get("longitude"))) {
//                            fuelTypes = (HashMap<String, Boolean>) ds.child("availableFuels").getValue();
//                            myDs = ds;
//                            if (fuelTypes != null) {
//                                Iterator it = fuelTypes.entrySet().iterator();
//                                while (it.hasNext()) {
//                                    Map.Entry mapElement = (Map.Entry) it.next();
//                                    for (Switch sw : switches)
//                                        if (sw.getText().equals(mapElement.getKey())) {
//                                            if ((Boolean) mapElement.getValue())
//                                                sw.setChecked(true);
//                                        }
//                                }
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//        confirmBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                HashMap<String, Boolean> availableFuels = new HashMap<>();
//                for (Switch sw : switches) {
//                    if (sw.isChecked())
//                        availableFuels.put(sw.getText().toString(), true);
//                    else
//                        availableFuels.put(sw.getText().toString(), false);
//                }
//                myDs.getRef().child("availableFuels").setValue(availableFuels);
//                finish();
//            }
//        });
//
//        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
//        scale_down = AnimationUtils.loadAnimation(this, R.anim.scale_down);
//
//        reportNoExist.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
//                    scale_up.setStartTime(0);
//                    reportNoExist.startAnimation(scale_up);
//                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
//                    scale_up.setStartTime(0);
//                    reportNoExist.startAnimation(scale_down);
//                }
//
//                return false;
//            }
//        });
//
//        cancelBtn = findViewById(R.id.cancelBtn);
//        cancelBtn.setOnClickListener(new View.OnClickListener() {
//                                         @Override
//                                         public void onClick(View view) {
//                                             finish();
//                                         }
//                                     }
//        );
//    }
//
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        googleMap.addMarker(new MarkerOptions().position(coor).title(petrolName));
//        float zoomLevel = 16.0f;
//        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coor, zoomLevel));
//        googleMap.getUiSettings().setAllGesturesEnabled(false);
//        googleMap.getUiSettings().setMapToolbarEnabled(false);
//    }
//
//    @Override
//    public void onResume() {
//        mapView.onResume();
//        super.onResume();
//    }
//
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        mapView.onPause();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        mapView.onDestroy();
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        mapView.onLowMemory();
//    }
//}