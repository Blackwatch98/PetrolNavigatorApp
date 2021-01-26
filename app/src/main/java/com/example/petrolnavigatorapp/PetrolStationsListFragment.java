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
import com.example.petrolnavigatorapp.services.QuickSortService;
import com.example.petrolnavigatorapp.utils.Petrol;

import java.util.List;

/**
 * A fragment representing a list of sorted nearby petrol stations.
 * To present any data it is required to visit MapsFragment first.
 */
public class PetrolStationsListFragment extends Fragment {

    private List<Petrol> petrolStationsList;
    private double lat, lon;
    private String prefFuel, prefType;
    private Context context;

    public PetrolStationsListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            petrolStationsList = (List<Petrol>) getArguments().getSerializable("petrols");
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
            RecyclerView recyclerView = (RecyclerView) view;
            PetrolsRecyclerViewAdapter adapter;
            RecyclerView.LayoutManager layoutManager;

            if (orderPrefs != null) {
                List<Petrol> newOrderList = getOrderPref(orderPrefs);
                layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                adapter = new PetrolsRecyclerViewAdapter(newOrderList, lat, lon, prefType, prefFuel, context);
                recyclerView.setLayoutManager(layoutManager);
            } else {
                layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
                adapter = new PetrolsRecyclerViewAdapter(petrolStationsList, lat, lon, prefType, prefFuel, context);
            }
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        return view;
    }

    /**
     * Returns list of petrol stations in order requested by user.
     *
     * @param orderPref Preference of list's elements order type. Available: Distance, Price, LastReportDate
     * @return sorted list of found petrol stations.
     */
    public List<Petrol> getOrderPref(String orderPref) {
        QuickSortService sort = new QuickSortService();
        List<Petrol> newOrderList;

        if (orderPref.equals("Distance"))
            newOrderList = sort.getSortedByDistance(petrolStationsList, lat, lon);
        else if (orderPref.equals("Price"))
            newOrderList = sort.getSortedByPrice(petrolStationsList, prefFuel, prefType);
        else
            newOrderList = sort.getSortedByLastReportDate(petrolStationsList, prefFuel, prefType);

        return newOrderList;
    }
}