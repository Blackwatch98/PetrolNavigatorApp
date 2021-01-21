package com.example.petrolnavigatorapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petrolnavigatorapp.adapters.PetrolsRecyclerViewAdapter;
import com.example.petrolnavigatorapp.adapters.VehiclesRecyclerViewAdapter;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;
import java.util.List;

public class VehiclesFragment extends Fragment {

    private Context context;
    private List<Vehicle> vehicleList = new LinkedList<>();
    private FloatingActionButton addVehicleButton;
    private VehiclesListener listener;
    private RecyclerView recyclerView;

    public VehiclesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vehicles, container, false);
        context = view.getContext();

        recyclerView = (RecyclerView) view.findViewById(R.id.vehicles_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false);
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


    private void getUserVehicles(View view) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        firestore.collection("users").document(user.getUid()).collection("vehicles").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for(QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    Vehicle vehicle = new Vehicle(
                            query.getString("name"),
                            query.getDouble("tankCapacity"),
                            query.getDouble("averageFuelConsumption"),
                            Integer.parseInt(query.get("fuelTypeId").toString()),
                            Integer.parseInt(query.get("currentFuelLevel").toString()),
                            Integer.parseInt(query.get("reserveFuelLevel").toString())
                    );
                    vehicleList.add(vehicle);
                }
                listener.getUserVehicles(vehicleList);


                VehiclesRecyclerViewAdapter adapter = new VehiclesRecyclerViewAdapter(vehicleList, context);


                recyclerView.setAdapter(adapter);

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (VehiclesFragment.VehiclesListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implements VehiclesListener");
        }
    }

    public interface VehiclesListener{
        void getUserVehicles(List<Vehicle> userVehicles);
    }


}