package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfigureAddVehicleActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private Context context;

    private EditText vehicleName, averageConsumption, tankCapacity;
    private Spinner currentFuelLevel, vehicleFuelType;
    private Button confirmButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_add_vehicle);

        context = this;

        vehicleName = findViewById(R.id.editTextVehicleName);
        averageConsumption = findViewById(R.id.editTextAverageConsumption);
        tankCapacity = findViewById(R.id.editTextVehicleCapacity);
        currentFuelLevel = findViewById(R.id.currentFuelLevelSpinner);
        vehicleFuelType = findViewById(R.id.vehicleFuelSpinner);

        ArrayAdapter typeSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.fuelTypes, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vehicleFuelType.setAdapter(typeSpinnerAdapter);

        final ArrayAdapter fuelLevelAdapter = ArrayAdapter.createFromResource(this, R.array.fractions, android.R.layout.simple_spinner_item);
        fuelLevelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currentFuelLevel.setAdapter(fuelLevelAdapter);

        confirmButton = findViewById(R.id.confirmButton2);
        cancelButton = findViewById(R.id.cancelButton2);

        final DocumentReference documentReference = firestore.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("vehicles").document();

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                documentReference.set(new Vehicle(
                        vehicleName.getText().toString(),
                        Double.parseDouble(tankCapacity.getText().toString()),
                        Double.parseDouble(averageConsumption.getText().toString()),
                        vehicleFuelType.getSelectedItem().toString(),
                        currentFuelLevel.getSelectedItemId()))
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
}