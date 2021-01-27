package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Create and edit vehicle menu.
 */
public class ConfigureAddVehicleActivity extends Activity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private Context context;
    private Vehicle currentVehicle;
    private EditText vehicleNameEditText, averageConsumptionEditText, tankCapacityEditText;
    private Spinner currentFuelLevelSpinner, vehicleFuelTypeSpinner, fuelReserveSpinner;
    private Button confirmButton, cancelButton;
    private CoordinatorLayout coordinatorLayout;
    private QueryDocumentSnapshot currentVehicleDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_add_vehicle);

        context = this;
        vehicleNameEditText = findViewById(R.id.editTextVehicleName);
        averageConsumptionEditText = findViewById(R.id.editTextAverageConsumption);
        tankCapacityEditText = findViewById(R.id.editTextVehicleCapacity);
        currentFuelLevelSpinner = findViewById(R.id.currentFuelLevelSpinner);
        vehicleFuelTypeSpinner = findViewById(R.id.vehicleFuelSpinner);
        fuelReserveSpinner = findViewById(R.id.fuelReserveSpinner);
        coordinatorLayout = findViewById(R.id.add_vehicle_background);
        coordinatorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        confirmButton = findViewById(R.id.confirmButton2);
        cancelButton = findViewById(R.id.cancelButton2);

        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.fuelTypes, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleFuelTypeSpinner.setAdapter(typeSpinnerAdapter);

        final ArrayAdapter fuelLevelAdapter = ArrayAdapter.createFromResource(this, R.array.fractions, android.R.layout.simple_spinner_item);
        fuelLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        currentFuelLevelSpinner.setAdapter(fuelLevelAdapter);
        fuelReserveSpinner.setAdapter(fuelLevelAdapter);

        final CollectionReference collectionReference = firestore.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("vehicles");
        final DocumentReference documentReference = collectionReference.document();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String vehicleName = bundle.getString("name");
            if (vehicleName != null) {
                loadVehicle(vehicleName, collectionReference);
            }
        } else
            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    documentReference.set(new Vehicle(
                            vehicleNameEditText.getText().toString(),
                            Double.parseDouble(tankCapacityEditText.getText().toString()),
                            Double.parseDouble(averageConsumptionEditText.getText().toString()),
                            (int) vehicleFuelTypeSpinner.getSelectedItemId(),
                            currentFuelLevelSpinner.getSelectedItemId() * Double.parseDouble(tankCapacityEditText.getText().toString()) / 8,
                            fuelReserveSpinner.getSelectedItemId() * Double.parseDouble(tankCapacityEditText.getText().toString()) / 8))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(context, "Skonfigurowano pomyślnie!", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, "Nie udało się skonfigurować...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent i = new Intent();
                    setResult(RESULT_OK, i);
                    finish();
                }
            });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    /**
     * Loads selected from VehiclesListFragment vehicle data to inputs.
     *
     * @param vehicleName Name of the vehicle.
     * @param ref         Reference to current user's vehicles collections in database.
     */
    private void loadVehicle(String vehicleName, CollectionReference ref) {
        ref.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    if (query.getString("name").equals(vehicleName)) {
                        currentVehicle = new Vehicle(
                                query.getString("name"),
                                query.getDouble("tankCapacity"),
                                query.getDouble("averageFuelConsumption"),
                                Integer.parseInt(query.get("fuelTypeId").toString()),
                                Double.parseDouble(query.get("currentFuelLevel").toString()),
                                Double.parseDouble(query.get("reserveFuelLevel").toString())
                        );
                        currentVehicleDocument = query;
                        break;
                    }
                }
                double tankCapacity = currentVehicle.getTankCapacity();

                vehicleNameEditText.setText(currentVehicle.getName());
                tankCapacityEditText.setText(String.valueOf(tankCapacity));
                averageConsumptionEditText.setText(String.valueOf(currentVehicle.getAverageFuelConsumption()));
                vehicleFuelTypeSpinner.setSelection(currentVehicle.getFuelTypeId());
                currentFuelLevelSpinner.setSelection((int) (currentVehicle.getCurrentFuelLevel() / tankCapacity * 8));
                fuelReserveSpinner.setSelection((int) (currentVehicle.getReserveFuelLevel() / tankCapacity * 8));

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ref.document(currentVehicleDocument.getId()).update(
                                "name", vehicleNameEditText.getText().toString(),
                                "tankCapacity", Double.parseDouble(tankCapacityEditText.getText().toString()),
                                "averageFuelConsumption", Double.parseDouble(averageConsumptionEditText.getText().toString()),
                                "fuelTypeId", (int) vehicleFuelTypeSpinner.getSelectedItemId(),
                                "currentFuelLevel", currentFuelLevelSpinner.getSelectedItemId() * tankCapacity / 8,
                                "reserveFuelLevel", fuelReserveSpinner.getSelectedItemId() * tankCapacity / 8
                        );
                        Intent i = new Intent();
                        setResult(RESULT_OK, i);
                        finish();
                    }
                });
            }
        });
    }
}