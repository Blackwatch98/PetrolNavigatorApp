package com.example.petrolnavigatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;


public class PetrolPopUpActivity extends Activity {

    private LinearLayout availableFuelsLayout;
    private Button changeTypeBtn;
    private DatabaseReference mRef;
    private double lat;
    private double lon;
    private Context context;
    private DataSnapshot popedPetrol;
    private PetrolRecyclerViewAdapter petrolRecyclerViewAdapter;

    //private Integer[] imgId = {R.drawable.pb95, R.drawable.pb98,R.drawable.on};

    private String[] imgNames= {"Elektryczny", "Benzyna", "LPG", "Etanol", "Diesel", "CNG"};
    private Integer[] imgId = {R.drawable.elektr, R.drawable.benz, R.drawable.lpg, R.drawable.etan, R.drawable.diesel, R.drawable.cng};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol_pop_up);
        context = this;
        availableFuelsLayout = findViewById(R.id.availableFuels);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String name = bundle.getString("petrolName");
            lat=bundle.getDouble("latitude");
            lon=bundle.getDouble("longitude");

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
                                    ds.child("coordinates").child("longitude").getValue().equals(lon)) {
                                popedPetrol = ds;

                                Iterator<DataSnapshot> items = ds.child("availableFuels").getChildren().iterator();
                                while(items.hasNext())
                                {
                                    DataSnapshot item = items.next();

                                    int pos = 0;
                                    for(String str : imgNames) {
                                        if (item.getKey().equals(str) && item.getValue().equals(true)) {
                                            ImageView img = new ImageView(context);
                                            img.setImageResource(imgId[pos]);
                                            availableFuelsLayout.addView(img);
                                            break;
                                        }
                                        pos++;
                                    }
                                }
                                break;
                            }
                        }
                        Spinner spinner = findViewById(R.id.fuelTypesSpinner);
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,R.array.fuelTypes,R.layout.support_simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                        spinner.setAdapter(adapter);

                        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                                RecyclerView recyclerView = findViewById(R.id.recyclerPetrolView);
                                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
                                LinkedList<Fuel> fuelList = new LinkedList<>();

                                for(DataSnapshot ds : popedPetrol.child("fuels").getChildren())
                                {
                                    Fuel fuel = new Fuel(Integer.parseInt(ds.child("icon").getValue().toString()),
                                            ds.child("price").getValue().toString(),
                                            ds.child("name").getValue().toString(),
                                            ds.child("type").getValue().toString(),
                                            Integer.parseInt(ds.child("reportCounter").getValue().toString()),
                                            ds.child("lastReportDate").getValue().toString());

                                    if(position == 0 && fuel.getType().equals("fluid"))
                                        fuelList.add(fuel);
                                    else if(position == 1 && fuel.getType().equals("gas"))
                                        fuelList.add(fuel);
                                    else if(position == 2 && fuel.getType().equals("unconv"))
                                        fuelList.add(fuel);
                                }

                                petrolRecyclerViewAdapter = new PetrolRecyclerViewAdapter(fuelList, context);
                                recyclerView.setLayoutManager(layoutManager);
                                recyclerView.setAdapter(petrolRecyclerViewAdapter);
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK)
        {
            if(requestCode == 1)
            {
                Bundle extras = data.getExtras();
                String price = extras.getString("priceString");
                String name = extras.getString("fuelName");
                String num = null;

                for(DataSnapshot ds : popedPetrol.child("fuels").getChildren())
                    if(ds.child("name").getValue().toString().equals(name))
                        num = ds.getKey();

                if(popedPetrol.child("fuels").child(num).child("price").getValue().equals(price))
                {
                    mRef.child(popedPetrol.getKey()).child("fuels").child(num).
                            child("reportCounter").setValue((Long)popedPetrol.child("fuels").child(num)
                            .child("reportCounter").getValue()+1);
                }
                else
                {
                    mRef.child(popedPetrol.getKey()).child("fuels").child(num).
                            child("reportCounter").setValue(1);

                    mRef.child(popedPetrol.getKey()).child("fuels").child(num).child("price").setValue(price);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                mRef.child(popedPetrol.getKey()).child("fuels").child(num).child("lastReportDate").setValue(sdf.format(new Date()));
            }
        }

    }
}