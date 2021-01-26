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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petrolnavigatorapp.ChangePriceActivity;
import com.example.petrolnavigatorapp.ConfigureAddVehicleActivity;
import com.example.petrolnavigatorapp.NavigationDrawerActivity;
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
        holder.averageFuelConsumption.setText("Średnie spalanie: " + vehicleList.get(position).getAverageFuelConsumption());
        holder.fuelType.setText("Paliwo: " + vehicleList.get(position).getFuelTypeId());
        holder.tankCapacity.setText("Pojemność baku: " + String.valueOf(vehicleList.get(position).getTankCapacity() + "l"));
        holder.currentFuelLevel.setText("Stan baku: " + vehicleList.get(position).getCurrentFuelLevel() + "l");
        holder.vehicleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ConfigureAddVehicleActivity.class);
                intent.putExtra("name", vehicleList.get(position).getName());
                ((NavigationDrawerActivity)context).startActivityForResult(intent,1);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    public class VehiclesRecyclerViewHolder extends RecyclerView.ViewHolder {
        CardView vehicleCard;
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
            vehicleCard = view.findViewById(R.id.vehicleCard);
            vehicleImage = view.findViewById(R.id.vehicleImage);
            vehicleName = view.findViewById(R.id.vehicleName);
            averageFuelConsumption = view.findViewById(R.id.averageConsumption);
            tankCapacity = view.findViewById(R.id.tankCapacity);
            fuelType = view.findViewById(R.id.vehicleFuelType);
            currentFuelLevel = view.findViewById(R.id.fuelLevel);
            deleteButton = view.findViewById(R.id.deleteVehicleButton);
        }
    }
}
