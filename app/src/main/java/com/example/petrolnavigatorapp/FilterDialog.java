package com.example.petrolnavigatorapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FilterDialog extends AppCompatDialogFragment {

    private Spinner fuelTypeSpinner;
    private Spinner fuelSpinner;
    private List<String> subcategories = new ArrayList<>();
    private int [] fuelTypesResource = {R.array.Benzyna,R.array.Diesel,R.array.LPG,R.array.Etanol,R.array.Elektryczny,R.array.CNG};
    private FilterDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog, null);

        fuelTypeSpinner = view.findViewById(R.id.spinner);
        fuelSpinner = view.findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.fuelTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fuelTypeSpinner.setAdapter(adapter);
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
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, fuels);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fuelSpinner.setAdapter(adapter2);
        }
        else
        {
            ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(), resource, android.R.layout.simple_spinner_item);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fuelSpinner.setAdapter(adapter2);
        }
    }

    public interface FilterDialogListener{
        void changeUserPreferences(String prefType, String prefFuel);
    }
}
