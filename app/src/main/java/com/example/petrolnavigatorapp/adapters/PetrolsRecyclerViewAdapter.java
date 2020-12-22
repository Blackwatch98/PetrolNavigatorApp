package com.example.petrolnavigatorapp.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.petrolnavigatorapp.ChangePriceActivity;
import com.example.petrolnavigatorapp.NavigationDrawerActivity;
import com.example.petrolnavigatorapp.PetrolPopUpActivity;
import com.example.petrolnavigatorapp.R;
import com.example.petrolnavigatorapp.firebase_utils.MyFirebaseStorage;
import com.example.petrolnavigatorapp.utils.Fuel;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PetrolsRecyclerViewAdapter extends RecyclerView.Adapter<PetrolsRecyclerViewAdapter.PetrolRecyclerViewHolder> {

    private final List<Petrol> petrols;
    private final double lat, lon;
    private final String prefFuel, prefType;
    private final Context context;
    private MyFirebaseStorage storage = new MyFirebaseStorage();

    public PetrolsRecyclerViewAdapter(List<Petrol> items, double lat, double lon, String prefType, String prefFuel, Context context) {
        petrols = items;
        this.lat = lat;
        this.lon = lon;
        this.context = context;
        this.prefFuel = prefFuel;
        this.prefType = prefType;
    }

    @Override
    public PetrolRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.petrols_list_row,parent,false);
        return new PetrolsRecyclerViewAdapter.PetrolRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PetrolRecyclerViewHolder holder, int position) {
        holder.petrol = petrols.get(position);
        storage.getPetrolIconRef(holder.petrol.getName());

        String distance = getDistance(lat,lon,holder.petrol.getLat(),holder.petrol.getLon())+ "m";
        Fuel fuel = getPrefFuel(holder.petrol, prefType, prefFuel);

        getFuelIcon(holder);
        holder.distanceText.setText(distance);
        holder.petrolName.setText(petrols.get(position).getName());
        if(fuel != null){
            holder.fuelPrice.setText(fuel.getPrice());
            holder.reportDate.setText(getDaysDifference(fuel.getLastReportDate()));
        }

    }

    private void getFuelIcon(final PetrolRecyclerViewHolder holder) {
        try
        {
            final File localFile = File.createTempFile("petrol_icon","png");
            storage.getPetrolIconRef(holder.petrol.getName()).getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    holder.petrolIcon.setImageBitmap(bitmap);
                }
            });
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private Fuel getPrefFuel(Petrol petrol, String typeName, String fuelName) {
        HashMap<String, Boolean> types = petrol.getAvailableFuels();
        List<Fuel> fuels = petrol.getFuels();

        if(fuelName.equals("Wszystko"))
        {
            if(typeName.equals("Wszystko"))
            {
                String availableType = null;
                for(String name : types.keySet())
                    if(types.get(name)){
                        availableType = name;
                        break;
                    }
                for(Fuel f : fuels)
                    if(f.getType().equals(availableType))
                        return f;
            }
            else {
                for(Fuel f : fuels)
                    if(f.getType().equals(typeName))
                        return f;
            }
        }
        else
            for(Fuel f : fuels)
                if(f.getName().equals(fuelName))
                    return f;

        return null;
    }

    @Override
    public int getItemCount() {
        return petrols.size();
    }

    private String getDistance(double lat1, double lon1, double lat2, double lon2)
    {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);
        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);
        double distance = location1.distanceTo(location2);

        return String.format(Locale.ENGLISH, "%.1f", distance);
    }

    private String getDaysDifference(String date)
    {
        String difference = null;
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date firstDate = sdf.parse(date);
            Date secondDate = new Date();

            long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            difference = ""+diff+" dni temu";

        }
        catch (ParseException e)
        {
            e.getMessage();
        }
        return difference;
    }

    public class PetrolRecyclerViewHolder extends RecyclerView.ViewHolder {
        public ImageView petrolIcon;
        public TextView petrolName;
        public TextView distanceText;
        public TextView fuelPrice;
        public TextView reportDate;
        public Petrol petrol;

        public PetrolRecyclerViewHolder(View view) {
            super(view);
            petrolIcon = view.findViewById(R.id.petrol_icon);
            petrolName = view.findViewById(R.id.list_petrol_name);
            distanceText = view.findViewById(R.id.list_distance);
            fuelPrice = view.findViewById(R.id.list_price);
            reportDate = view.findViewById(R.id.list_reportDate);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PetrolPopUpActivity.class);
                    intent.putExtra("latitude", petrol.getLat());
                    intent.putExtra("longitude", petrol.getLon());
                    ((NavigationDrawerActivity)context).startActivityForResult(intent, 1);
                }
            });
        }


    }
}