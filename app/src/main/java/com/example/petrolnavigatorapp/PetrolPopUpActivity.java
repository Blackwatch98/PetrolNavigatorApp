package com.example.petrolnavigatorapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public class PetrolPopUpActivity extends Activity {

    private ListView listView;
    private LinearLayout availableFuels;
    private Button changeTypeBtn;
    //private String[] fuels = {"Pb95","Pb98", "Diesel"};
    //private Integer[] imgId = {R.drawable.pb95, R.drawable.pb98,R.drawable.on};
    //private Integer[] imgId2 = {R.drawable.benz, R.drawable.diesel,R.drawable.lpg};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petrol_pop_up);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String name = bundle.getString("petrolName");
            final double lat=bundle.getDouble("latitude");
            final double lon=bundle.getDouble("longitude");

            listView = (ListView)findViewById(R.id.customListView);
            //CustomListViewAdapter customListViewAdapter = new CustomListViewAdapter(this,fuels,imgId);
            //listView.setAdapter(customListViewAdapter);

            availableFuels = (LinearLayout) findViewById(R.id.availableFuels);
            /*
            for(int i = 0; i < 2; i++)
            {
                ImageView img = new ImageView(this);
                img.setImageResource(imgId2[i]);
                availableFuels.addView(img);
            }
            */

            changeTypeBtn = new Button(this);
            changeTypeBtn.setText("Zmień");
            availableFuels.addView(changeTypeBtn);
            /*
            changeTypeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ChangeFuelTypesActivity.class);
                    intent.putExtra("latitude",lat);
                    intent.putExtra("longitude",lon);
                    view.getContext().startActivity(intent);
                }
            });
            */
            TextView petrolName = findViewById(R.id.petrolName);
            TextView petrolCoor= findViewById(R.id.petrolCoor);

            petrolName.setText(name);
            petrolCoor.setText(""+lat+", "+lon);
        }

         DisplayMetrics dm = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(dm);

         int height = dm.heightPixels;
         int width = dm.widthPixels;

         getWindow().setLayout((int)(width*.8),(int)(height*.6));
    }
}