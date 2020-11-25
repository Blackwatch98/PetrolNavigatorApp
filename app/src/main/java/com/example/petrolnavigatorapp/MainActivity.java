package com.example.petrolnavigatorapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {


    private TextView text;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final MediaPlayer engineSound = MediaPlayer.create(this, R.raw.engine_sound);
        final Button btn = findViewById(R.id.startButton);
        text = findViewById(R.id.appTitle);
        mAuth = FirebaseAuth.getInstance();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation anim = AnimationUtils.loadAnimation(MainActivity.this,R.anim.shake);

                final Animation.AnimationListener animationListener = new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        engineSound.setVolume(1f,1f);
                        engineSound.start();
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        engineSound.stop();
                        Intent intent;

                        if(mAuth.getCurrentUser() == null)
                            intent = new Intent(MainActivity.this,LoginActivity.class);
                        else
                            intent = new Intent(MainActivity.this,MapsActivity.class);

                        startActivity(intent);
                        finish();
                        //mAuth.signOut();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                };
                anim.setAnimationListener(animationListener);
                btn.startAnimation(anim);
                text.startAnimation(anim);
            }
        });
    }
}