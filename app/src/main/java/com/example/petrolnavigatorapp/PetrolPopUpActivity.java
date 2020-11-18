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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petrolnavigatorapp.firebase_utils.MyFirebaseStorage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;


public class PetrolPopUpActivity extends Activity {

    final int FUELS_IN_LINEAR_ROW = 4;
    private LinearLayout availableFuelsLayout;
    private DatabaseReference mRef;
    private MyFirebaseStorage sRef;
    private double lat, lon;
    private Context context;
    private DataSnapshot popedPetrol;
    private PetrolRecyclerViewAdapter petrolRecyclerViewAdapter;
    private Animation scale_up, scale_down;

    private String[] imgNames = {"Elektryczny", "Benzyna", "LPG", "Etanol", "Diesel", "CNG"};
    private Integer[] imgId = {R.drawable.elektr, R.drawable.benz, R.drawable.lpg, R.drawable.etan, R.drawable.diesel, R.drawable.cng};

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

        lat = bundle.getDouble("latitude");
        lon = bundle.getDouble("longitude");

        CoordinatorLayout coordinatorLayout = findViewById(R.id.popUpBackground);
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
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.child("coordinates").child("latitude").getValue().equals(lat) &&
                                ds.child("coordinates").child("longitude").getValue().equals(lon)) {
                            popedPetrol = ds;
                            break;
                        }
                    }

                    Iterator<DataSnapshot> items = popedPetrol.child("availableFuels").getChildren().iterator();
                    LinkedList<Integer> images = new LinkedList<>();
                    while (items.hasNext()) {
                        DataSnapshot item = items.next();
                        int pos = 0;
                        for (String str : imgNames) {
                            if (item.getKey().equals(str) && item.getValue().equals(true)) {
                                images.add(pos);
                                break;
                            }
                            pos++;
                        }
                    }
                    int counter;
                    for (int i = 0; i <= imgId.length/FUELS_IN_LINEAR_ROW; i++) {
                        LinearLayout row = new LinearLayout(context);
                        if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        else if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));

                        if(images.size() < FUELS_IN_LINEAR_ROW)
                            counter = images.size();
                        else
                            counter = FUELS_IN_LINEAR_ROW;

                        for (int j = 0; j < counter; j++) {
                            RelativeLayout.LayoutParams layoutParams;
                            if(context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                                layoutParams = new RelativeLayout.LayoutParams(width / 5, height / 17);
                            else
                                layoutParams = new RelativeLayout.LayoutParams(height / 5, width / 17);

                            ImageView img = new ImageView(context);
                            img.setPadding(5, 5, 5, 5);
                            img.setLayoutParams(layoutParams);
                            img.setImageResource(imgId[images.get(images.size()-1)]);
                            images.remove(images.size()-1);
                            row.addView(img);
                        }
                        availableFuelsLayout.addView(row);
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

                            TextView petrolName = findViewById(R.id.petrolName);
                            TextView petrolCoor= findViewById(R.id.petrolAddress);

                            petrolName.setText(popedPetrol.child("name").getValue().toString());
                            petrolCoor.setText(popedPetrol.child("address").getValue().toString());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                }

                sRef = new MyFirebaseStorage();
                try
                {
                    final File localFile = File.createTempFile("petrol_icon","png");
                    sRef.getPetrolIconRef(popedPetrol.child("name").getValue().toString()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final Button changeTypeBtn = findViewById(R.id.setChanges);
        changeTypeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ChangeFuelTypesActivity.class);
                intent.putExtra("latitude",lat);
                intent.putExtra("longitude",lon);
                intent.putExtra("petrolName",popedPetrol.child("name").getValue().toString());
                view.getContext().startActivity(intent);
            }
        });


        scale_up = AnimationUtils.loadAnimation(this,R.anim.scale_up);
        scale_down = AnimationUtils.loadAnimation(this,R.anim.scale_down);
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
                finish();
                startActivity(getIntent());
            }
        }

    }
}