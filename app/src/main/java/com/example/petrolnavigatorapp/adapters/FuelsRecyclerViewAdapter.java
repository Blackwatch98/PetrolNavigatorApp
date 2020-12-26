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
import com.example.petrolnavigatorapp.utils.Fuel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FuelsRecyclerViewAdapter extends RecyclerView.Adapter<FuelsRecyclerViewAdapter.FuelRecyclerViewHolder> {

    private List<Fuel> fuelsList;
    private Context context;
    private LinkedList<Button> changeButtonsList;
    private String petrolId;
    private boolean isMinimalDistanceReached;

    public FuelsRecyclerViewAdapter(List<Fuel> fuels, Context con, String petrolId, Boolean isMinimalDistanceReached)
    {
        fuelsList = fuels;
        context = con;
        this.petrolId = petrolId;
        changeButtonsList = new LinkedList<>();
        this.isMinimalDistanceReached = isMinimalDistanceReached;
    }

    @NonNull
    @Override
    public FuelsRecyclerViewAdapter.FuelRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.fuel_list_row,parent,false);
        return new FuelsRecyclerViewAdapter.FuelRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(FuelsRecyclerViewAdapter.FuelRecyclerViewHolder holder, final int position) {
        holder.fuelIcon.setImageResource(fuelsList.get(position).getIcon());
        holder.dateText.setText(fuelsList.get(position).getName());
        if(fuelsList.get(position).getPrice().equals("0.00"))
            holder.priceText.setText("Brak");
        else
            holder.priceText.setText(fuelsList.get(position).getPrice() +"zł");

        if(fuelsList.get(position).getLastReportDate().equals(null))
            holder.dateText.setText("Brak");
        else {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date firstDate = sdf.parse(fuelsList.get(position).getLastReportDate());
                Date secondDate = new Date();

                long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                holder.dateText.setText("" + diff + " dni temu");
            } catch (ParseException e) {
                e.getMessage();
            }
        }
        holder.priceText.setEnabled(false);
        changeButtonsList.add(holder.changeButton);

        holder.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isMinimalDistanceReached) {
                    Toast.makeText(context, "Jesteś zbyt daleko, aby wykonać zgłoszenie!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, ChangePriceActivity.class);
                intent.putExtra("fuelClass", String.valueOf(fuelsList.get(position).getPrice()));
                intent.putExtra("fuelName", fuelsList.get(position).getName());
                intent.putExtra("petrolId", petrolId);
                ((PetrolPopUpActivity)context).startActivityForResult(intent, 1);
            }
        });
    }
    public LinkedList<Button> getButtonsList()
    {
        return changeButtonsList;
    }

    @Override
    public int getItemCount() {
        return fuelsList.size();
    }

    public class FuelRecyclerViewHolder extends RecyclerView.ViewHolder {
       ImageView fuelIcon;
       EditText priceText;
       TextView dateText;
       Button changeButton;

       FuelRecyclerViewHolder(View view)
       {
           super(view);
           fuelIcon = view.findViewById(R.id.imageView);
           priceText = view.findViewById(R.id.priceView);
           dateText = view.findViewById(R.id.dateText);
           changeButton = view.findViewById(R.id.changePriceBtn);
       }
    }
}
