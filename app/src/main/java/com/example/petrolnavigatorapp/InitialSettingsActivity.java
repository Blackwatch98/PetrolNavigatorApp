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

public class InitialSettingsActivity extends AppCompatActivity {

    private SeekBar seekBar;
    private TextView currentRadiusText;
    private Animation scale_up, scale_down;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_settings);

        final Button confirmButton = findViewById(R.id.confirmButton1);
        currentRadiusText = findViewById(R.id.radiusValue);

        seekBar = findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentRadiusText.setText(""+seekBar.getProgress()+"km");
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
                Intent intent = new Intent(InitialSettingsActivity.this, MapsActivity.class);
                int value = seekBar.getProgress();
                intent.putExtra("seekBarValue", value*1000);
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
                    confirmButton.startAnimation(scale_up);
                }
                else if(motionEvent.getAction()==MotionEvent.ACTION_UP)
                {
                    confirmButton.startAnimation(scale_down);
                }

                return false;
            }
        });
    }
}