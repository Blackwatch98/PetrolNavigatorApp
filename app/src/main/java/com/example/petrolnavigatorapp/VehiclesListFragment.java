package com.example.petrolnavigatorapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petrolnavigatorapp.adapters.VehiclesRecyclerViewAdapter;
import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

/**
 * Fragment that presents user's collection of vehicles.
 */
public class VehiclesListFragment extends Fragment {

    private Context context;
    private List<Vehicle> vehiclesList;
    private FloatingActionButton addVehicleButton;
    private VehiclesListener listener;
    private RecyclerView recyclerView;

    public VehiclesListFragment() {
        vehiclesList = new LinkedList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //no arguments passed for now
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vehicles, container, false);
        context = view.getContext();

        recyclerView = (RecyclerView) view.findViewById(R.id.vehicles_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        addVehicleButton = view.findViewById(R.id.add_vehicle_button);
        addVehicleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ConfigureAddVehicleActivity.class);
                getActivity().startActivityForResult(intent, 1);
            }
        });
        getUserVehicles(view);
        return view;
    }

    /**
     * Gets user's collection of vehicles from database and present on the list.
     * @param view current fragment view
     */
    private void getUserVehicles(View view) {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        fireStore.collection("users").document(user.getUid()).collection("vehicles").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                            Vehicle vehicle = new Vehicle(
                                    query.getString("name"),
                                    query.getDouble("tankCapacity"),
                                    query.getDouble("averageFuelConsumption"),
                                    Integer.parseInt(query.get("fuelTypeId").toString()),
                                    Double.parseDouble(query.get("currentFuelLevel").toString()),
                                    Double.parseDouble(query.get("reserveFuelLevel").toString())
                            );
                            vehiclesList.add(vehicle);
                        }
                        listener.getUserVehicles(vehiclesList);
                        VehiclesRecyclerViewAdapter adapter = new VehiclesRecyclerViewAdapter(vehiclesList, context);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.getMessage();
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (VehiclesListFragment.VehiclesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implements VehiclesListener");
        }
    }

    /**
     * Interface used to pass new added vehicles list to NavigationDrawerActivity.
     */
    public interface VehiclesListener {
        void getUserVehicles(List<Vehicle> userVehicles);
    }
}