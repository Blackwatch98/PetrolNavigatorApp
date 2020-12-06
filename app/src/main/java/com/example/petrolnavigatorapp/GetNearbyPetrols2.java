package com.example.petrolnavigatorapp;


import android.content.Intent;
import android.os.AsyncTask;

import com.example.petrolnavigatorapp.utils.Petrol;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

public class GetNearbyPetrols2 extends AsyncTask<Object,String,String> {

    private GoogleMap mMap;
    private String url;
    private  String data;
    private NavigationDrawerActivity drawerActivity;
    private FirebaseFirestore fireStore;
    private List<Marker> markers;
    private TaskListener taskListener;
    private List<Petrol> petrolsList;

    GetNearbyPetrols2()
    {
        petrolsList = new LinkedList<>();
    };

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        markers = new LinkedList<>();
        drawerActivity = (NavigationDrawerActivity)objects[2];
        taskListener = (TaskListener)objects[3];

        try
        {
            URL myURL = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection)myURL.openConnection();
            httpURLConnection.connect();
            InputStream is = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();

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
                        Intent intent = new Intent(drawerActivity, PetrolPopUpActivity.class);
                        intent.putExtra("latitude", marker.getPosition().latitude);
                        intent.putExtra("longitude", marker.getPosition().longitude);
                        drawerActivity.startActivity(intent);

                        return false;
                    }
                });
            }

            fireStore = FirebaseFirestore.getInstance();
            final CollectionReference reff = fireStore.collection("petrol_stations");
            for(final Petrol petrol : petrolsList)
            {
                reff.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots)
                        {
                            double lat = Double.parseDouble(querySnapshot.get("lat").toString());
                            double lon = Double.parseDouble(querySnapshot.get("lon").toString());
                            if(petrol.getLat() == lat && petrol.getLon() == lon)
                                return;

                        }
                        reff.add(petrol);
                    }
                });
            }
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        taskListener.onTaskFinish(markers);
    }
}