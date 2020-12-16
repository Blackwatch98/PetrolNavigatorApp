package com.example.petrolnavigatorapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FilterDialog extends AppCompatDialogFragment {

    private Spinner fuelTypeSpinner;
    private Spinner fuelSpinner;
    final private int [] fuelTypesResource = {R.array.Benzyna,R.array.Diesel,R.array.LPG,R.array.Etanol,R.array.Elektryczny,R.array.CNG};
    private FilterDialogListener listener;
    private ArrayAdapter typeSpinnerAdapter, fuelSpinnerAdapter;
    boolean isAlreadyWorking = false;
    private String prefType, prefFuel;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog, null);

        fuelTypeSpinner = view.findViewById(R.id.spinner);
        fuelSpinner = view.findViewById(R.id.spinner2);
        typeSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.fuelTypes, android.R.layout.simple_spinner_item);
        typeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuelTypeSpinner.setAdapter(typeSpinnerAdapter);

        builder.setView(view)
                .setTitle("Filtruj")
                .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getContext(), "Anulowano", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Potwierdź", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String fuelType = fuelTypeSpinner.getSelectedItem().toString();
                        String fuel = fuelSpinner.getSelectedItem().toString();
                        listener.changeUserPreferences(fuelType,fuel);
                    }
                });

        setLastPreferences();

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (FilterDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implements FilterDialogListener");
        }
    }

    private void fillSubspinner(int resource, boolean isEverything) {
        if(isEverything)
        {
            List<String> fuels = new ArrayList<>();
            for(int res : fuelTypesResource)
            {
                String[] row = getResources().getStringArray(res);
                for(String item : row)
                    fuels.add(item);
            }
            fuelSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fuels);
            fuelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fuelSpinner.setAdapter(fuelSpinnerAdapter);
        }
        else
        {
            fuelSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), resource, android.R.layout.simple_spinner_item);
            fuelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fuelSpinner.setAdapter(fuelSpinnerAdapter);
        }

        if(isAlreadyWorking)
        {
            fuelSpinner.setSelection(fuelSpinnerAdapter.getPosition(prefFuel));
            isAlreadyWorking = false;
        }
    }

    public interface FilterDialogListener{
        void changeUserPreferences(String prefType, String prefFuel);
    }

    private void setLastPreferences()
    {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DocumentReference userDocument = fireStore.collection("users").document(mAuth.getCurrentUser().getUid());
        userDocument.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    prefType = documentSnapshot.get("userSettings.prefFuelType").toString();
                    prefFuel = documentSnapshot.get("userSettings.prefFuel").toString();
                    List<String> fuels = new ArrayList<>();
                    if(prefType.equals("Wszystko")) {

                        for (int res : fuelTypesResource) {
                            String[] row = getResources().getStringArray(res);
                            for (String item : row)
                                fuels.add(item);
                        }
                    }
                    else
                    {
                        int id = getResources().getIdentifier(prefType, "array", getActivity().getPackageName());
                        String [] array = getResources().getStringArray(id);
                        for (String item : array)
                            fuels.add(item);
                    }
                    fuelSpinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fuels);
                    fuelSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    fuelSpinner.setAdapter(fuelSpinnerAdapter);
                    fuelTypeSpinner.setSelection(typeSpinnerAdapter.getPosition(prefType));
                    isAlreadyWorking = true;
                }
                fuelTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                        if(adapterView.getItemAtPosition(position).equals("Benzyna"))
                            fillSubspinner(fuelTypesResource[0], false);
                        else if(adapterView.getItemAtPosition(position).equals("Diesel"))
                            fillSubspinner(fuelTypesResource[1], false);
                        else if(adapterView.getItemAtPosition(position).equals("LPG"))
                            fillSubspinner(fuelTypesResource[2], false);
                        else if(adapterView.getItemAtPosition(position).equals("Etanol"))
                            fillSubspinner(fuelTypesResource[3], false);
                        else if(adapterView.getItemAtPosition(position).equals("Elektryczny"))
                            fillSubspinner(fuelTypesResource[4], false);
                        else if(adapterView.getItemAtPosition(position).equals("CNG"))
                            fillSubspinner(fuelTypesResource[5], false);
                        else if(adapterView.getItemAtPosition(position).equals("Wszystko"))
                            fillSubspinner(0, true);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        });
    }
}
