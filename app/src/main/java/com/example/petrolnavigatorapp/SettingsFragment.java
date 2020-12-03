package com.example.petrolnavigatorapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class SettingsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView currentRadiusText;
    private SeekBar seekBar;
    private FirebaseFirestore fireStore;
    private FirebaseAuth mAuth;

    private Animation scale_up, scale_down;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        seekBar = view.findViewById(R.id.seekBar2);
        currentRadiusText = view.findViewById(R.id.currentRadiusText2);


        mAuth = FirebaseAuth.getInstance();
        fireStore = FirebaseFirestore.getInstance();

        DocumentReference documentReference = fireStore.collection("users")
                .document(mAuth.getCurrentUser().getUid());

        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    Map<String, Object> map = (Map<String,Object>)documentSnapshot.get("userSettings");
                    seekBar.setProgress(Integer.parseInt(map.get("searchRadius").toString()));
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                currentRadiusText.setText(""+seekBar.getProgress()+1+"km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        scale_up = AnimationUtils.loadAnimation(getContext(),R.anim.scale_up);
        scale_down = AnimationUtils.loadAnimation(getContext(),R.anim.scale_down);


        final Button confirmButton = view.findViewById(R.id.settingsConfirmButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int value = seekBar.getProgress()+1;
                DocumentReference documentReference = fireStore.collection("users").document(mAuth.getCurrentUser().getUid());
                documentReference.update("userSettings.searchRadius", value);
            }
        });

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

        return view;
    }
}