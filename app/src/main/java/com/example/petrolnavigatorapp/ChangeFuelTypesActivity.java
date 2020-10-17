package com.example.petrolnavigatorapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ChangeFuelTypesActivity extends AppCompatActivity {

    private HashMap<String,Boolean> fuelTypes;
    private List<Switch> switches;
    private Switch beznSwitch;
    private Switch dieselSwitch;
    private Switch lpgSwitch;
    private Switch etaSwitch;
    private Switch elecSwitch;
    private Switch cngSwitch;
    private Button confirmBtn;
    private LatLng coor;

    private DatabaseReference reff;
    private DataSnapshot myDs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_fuel_types);

        beznSwitch = findViewById(R.id.benzSwitch);
        dieselSwitch = findViewById(R.id.dieselSwitch);
        lpgSwitch = findViewById(R.id.lpgSwitch);
        etaSwitch = findViewById(R.id.etaSwitch);
        elecSwitch = findViewById(R.id.elecSwitch);
        cngSwitch = findViewById(R.id.cngSwitch);
        confirmBtn = findViewById(R.id.confirmBtn2);
        switches = new LinkedList<>();

        switches.add(beznSwitch);
        switches.add(dieselSwitch);
        switches.add(lpgSwitch);
        switches.add(etaSwitch);
        switches.add(elecSwitch);
        switches.add(cngSwitch);

        reff = FirebaseDatabase.getInstance().getReference().child("Petrols");
        Bundle bundle = getIntent().getExtras();
        double lat=bundle.getDouble("latitude");
        double lon=bundle.getDouble("longitude");
        this.coor = new LatLng(lat,lon);


        reff.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    for(DataSnapshot ds : dataSnapshot.getChildren())
                    {
                        HashMap<String,Double> test = new HashMap<>();
                        test.put("latitude",coor.latitude);
                        test.put("longitude", coor.longitude);

                        if(ds.child("coordinates").child("latitude").getValue().equals(test.get("latitude")))
                        {
                            // to w tym ds zrób coś
                            fuelTypes = (HashMap<String, Boolean>) ds.child("availableFuels").getValue();
                            myDs = ds;
                            if(fuelTypes != null)
                            {
                                for(int i = 0; i < fuelTypes.size(); i++)
                                {
                                    if(fuelTypes.get( (fuelTypes.keySet().toArray())[ i ] ).equals(true))
                                    {
                                        switches.get(i).setChecked(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String,Boolean> availableFuels = new HashMap<>();
                for(int i = 0; i < switches.size(); i++)
                {
                    if(switches.get(i).isChecked())
                        availableFuels.put(fuelTypes.keySet().toArray()[i].toString(),true);
                    else
                        availableFuels.put(fuelTypes.keySet().toArray()[i].toString(),false);
                        //get( (fuelTypes.keySet().toArray())[ i ])
                }

                myDs.getRef().child("availableFuels").setValue(availableFuels);
                finish();
            }
        });
    }
}