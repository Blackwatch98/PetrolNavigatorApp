package com.example.petrolnavigatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

public class ChangePriceActivity extends AppCompatActivity {

    private NumberPicker integer_picker;
    private NumberPicker fraction_picker;
    private NumberPicker fraction_picker2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_price);

        Button confirmBtn;

        integer_picker = findViewById(R.id.integer_picker);
        fraction_picker = findViewById(R.id.fraction_picker);
        fraction_picker2 = findViewById(R.id.fraction_picker2);
        confirmBtn = findViewById(R.id.confirmBtn);

        Bundle bundle = getIntent().getExtras();
        String values = bundle.getString("fuelClass");

        integer_picker.setMinValue(0);
        integer_picker.setMaxValue(9);
        fraction_picker.setMinValue(0);
        fraction_picker.setMaxValue(9);
        fraction_picker2.setMinValue(0);
        fraction_picker2.setMaxValue(9);

        integer_picker.setValue(Character.getNumericValue((values.charAt(0))));
        fraction_picker.setValue(Character.getNumericValue((values.charAt(2))));
        fraction_picker2.setValue(Character.getNumericValue((values.charAt(3))));

        confirmBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(integer_picker.getValue()).append(".").
                        append(fraction_picker.getValue()).append(fraction_picker2.getValue());

                Intent i = new Intent();
                i.putExtra("priceString",stringBuilder.toString());
                i.putExtra("fuelName","Pb98");
                setResult(RESULT_OK,i);

                finish();
            }
        });
    }
}