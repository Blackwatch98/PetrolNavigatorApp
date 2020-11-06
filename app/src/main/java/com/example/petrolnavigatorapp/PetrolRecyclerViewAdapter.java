package com.example.petrolnavigatorapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PetrolRecyclerViewAdapter extends RecyclerView.Adapter<PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder> {

    private List<Fuel> fuelsList;
    private Context context;
    private LinkedList<Button> changeButtonsList;

    PetrolRecyclerViewAdapter(List<Fuel> fuels, Context con)
    {
        fuelsList = fuels;
        context = con;
        changeButtonsList = new LinkedList<>();
    }

    @NonNull
    @Override
    public PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.petrol_list_row,parent,false);
        return new PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PetrolRecyclerViewAdapter.PetrolRecyclerViewHolder holder, final int position) {
        holder.fuelIcon.setImageResource(fuelsList.get(position).getIcon());
        holder.dateText.setText(fuelsList.get(position).getName());
        holder.priceText.setText(fuelsList.get(position).getPrice() +"zł");

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date firstDate = sdf.parse(fuelsList.get(position).getLastReportDate());
            Date secondDate = new Date();

            long diffInMillies = Math.abs(secondDate.getTime() - firstDate.getTime());
            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            holder.dateText.setText(""+diff+" dni temu");
        }
        catch (ParseException e)
        {
            e.getMessage();
        }
        holder.reportText.setText("Zgłoszeń: "+fuelsList.get(position).getReportCounter());
        holder.priceText.setEnabled(false);
        changeButtonsList.add(holder.changeButton);

        holder.changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChangePriceActivity.class);
                intent.putExtra("fuelClass", String.valueOf(fuelsList.get(position).getPrice()));
                intent.putExtra("fuelName", fuelsList.get(position).getName());
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

    public class PetrolRecyclerViewHolder extends RecyclerView.ViewHolder {
       ImageView fuelIcon;
       EditText priceText;
       TextView dateText, reportText;
       Button changeButton;

       PetrolRecyclerViewHolder(View view)
       {
           super(view);
           fuelIcon = view.findViewById(R.id.imageView);
           priceText = view.findViewById(R.id.priceView);
           dateText = view.findViewById(R.id.dateText);
           reportText = view.findViewById(R.id.reportText);
           changeButton = view.findViewById(R.id.changePriceBtn);
       }
    }
}
