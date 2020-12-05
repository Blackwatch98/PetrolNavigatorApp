package com.example.petrolnavigatorapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class ChangePriceActivity extends AppCompatActivity {

    private String fuelName;
    private String petrolId;
    private FirebaseFirestore firestore;
    private NumberPicker integer_picker;
    private NumberPicker fraction_picker;
    private NumberPicker fraction_picker2;

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
        String values = bundle.getString("fuelClass");
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

        confirmBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(integer_picker.getValue()).append(".").
                        append(fraction_picker.getValue()).append(fraction_picker2.getValue());

                firestore = FirebaseFirestore.getInstance();
                documentReference = firestore.collection("petrol_stations").document(petrolId);
                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists())
                        {
                            //PRZEROBIĆ NA WYSYŁANIE ZGŁOSZEŃ ZAMIAST BEZPOŚREDNIEJ ZMIANY CENY
                            List<HashMap<String, Object>> lista = (List<HashMap<String, Object>>) documentSnapshot.get("fuels");

                            for(HashMap<String, Object> item : lista) {
                                if(item.get("name").toString().equals(fuelName))
                                {
                                    item.put("price", stringBuilder.toString());
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                                    item.put("lastReportDate", sdf.format(new Date()));
                                    int counter = Integer.parseInt(item.get("reportCounter").toString());
                                    item.put("reportCounter",counter+1);
                                    break;
                                }
                            }
                            documentReference.update("fuels", lista);
                        }
                    }
                });
                Intent i = new Intent();
                setResult(RESULT_OK,i);
                Toast.makeText(getBaseContext(),"Wysłano zgłoszenie", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(),"Anulowano zgłoszenie", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        fotoBtn.setEnabled(false);
        voiceBtn.setEnabled(false);
    }
}