package com.example.petrolnavigatorapp.realtime_database_utils;


import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.example.petrolnavigatorapp.NavigationDrawerActivity;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.example.petrolnavigatorapp.PetrolPopUpActivity;
import com.example.petrolnavigatorapp.interfaces.TaskListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class GetNearbyPetrols extends AsyncTask<Object,String,String> {

    private GoogleMap mMap;
    private String url;
    private NavigationDrawerActivity mapsActivity;
    private InputStream is;
    private BufferedReader bufferedReader;
    private StringBuilder stringBuilder;
    private  String data;

    private DatabaseReference reff;
    private List<Marker> markers;
    private TaskListener taskListener;

    private List<Petrol> petrolsList;
    private long counter = 0;
    private boolean  counterFlag = true;

    GetNearbyPetrols()
    {
        petrolsList = new LinkedList<>();
    };

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        markers = new LinkedList<>();
        mapsActivity = (NavigationDrawerActivity)objects[2];
        taskListener = (TaskListener)objects[3];

        try
        {
            URL myURL = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)myURL.openConnection();
            httpURLConnection.connect();
            is = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            stringBuilder = new StringBuilder();

            while((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line);
            }

            data = stringBuilder.toString();
        }
        catch(MalformedURLException e)
        {
            e.getMessage();
        }
        catch (IOException e)
        {
            e.getMessage();
        }

        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try
        {
            final JSONObject parentObject = new JSONObject(s);
            final JSONArray resultsArray = parentObject.getJSONArray("results");

            for(int i = 0; i < resultsArray.length(); i++)
            {
                JSONObject jsonObject = resultsArray.getJSONObject(i);
                JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");

                String latitude = locationObject.getString("lat");
                String longitude = locationObject.getString("lng");

                JSONObject nameObject = resultsArray.getJSONObject(i);
                final String petrolName = nameObject.getString("name");
                String vincity = nameObject.getString("vicinity");

                LatLng coor = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                petrolsList.add(new Petrol(petrolName,coor.latitude,coor.longitude,vincity));

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(petrolName+"," + vincity);
                markerOptions.position(coor);

                Marker mLocationMarker = mMap.addMarker(markerOptions);
                markers.add(mLocationMarker);

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Intent intent = new Intent(mapsActivity, PetrolPopUpActivity.class);
                        intent.putExtra("latitude", marker.getPosition().latitude);
                        intent.putExtra("longitude", marker.getPosition().longitude);
                        mapsActivity.startActivity(intent);

                        return false;
                    }
                });
            }

            reff = FirebaseDatabase.getInstance().getReference("Petrols");

            reff.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(counterFlag)
                    {
                        counter = dataSnapshot.getChildrenCount();
                        counterFlag = false;
                    }

                    if(dataSnapshot.exists()) {
                        if(dataSnapshot.getChildrenCount() < counter)
                            return;

                        for(Petrol ps : petrolsList) {
                            boolean isInDB = false;

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                LatLng temp = new LatLng(Double.parseDouble(ds.child("coordinates").child("latitude").getValue().toString()),
                                        Double.parseDouble(ds.child("coordinates").child("longitude").getValue().toString()));

//                                if (ps.getCoordinates().equals(temp)) {
//                                    isInDB = true;
//                                    break;
//                                }

                            }
                            if (!isInDB) {
                                reff.child(String.valueOf(counter)).setValue(ps);
                                counter++;
                            }
                        }
                    }
                    else
                    {
                        for(Petrol test : petrolsList)
                            reff.child(String.valueOf(counter)).setValue(test);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    System.out.println("Wystąpił błąd snapshota!");
                }
            });

        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        taskListener.onTaskFinish(markers);
    }
}