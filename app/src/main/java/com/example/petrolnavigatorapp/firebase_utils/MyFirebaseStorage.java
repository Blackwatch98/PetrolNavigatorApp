package com.example.petrolnavigatorapp.firebase_utils;

import com.google.firebase.storage.StorageReference;

/**
 * Class to communicate with Firebase Storage.
 */
public class MyFirebaseStorage {
    private StorageReference sRef;

    public MyFirebaseStorage() {
        sRef = com.google.firebase.storage.FirebaseStorage.getInstance().getReference();
    }

    public StorageReference getPetrolIconRef(String petrolName) {
        StorageReference tempRef = sRef.child("petrols_icons");
        if (petrolName.toUpperCase().contains("BP"))
            return tempRef.child("bp_logo.png");
        else if (petrolName.toUpperCase().contains("SHELL"))
            return tempRef.child("shell_logo.png");
        else if (petrolName.toUpperCase().contains("ORLEN"))
            return tempRef.child("orlen_logo.png");
        else if (petrolName.toUpperCase().contains("CIRCLE K"))
            return tempRef.child("circle_k_logo.png");
        else if (petrolName.toUpperCase().contains("LOTOS"))
            return tempRef.child("lotos_logo.png");
        else
            return tempRef.child("default_logo.png");
    }
}
