package com.example.petrolnavigatorapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petrolnavigatorapp.ChangePriceActivity;
import com.example.petrolnavigatorapp.PetrolPopUpActivity;
import com.example.petrolnavigatorapp.R;
import com.example.petrolnavigatorapp.utils.Vehicle;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class VehiclesRecyclerViewAdapter extends RecyclerView.Adapter<VehiclesRecyclerViewAdapter.VehiclesRecyclerViewHolder>{

    private List<Vehicle> vehicleList;
    private Context context;

    public VehiclesRecyclerViewAdapter(List<Vehicle> vehicleList, Context context)
    {
        this.vehicleList = vehicleList;
        this.context = context;
    }

    @NonNull
    @Override
    public VehiclesRecyclerViewAdapter.VehiclesRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.vehicles_list_row,parent,false);
        return new VehiclesRecyclerViewAdapter.VehiclesRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VehiclesRecyclerViewAdapter.VehiclesRecyclerViewHolder holder, final int position) {
        holder.vehicleName.setText(vehicleList.get(position).getName());
        holder.averageFuelConsumption.setText(String.valueOf(vehicleList.get(position).getAverageFuelConsumption()));
        holder.fuelType.setText(vehicleList.get(position).getFuelType());
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public class VehiclesRecyclerViewHolder extends RecyclerView.ViewHolder {
        ImageView vehicleImage;
        TextView vehicleName;
        TextView averageFuelConsumption;
        TextView tankCapacity;
        TextView currentFuelLevel;
        TextView fuelType;
        Button deleteButton;

        VehiclesRecyclerViewHolder(View view)
        {
            super(view);
            vehicleImage = view.findViewById(R.id.vehicleImage);
            vehicleName = view.findViewById(R.id.vehicleName);
            averageFuelConsumption = view.findViewById(R.id.averageConsumption);
            fuelType = view.findViewById(R.id.vehicleFuelType);
            deleteButton = view.findViewById(R.id.deleteVehicleButton);
        }
    }
}
