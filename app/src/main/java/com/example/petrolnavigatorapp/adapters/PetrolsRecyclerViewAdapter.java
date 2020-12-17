package com.example.petrolnavigatorapp.adapters;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.petrolnavigatorapp.ChangePriceActivity;
import com.example.petrolnavigatorapp.NavigationDrawerActivity;
import com.example.petrolnavigatorapp.PetrolPopUpActivity;
import com.example.petrolnavigatorapp.R;
import com.example.petrolnavigatorapp.dummy.DummyContent.DummyItem;
import com.example.petrolnavigatorapp.utils.Petrol;

import java.util.List;

public class PetrolsRecyclerViewAdapter extends RecyclerView.Adapter<PetrolsRecyclerViewAdapter.PetrolRecyclerViewHolder> {

    private final List<Petrol> petrols;
    private Context context;

    public PetrolsRecyclerViewAdapter(List<Petrol> items, Context context) {
        petrols = items;
        this.context = context;
    }

    @Override
    public PetrolRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.petrols_list_row,parent,false);
        return new PetrolsRecyclerViewAdapter.PetrolRecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PetrolRecyclerViewHolder holder, int position) {
        holder.petrol = petrols.get(position);

        holder.petrolName.setText(petrols.get(position).getName());
        holder.distance.setText("0m");
        holder.fuelPrice.setText("0zł");
        holder.reportDate.setText("0 dni temu");
    }

    @Override
    public int getItemCount() {
        return petrols.size();
    }

    public class PetrolRecyclerViewHolder extends RecyclerView.ViewHolder {
        public TextView petrolName;
        public TextView distance;
        public TextView fuelPrice;
        public TextView reportDate;
        public Petrol petrol;

        public PetrolRecyclerViewHolder(View view) {
            super(view);
            petrolName = view.findViewById(R.id.list_petrol_name);
            distance = view.findViewById(R.id.list_distance);
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