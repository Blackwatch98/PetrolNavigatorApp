package com.example.petrolnavigatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.petrolnavigatorapp.services.UsersReportService;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Activity that holds sending reports that concern fuel prices.
 * For now only manual input or by using spinners are available.
 */
public class ChangePriceActivity extends AppCompatActivity {

    private final double PRICE_CHANGE_TOLERANCE = 1.5;
    private String fuelName;
    private String petrolId;
    private FirebaseFirestore fireStore;
    private NumberPicker integer_picker, fraction_picker, fraction_picker2;

    private DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_price);

        final Button confirmBtn, cancelBtn, voiceBtn, fotoBtn;
        TextView fuelNameTextView;

        fuelNameTextView = findViewById(R.id.fuelName);
        integer_picker = findViewById(R.id.integer_picker);
        fraction_picker = findViewById(R.id.fraction_picker);
        fraction_picker2 = findViewById(R.id.fraction_picker2);
        confirmBtn = findViewById(R.id.confirmBtn);
        cancelBtn = findViewById(R.id.cancelBtn2);
        voiceBtn = findViewById(R.id.voiceBtn);
        fotoBtn = findViewById(R.id.fotoBtn);

        Bundle bundle = getIntent().getExtras();
        final String values = bundle.getString("fuelClass");
        fuelName = bundle.getString("fuelName");
        petrolId = bundle.getString("petrolId");

        fuelNameTextView.setText(fuelName);

        integer_picker.setMinValue(0);
        integer_picker.setMaxValue(9);
        fraction_picker.setMinValue(0);
        fraction_picker.setMaxValue(9);
        fraction_picker2.setMinValue(0);
        fraction_picker2.setMaxValue(9);

        integer_picker.setValue(Character.getNumericValue((values.charAt(0))));
        fraction_picker.setValue(Character.getNumericValue((values.charAt(2))));
        fraction_picker2.setValue(Character.getNumericValue((values.charAt(3))));

        fireStore = FirebaseFirestore.getInstance();
        documentReference = fireStore.collection("petrol_stations").document(petrolId);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(integer_picker.getValue()).append(".").
                        append(fraction_picker.getValue()).append(fraction_picker2.getValue());

                double price = Double.parseDouble(stringBuilder.toString());
                double original_price = Double.parseDouble(values);
                if (original_price != 0.00 && Math.abs(price - original_price) > PRICE_CHANGE_TOLERANCE) {
                    Toast.makeText(getBaseContext(), "Twoje zgłoszenie zbytnio odbiega od aktualnej ceny :C", Toast.LENGTH_SHORT).show();
                    return;
                }

                UsersReportService usersReportService = new UsersReportService(documentReference);
                if (price == original_price)
                    usersReportService.sendNewPriceReport(stringBuilder.toString(), fuelName, true);
                else
                    usersReportService.sendNewPriceReport(stringBuilder.toString(), fuelName, false);

                Intent i = new Intent();
                setResult(RESULT_OK, i);
                Toast.makeText(getBaseContext(), "Wysłano zgłoszenie", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                Toast.makeText(getBaseContext(), "Anulowano zgłoszenie", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        fotoBtn.setEnabled(false);
        voiceBtn.setEnabled(false);
    }
}