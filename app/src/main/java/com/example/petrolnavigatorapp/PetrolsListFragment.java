package com.example.petrolnavigatorapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petrolnavigatorapp.adapters.PetrolsRecyclerViewAdapter;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.google.type.LatLng;

import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * A fragment representing a list of Items.
 */
public class PetrolsListFragment extends Fragment {

    private List<Petrol> petrols;
    private double lat, lon;
    private String prefFuel, prefType;
    private Context context;

    public PetrolsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            petrols = (List<Petrol>) getArguments().getSerializable("petrols");
            lat = getArguments().getDouble("lat");
            lon = getArguments().getDouble("lon");
            prefFuel = getArguments().getString("prefFuel");
            prefType = getArguments().getString("prefType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_petrols_list, container, false);

        if (view instanceof RecyclerView) {
            context = view.getContext();

            SharedPreferences shared = getActivity().getPreferences(Context.MODE_PRIVATE);
            String orderPrefs = shared.getString("orderPrefs", "");

            if(orderPrefs != null)
            {
                List<Petrol> newOrderList = getOrderPref(orderPrefs);
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
                PetrolsRecyclerViewAdapter adapter = new PetrolsRecyclerViewAdapter(newOrderList, lat, lon, prefType, prefFuel, context);

                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
            else
            {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
                PetrolsRecyclerViewAdapter adapter = new PetrolsRecyclerViewAdapter(petrols, lat, lon, prefType, prefFuel, context);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
        }
        return view;
    }

    public List<Petrol> getOrderPref(String orderPref) {
        QuickSort sort = new QuickSort();
        List<Petrol> newOrderList;

        if (orderPref.equals("Distance"))
            newOrderList = sort.getSortedByDistance(petrols, lat, lon);
        else if(orderPref.equals("Price"))
            newOrderList = sort.getSortedByPrice(petrols, prefFuel, prefType);
        else
            newOrderList = sort.getSortedByLastReportDate(petrols, prefFuel, prefType);

        return  newOrderList;
    }
}