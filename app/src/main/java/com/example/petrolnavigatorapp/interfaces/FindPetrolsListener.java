package com.example.petrolnavigatorapp.interfaces;

import com.example.petrolnavigatorapp.utils.Petrol;

import java.util.List;

public interface FindPetrolsListener {
    void getPetrolsList(List<Petrol> petrols);
    void getUserPrefs(String prefType, String prefFuel);
}
