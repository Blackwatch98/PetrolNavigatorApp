package com.example.petrolnavigatorapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PetrolRecyclerViewAdapter extends RecyclerView.Adapter<PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder> {

    private List<Fuel> fuelsList;
    private Context context;

    PetrolRecyclerViewAdapter(List<Fuel> fuels, Context con)
    {
        fuelsList = fuels;
        context = con;
    }

    @NonNull
    @Override
    public PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.petrol_list_row,parent,false);
        return new PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder holder, int position) {
        holder.fuelIcon.setImageResource(fuelsList.get(position).getIcon());
        holder.dateText.setText(fuelsList.get(position).getName());
        holder.priceText.setText(Double.toString(fuelsList.get(position).getPrice()) +"zł");
        holder.priceText.setEnabled(false);
    }

    @Override
    public int getItemCount() {
        return fuelsList.size();
    }

    public class PetrolRecyclerViewHolder extends RecyclerView.ViewHolder {
       ImageView fuelIcon;
       EditText priceText;
       TextView dateText, reportText;

       PetrolRecyclerViewHolder(View view)
       {
           super(view);
           fuelIcon = view.findViewById(R.id.imageView);
           priceText = view.findViewById(R.id.priceView);
           dateText = view.findViewById(R.id.dateText);
           reportText = view.findViewById(R.id.reportText);
       }
    }
}
