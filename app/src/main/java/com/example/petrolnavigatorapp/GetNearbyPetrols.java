package com.example.petrolnavigatorapp;


import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

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
    private MapsActivity mapsActivity;
    private InputStream is;
    private BufferedReader bufferedReader;
    private StringBuilder stringBuilder;
    private  String data;

    //Firebase
    private DatabaseReference reff;
    private Petrol petrol;
    private long maxId = 0;
    private List<Marker> markers;
    private TaskListener taskListener;
    private DataSnapshot myDs;

    GetNearbyPetrols()
    {
    };

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        markers = new LinkedList<>();
        mapsActivity = (MapsActivity)objects[2];
        taskListener = (TaskListener)objects[2];


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
        System.out.println(data);
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        try
        {
            JSONObject parentObject = new JSONObject(s);
            JSONArray resultsArray = parentObject.getJSONArray("results");
            System.out.println(resultsArray.length());
            for(int i = 0; i < resultsArray.length(); i++)
            {
                JSONObject jsonObject = resultsArray.getJSONObject(i);
                JSONObject locationObject = jsonObject.getJSONObject("geometry").getJSONObject("location");

                String latitude = locationObject.getString("lat");
                String longitude = locationObject.getString("lng");

                JSONObject nameObject = resultsArray.getJSONObject(i);
                String name_petrol = nameObject.getString("name");
                String vincity = nameObject.getString("vicinity");

                LatLng latLng = new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude));
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title(name_petrol+"," + vincity);
                markerOptions.position(latLng);

                Marker mLocationMarker = mMap.addMarker(markerOptions);
                markers.add(mLocationMarker);

                reff = FirebaseDatabase.getInstance().getReference().child("Petrol");

                //po wciśnięciu na marker ustaw paliwa
                //mMap.setOnMarkerClickListener();
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        //MarkerInfo m = (MarkerInfo)map.get(marker);
                        //FragmentManager fragmentManager = mapsActivity.getSupportFragmentManager();
                        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        //PetrolMarkerFragment fragment = new PetrolMarkerFragment();
                        //fragmentTransaction.add(R.id.map, fragment);
                        //fragmentTransaction.commit();

                        /*
                        Intent intent = new Intent(mapsActivity, PopUpActivity.class);
                        intent.putExtra("petrolName", marker.getTitle());
                        intent.putExtra("latitude",marker.getPosition().latitude);
                        intent.putExtra("longitude",marker.getPosition().longitude);
                        mapsActivity.startActivity(intent);

                         */
                        return false;
                    }
                });


                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists())
                        {
                            maxId = dataSnapshot.getChildrenCount();
                        }
                        myDs = dataSnapshot;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

//                if(!myDs.exists())
//                {
                    petrol = new Petrol(name_petrol,latLng);
                    reff.child(String.valueOf(maxId+i)).setValue(petrol);
       //          }


            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        taskListener.onTaskFinish(markers);
    }


}
