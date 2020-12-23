package com.example.petrolnavigatorapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDialogFragment;

public class ListFilterDialog extends AppCompatDialogFragment {

    private OrderPrefListener listener;
    private RadioGroup prefsRadioGroup;
    private RadioButton radioButton;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.list_filter_dialog, null);

        prefsRadioGroup = view.findViewById(R.id.prefsRadioGroup);
        prefsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                radioButton = view.findViewById(id);
                System.out.println(radioButton.getText());
            }
        });

        String orderPrefs;
        SharedPreferences shared = getActivity().getPreferences(Context.MODE_PRIVATE);
        orderPrefs = shared.getString("orderPrefs", "");
        if(orderPrefs.equals("Distance") || orderPrefs == null)
            radioButton = view.findViewById(R.id.radioButton);
        else if(orderPrefs.equals("Price"))
            radioButton = view.findViewById(R.id.radioButton2);
        else
            radioButton = view.findViewById(R.id.radioButton3);

        radioButton.setChecked(true);

        builder.setView(view);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OrderPrefListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()+" must implements OrderPrefListener");
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(radioButton.getText().equals("Odległość"))
            editor.putString("orderPrefs", "Distance");
        else if(radioButton.getText().equals("Ceny paliwa"))
            editor.putString("orderPrefs", "Price");
        else
            editor.putString("orderPrefs", "Date");

        editor.apply();
        listener.refreshList();
    }

    public interface OrderPrefListener{
        void refreshList();
    }
}
