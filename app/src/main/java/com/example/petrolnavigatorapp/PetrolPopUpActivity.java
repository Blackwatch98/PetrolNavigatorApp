package com.example.petrolnavigatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.petrolnavigatorapp.services.MathService;
import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.Petrol;
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
import java.util.Map;

/**
 * This activity represents preview menu of petrol station data.
 * Contains information about: petrol name, available fuel types, fuel prices etc.
 * From here user can get to ChangePricesActivity and ChangeFuelTypesActivity
 */
public class PetrolPopUpActivity extends Activity {

    final private int FUELS_IN_LINEAR_ROW = 4;
    final private int X_SCREEN_DIVIDER = 5, Y_SCREEN_DIVIDER = 17;
    final private double REQUIRED_MINIMAL_DISTANCE_IN_METERS = 100;

    private FirebaseFirestore fireStore;
    private MyFirebaseStorage sRef;
    private double petrolLatitude, petrolLongitude;
    private Context context;
    private FuelsRecyclerViewAdapter fuelsRecyclerViewAdapter;
    private LinearLayout availableFuelsLayout;
    private Animation scale_up, scale_down;
    private Petrol popedStation;
    private String petrolId;
    private List<Fuel> fuelList;
    private double userLatitude, userLongitude;
    private boolean isMinimalDistanceReached = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol_pop_up);
        context = this;
        availableFuelsLayout = findViewById(R.id.availableFuels);
        Bundle bundle = getIntent().getExtras();

        userLatitude = bundle.getDouble("userLat");
        userLongitude = bundle.getDouble("userLon");
        petrolLatitude = bundle.getDouble("latitude");
        petrolLongitude = bundle.getDouble("longitude");

        MathService mathService = new MathService();
        if (mathService.getDistanceBetweenTwoPoints(userLatitude, userLongitude, petrolLatitude, petrolLongitude) <= REQUIRED_MINIMAL_DISTANCE_IN_METERS) {
            isMinimalDistanceReached = true;
        }

        CoordinatorLayout coordinatorLayout = findViewById(R.id.popUpBackground);
        coordinatorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        fireStore = FirebaseFirestore.getInstance();
        fireStore.collection("petrol_stations").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots == null)
                    return;

                setPopedPetrolStation(queryDocumentSnapshots);
                if (popedStation == null) {
                    System.out.println("Station does not exist!");
                    return;
                }

                HashMap<String, Integer> availableFuelTypesHashMap = getFuelImages(popedStation.getAvailableFuels());
                setFuelsList(availableFuelTypesHashMap);
                setFuelTypesResponsiveList(availableFuelTypesHashMap);

                TextView petrolName = findViewById(R.id.petrolName);
                TextView petrolCoor = findViewById(R.id.petrolAddress);
                petrolName.setText(popedStation.getName());
                petrolCoor.setText(popedStation.getAddress());

                setStationLogoImage();

                handleReportChangesButton();
            }
        });
    }

    /**
     * Handles report changes button listener.
     */
    private void handleReportChangesButton() {
        final Button changeTypeBtn = findViewById(R.id.setChanges);
        changeTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isMinimalDistanceReached) {
                    Toast.makeText(context, "Jesteś zbyt daleko, aby wykonać zgłoszenie!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (popedStation != null) {
                    System.out.println("Station does not exist!");
                    return;
                }
                Intent intent = new Intent(view.getContext(), ChangeFuelTypesActivity.class);
                intent.putExtra("latitude", popedStation.getLat());
                intent.putExtra("longitude", popedStation.getLon());
                intent.putExtra("petrolName", popedStation.getName());
                intent.putExtra("petrolId", petrolId);
                ((PetrolPopUpActivity) context).startActivityForResult(intent, 2);
            }
        });

        scale_up = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        scale_down = AnimationUtils.loadAnimation(context, R.anim.scale_down);
        changeTypeBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                    changeTypeBtn.startAnimation(scale_up);
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                    changeTypeBtn.startAnimation(scale_down);

                return false;
            }
        });
    }

    /**
     * Sets logo image got from Firebase Storage cloud.
     */
    private void setStationLogoImage() {
        sRef = new MyFirebaseStorage();
        try {
            final File localFile = File.createTempFile("petrol_icon", "png");
            sRef.getPetrolIconRef(popedStation.getName()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    ((ImageView) findViewById(R.id.petrol_icon)).setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function creates responsive fuel types list that's size depends of screen resolution and orientation.
     * @param availableFuelTypesHashMap HashMap of fuel types available on poped petrol station.
     */
    private void setFuelTypesResponsiveList(HashMap<String, Integer> availableFuelTypesHashMap) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        int list_size = availableFuelTypesHashMap.size();
        if (petrolLatitude != popedStation.getLat() || petrolLongitude != popedStation.getLon()) {
            return;
        }

        int counter;
        for (int i = 0; i <= list_size / FUELS_IN_LINEAR_ROW; i++) {
            LinearLayout row = new LinearLayout(context);
            if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            else if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

            if (availableFuelTypesHashMap.size() < FUELS_IN_LINEAR_ROW)
                counter = availableFuelTypesHashMap.size();
            else
                counter = FUELS_IN_LINEAR_ROW;

            for (int j = 0; j < counter; j++) {
                RelativeLayout.LayoutParams layoutParams;
                if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    layoutParams = new RelativeLayout.LayoutParams(width / X_SCREEN_DIVIDER, height / Y_SCREEN_DIVIDER);
                else
                    layoutParams = new RelativeLayout.LayoutParams(height / X_SCREEN_DIVIDER, width / Y_SCREEN_DIVIDER);

                Map.Entry<String, Integer> entry = availableFuelTypesHashMap.entrySet().iterator().next();
                final String fuelTypeName = entry.getKey();

                ImageView img = new ImageView(context);
                img.setPadding(5, 5, 5, 5);
                img.setLayoutParams(layoutParams);
                img.setImageResource(availableFuelTypesHashMap.get(fuelTypeName));
                availableFuelTypesHashMap.remove(fuelTypeName);
                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, fuelTypeName, Toast.LENGTH_SHORT).show();
                        fuelList = new LinkedList<>();
                        for (Fuel fuel : popedStation.getFuels())
                            if (fuel.getType().equals(fuelTypeName))
                                fuelList.add(fuel);
                        RecyclerView recyclerView = findViewById(R.id.recyclerPetrolView);
                        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

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

    /**
     * Sets list for fuels data.
     * @param availableFuelTypesHashMap HashMap of fuel types available on poped petrol station.
     */
    private void setFuelsList(HashMap<String, Integer> availableFuelTypesHashMap) {
        fuelList = new LinkedList<>();
        if (availableFuelTypesHashMap != null && availableFuelTypesHashMap.size() > 0) {
            System.out.println("No petrol data found!");
            return;
        }
        String default_name = availableFuelTypesHashMap.entrySet().iterator().next().getKey();
        for (Fuel fuel : popedStation.getFuels())
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

    private void setPopedPetrolStation(QuerySnapshot queryDocumentSnapshots) {
        FirestorePetrolsDB firestorePetrolsDB = new FirestorePetrolsDB();
        for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
            double lat = Double.parseDouble(query.get("lat").toString());
            double lon = Double.parseDouble(query.get("lon").toString());

            if (petrolLatitude == lat && petrolLongitude == lon) {
                popedStation = firestorePetrolsDB.SnapshotToPetrol(query);
                petrolId = query.getId();
                break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1 || requestCode == 2) {
                finish();
            }
        }
    }

    /**
     * Gets fuel types icons from database.
     * @param availableFuelTypesHashMap HashMap of fuel types available on poped petrol station.
     * @return Map of fuel type and assigned image icon.
     */
    public HashMap<String, Integer> getFuelImages(HashMap<String, Boolean> availableFuelTypesHashMap) {
        HashMap<String, Integer> map = new HashMap<>();
        for (String item : availableFuelTypesHashMap.keySet()) {
            String key = item.toString();
            Boolean value = availableFuelTypesHashMap.get(item);
            if (value == null)
                continue;

            if (value)
                switch (key) {
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
}