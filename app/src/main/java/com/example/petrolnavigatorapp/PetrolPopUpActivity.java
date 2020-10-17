package com.example.petrolnavigatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;


public class PetrolPopUpActivity extends Activity {

    private LinearLayout availableFuelsLayout;
    private Button changeTypeBtn;
    private DatabaseReference mRef;
    private double lat;
    private double lon;
    private HashMap<String,Boolean> availableFuels;
    private Context context;

    //private String[] fuels = {"Pb95","Pb98", "Diesel"};
    //private Integer[] imgId = {R.drawable.pb95, R.drawable.pb98,R.drawable.on};
    private Integer[] imgId2 = {R.drawable.benz, R.drawable.cng, R.drawable.diesel, R.drawable.elektr, R.drawable.etan, R.drawable.lpg};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol_pop_up);
        context = this;
        availableFuelsLayout = (LinearLayout) findViewById(R.id.availableFuels);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String name = bundle.getString("petrolName");
            lat=bundle.getDouble("latitude");
            lon=bundle.getDouble("longitude");

            NestedScrollView nestedScrollView = findViewById(R.id.nest);
            CoordinatorLayout coordinatorLayout = findViewById(R.id.ok);

            coordinatorLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            mRef = FirebaseDatabase.getInstance().getReference("Petrols");

            mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            if(ds.child("coordinates").child("latitude").getValue().equals(lat) &&
                                    ds.child("coordinates").child("longitude").getValue().equals(lon))
                            {
                                availableFuels = new HashMap<>();
                                Iterator<DataSnapshot> items = ds.child("availableFuels").getChildren().iterator();
                                while(items.hasNext())
                                {
                                    DataSnapshot item = items.next();

                                    availableFuels.put(item.getKey(), (Boolean) item.getValue());
                                }
                                int i = 0;
                                for(Boolean bool : availableFuels.values())
                                {
                                    if(bool)
                                    {
                                        ImageView img = new ImageView(context);
                                        img.setImageResource(imgId2[i]);
                                        availableFuelsLayout.addView(img);
                                    }

                                    i++;
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            changeTypeBtn = findViewById(R.id.setChanges);

            changeTypeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ChangeFuelTypesActivity.class);
                    intent.putExtra("latitude",lat);
                    intent.putExtra("longitude",lon);
                    view.getContext().startActivity(intent);
                }
            });

            TextView petrolName = findViewById(R.id.petrolName);
            TextView petrolCoor= findViewById(R.id.petrolCoor);

            petrolName.setText(name);
            petrolCoor.setText(""+lat+", "+lon);
        }
    }
}