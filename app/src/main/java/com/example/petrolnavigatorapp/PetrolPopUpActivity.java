package com.example.petrolnavigatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petrolnavigatorapp.adapters.FuelsRecyclerViewAdapter;
import com.example.petrolnavigatorapp.firebase_utils.FirestorePetrolsDB;
import com.example.petrolnavigatorapp.firebase_utils.MyFirebaseStorage;
import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PetrolPopUpActivity extends Activity {

    final private int FUELS_IN_LINEAR_ROW = 4;
    final private int X_SCREEN_DIVIDER = 5, Y_SCREEN_DIVIDER = 17;
    final private double REQUIRED_MINIMAL_DISTANCE_IN_METERS = 100;

    private FirebaseFirestore firestore;
    private MyFirebaseStorage sRef;
    private double latitude, longitude;
    private Context context;
    private FuelsRecyclerViewAdapter fuelsRecyclerViewAdapter;
    private LinearLayout availableFuelsLayout;
    private Animation scale_up, scale_down;
    private Petrol popedPetrol;
    private String petrolId;
    private  List<Fuel> fuelList;
    private double userLat, userLon;
    private boolean isMinimalDistanceReached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol_pop_up);
        context = this;
        availableFuelsLayout = findViewById(R.id.availableFuels);
        Bundle bundle = getIntent().getExtras();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        userLat = bundle.getDouble("userLat");
        userLon = bundle.getDouble("userLon");
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");

        if(getDistance(userLat,userLon,latitude,longitude) <= REQUIRED_MINIMAL_DISTANCE_IN_METERS) {
            isMinimalDistanceReached = true;
        }

        CoordinatorLayout coordinatorLayout = findViewById(R.id.popUpBackground);
        coordinatorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firestore = FirebaseFirestore.getInstance();
        firestore.collection("petrol_stations").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots == null)
                    return;

                FirestorePetrolsDB test = new FirestorePetrolsDB();
                for(QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    double lat = Double.parseDouble(query.get("lat").toString());
                    double lon = Double.parseDouble(query.get("lon").toString());

                    if(latitude == lat && longitude == lon)
                    {
                        popedPetrol = test.SnapshotToPetrol(query);
                        petrolId = query.getId();
                        break;
                    }

                }
                if(popedPetrol == null) {
                    System.out.println("Stacja nie istnieje!");
                    return;
                }

                HashMap<String, Integer> map = getFuelImages(popedPetrol.getAvailableFuels());
                fuelList = new LinkedList<>();
                if(map != null && map.size() > 0) {
                    String default_name = map.entrySet().iterator().next().getKey();
                    for (Fuel fuel : popedPetrol.getFuels())
                        if (fuel.getType().equals(default_name)) {
                            fuelList.add(fuel);
                        }

                    RecyclerView recyclerView = findViewById(R.id.recyclerPetrolView);
                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

                    fuelsRecyclerViewAdapter = new FuelsRecyclerViewAdapter(fuelList, context, petrolId, isMinimalDistanceReached);
                    recyclerView.setLayoutManager(layoutManager);
                    recyclerView.setAdapter(fuelsRecyclerViewAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                }
                int list_size = map.size();
                if(latitude == popedPetrol.getLat() && longitude == popedPetrol.getLon())
                {
                    int counter = 0;
                    for (int i = 0; i <= list_size/FUELS_IN_LINEAR_ROW; i++) {
                        LinearLayout row = new LinearLayout(context);
                        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        else if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

                        if(map.size() < FUELS_IN_LINEAR_ROW)
                            counter = map.size();
                        else
                            counter = FUELS_IN_LINEAR_ROW;

                        for (int j = 0; j < counter; j++) {
                            RelativeLayout.LayoutParams layoutParams;
                            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                                layoutParams = new RelativeLayout.LayoutParams(width / X_SCREEN_DIVIDER, height / Y_SCREEN_DIVIDER);
                            else
                                layoutParams = new RelativeLayout.LayoutParams(height / X_SCREEN_DIVIDER, width / Y_SCREEN_DIVIDER);

                            Map.Entry<String,Integer> entry = map.entrySet().iterator().next();
                            final String fuelTypeName = entry.getKey();

                            ImageView img = new ImageView(context);
                            img.setPadding(5, 5, 5, 5);
                            img.setLayoutParams(layoutParams);
                            img.setImageResource(map.get(fuelTypeName));
                            map.remove(fuelTypeName);
                            img.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Toast.makeText(context, fuelTypeName, Toast.LENGTH_SHORT).show();
                                    fuelList = new LinkedList<>();
                                    for(Fuel fuel : popedPetrol.getFuels())
                                        if(fuel.getType().equals(fuelTypeName))
                                            fuelList.add(fuel);
                                    RecyclerView recyclerView = findViewById(R.id.recyclerPetrolView);
                                    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);

                                    fuelsRecyclerViewAdapter = new FuelsRecyclerViewAdapter(fuelList, context, petrolId, isMinimalDistanceReached);
                                    recyclerView.setLayoutManager(layoutManager);
                                    recyclerView.setAdapter(fuelsRecyclerViewAdapter);
                                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                                }
                            });
                            row.addView(img);
                        }
                        availableFuelsLayout.addView(row);
                    }
                }

                TextView petrolName = findViewById(R.id.petrolName);
                TextView petrolCoor= findViewById(R.id.petrolAddress);

                petrolName.setText(popedPetrol.getName());
                petrolCoor.setText(popedPetrol.getAddress());

                sRef = new MyFirebaseStorage();
                try
                {
                    final File localFile = File.createTempFile("petrol_icon","png");
                    sRef.getPetrolIconRef(popedPetrol.getName()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            ((ImageView)findViewById(R.id.petrol_icon)).setImageBitmap(bitmap);
                        }
                    });
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                final Button changeTypeBtn = findViewById(R.id.setChanges);
                changeTypeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!isMinimalDistanceReached) {
                            Toast.makeText(context, "Jesteś zbyt daleko, aby wykonać zgłoszenie!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(popedPetrol != null) {
                            Intent intent = new Intent(view.getContext(), ChangeFuelTypesActivity.class);
                            intent.putExtra("latitude",popedPetrol.getLat());
                            intent.putExtra("longitude",popedPetrol.getLon());
                            intent.putExtra("petrolName",popedPetrol.getName());
                            intent.putExtra("petrolId", petrolId);
                            ((PetrolPopUpActivity)context).startActivityForResult(intent, 2);
                        }
                    }
                });

                scale_up = AnimationUtils.loadAnimation(context,R.anim.scale_up);
                scale_down = AnimationUtils.loadAnimation(context,R.anim.scale_down);
                changeTypeBtn.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                            changeTypeBtn.startAnimation(scale_up);
                        else if(motionEvent.getAction()==MotionEvent.ACTION_UP)
                            changeTypeBtn.startAnimation(scale_down);

                        return false;
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode==RESULT_OK)
        {
            if(requestCode == 1 )
            {
                finish();
                //startActivity(getIntent());
            }
            else if(requestCode == 2)
            {
                finish();
                //startActivity(getIntent());
            }
        }

    }

    public HashMap<String, Integer> getFuelImages(HashMap<String,Boolean> fuelsMap)
    {
        HashMap<String, Integer> map = new HashMap<>();
        for(String item : fuelsMap.keySet()) {
            String key = item.toString();
            Boolean value = fuelsMap.get(item);

            if(value)
                switch (key)
                {
                    case "Benzyna":
                        map.put("Benzyna", R.drawable.benz);
                        break;
                    case "Diesel":
                        map.put("Diesel", R.drawable.diesel);
                        break;
                    case "LPG":
                        map.put("LPG", R.drawable.lpg);
                        break;
                    case "Etanol":
                        map.put("Etanol", R.drawable.etan);
                        break;
                    case "Elektryczny":
                        map.put("Elektryczny", R.drawable.elektr);
                        break;
                    case "CNG":
                        map.put("CNG", R.drawable.cng);
                        break;
                }

        }
        return map;
    }

    private double getDistance(double lat1, double lon1, double lat2, double lon2)
    {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        double distance = location1.distanceTo(location2);

        return distance;
    }
}