package com.example.petrolnavigatorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class InitialSettingsActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView currentRadiusText;
    private Animation scale_up, scale_down;
    private FirebaseFirestore fireStore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);

        fireStore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final Button confirmButton = findViewById(R.id.confirmButton1);
        currentRadiusText = findViewById(R.id.radiusValue);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentRadiusText.setText(""+(seekBar.getProgress()+1)+"km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = seekBar.getProgress()+1;
                //WAŻNA ZALETA UPDATE POJEDYŃCZYCH WARTOŚCI SZYBKI I PRZYJEMNY
                DocumentReference documentReference = fireStore.collection("users").document(userId);
                documentReference.update("userSettings.searchRadius", value);

                Intent intent = new Intent(InitialSettingsActivity.this, NavigationDrawerActivity.class);
                startActivity(intent);
                finish();
            }
        });

        scale_up = AnimationUtils.loadAnimation(this,R.anim.scale_up);
        scale_down = AnimationUtils.loadAnimation(this,R.anim.scale_down);

        confirmButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN)
                {
                    scale_up.setStartTime(0);
                    confirmButton.startAnimation(scale_up);
                }
                else if(motionEvent.getAction()==MotionEvent.ACTION_UP)
                {
                    scale_up.setStartTime(0);
                    confirmButton.startAnimation(scale_down);
                }

                return false;
            }
        });
    }
}