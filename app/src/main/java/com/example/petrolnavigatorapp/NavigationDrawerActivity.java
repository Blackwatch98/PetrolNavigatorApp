package com.example.petrolnavigatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.petrolnavigatorapp.interfaces.FindPetrolsListener;
import com.example.petrolnavigatorapp.utils.Petrol;
import com.example.petrolnavigatorapp.utils.Vehicle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * NavigationDrawer that holds switching and exchanging data between fragments.
 */
public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        FilterDialog.FilterDialogListener, FindPetrolsListener, MapsFragment.UserLocalizationListener, ListFilterDialog.OrderPrefListener,
        VehiclesListFragment.VehiclesListener {

    private DrawerLayout drawer;
    private Toolbar current_toolbar, map_toolbar, list_toolbar, settings_toolbar, vehicles_toolbar, plan_route_toolbar;
    private List<Petrol> foundStations;
    private String prefFuel, prefType;
    private List<Vehicle> userVehicles;
    private LatLng userLocalization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        map_toolbar = findViewById(R.id.map_nav_toolbar);
        list_toolbar = findViewById(R.id.list_nav_toolbar);
        vehicles_toolbar = findViewById(R.id.vehicles_nav_toolbar);
        settings_toolbar = findViewById(R.id.settings_nav_toolbar);
        plan_route_toolbar = findViewById(R.id.plan_route_toolbar);

        current_toolbar = map_toolbar;
        setSupportActionBar(current_toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onStartGetUserVehicles();
        setToggles();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MapsFragment()).commit();
            navigationView.setCheckedItem(R.id.maps);
        }
    }

    /**
     * Sets toggles for each fragment's toolbar.
     */
    private void setToggles() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, map_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle2 = new ActionBarDrawerToggle(this, drawer, settings_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle3 = new ActionBarDrawerToggle(this, drawer, list_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle4 = new ActionBarDrawerToggle(this, drawer, vehicles_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle5 = new ActionBarDrawerToggle(this, drawer, plan_route_toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(toggle2);
        drawer.addDrawerListener(toggle3);
        drawer.addDrawerListener(toggle4);
        drawer.addDrawerListener(toggle5);
        toggle.syncState();
        toggle2.syncState();
        toggle3.syncState();
        toggle4.syncState();
        toggle5.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    /**
     * Changes current seen fragment and sets new toolbar for it.
     *
     * @param menuItem Selected menu item that represents fragment.
     * @return Flag that tells should selected item be displayed.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        current_toolbar.setVisibility(View.GONE);
        switch (menuItem.getItemId()) {
            case R.id.maps:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapsFragment()).commit();
                current_toolbar = map_toolbar;
                break;
            case R.id.list:
                ArrayList<Petrol> petrolStationArrayList = new ArrayList<>(foundStations.size());
                petrolStationArrayList.addAll(foundStations);
                Bundle bundle = new Bundle();
                bundle.putSerializable("petrols", petrolStationArrayList);
                bundle.putDouble("lat", userLocalization.latitude);
                bundle.putDouble("lon", userLocalization.longitude);
                bundle.putString("prefFuel", prefFuel);
                bundle.putString("prefType", prefType);
                PetrolStationsListFragment fragment = new PetrolStationsListFragment();
                fragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment).commit();
                current_toolbar = list_toolbar;
                setSupportActionBar(current_toolbar);
                break;
            case R.id.plan_route:
                Bundle bundle2 = new Bundle();
                if (userVehicles != null) {
                    ArrayList<Vehicle> vehicles = new ArrayList<>(userVehicles.size());
                    vehicles.addAll(userVehicles);
                    bundle2.putSerializable("userVehicles", vehicles);
                }
                PlanRouteFragment fragment2 = new PlanRouteFragment();
                fragment2.setArguments(bundle2);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment2).commit();
                current_toolbar = plan_route_toolbar;
                setSupportActionBar(current_toolbar);
                break;
            case R.id.vehicles:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new VehiclesListFragment()).commit();
                current_toolbar = vehicles_toolbar;
                break;
            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                current_toolbar = settings_toolbar;
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Wylogowałeś się. Do zobaczenia :)", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent = new Intent(NavigationDrawerActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        current_toolbar.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbarmenu, menu);
        if (current_toolbar.equals(list_toolbar)) {
            menu.findItem(R.id.list_filter_icon).setVisible(true);
            menu.findItem(R.id.menu_item_filter).setVisible(false);
        } else if (current_toolbar.equals(plan_route_toolbar)) {
            menu.findItem(R.id.list_filter_icon).setVisible(false);
            menu.findItem(R.id.menu_item_filter).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            case R.id.menu_item_filter:
                FilterDialog filterDialog = new FilterDialog();
                filterDialog.show(getSupportFragmentManager(), "dialog");
                break;
            case R.id.list_filter_icon:
                ListFilterDialog filterDialog2 = new ListFilterDialog();
                filterDialog2.show(getSupportFragmentManager(), "dialog");
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates user preferred fuel in database and refreshes fragment.
     *
     * @param prefType Fuel type preferred by user.
     * @param prefFuel Fuel preferred by user.
     */
    @Override
    public void changeUserPreferences(String prefType, String prefFuel) {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DocumentReference userDocument = fireStore.collection("users").document(mAuth.getCurrentUser().getUid());
        userDocument.update(
                "userSettings.prefFuel", prefFuel,
                "userSettings.prefFuelType", prefType
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), "Zmieniono ustawienia preferencji...", Toast.LENGTH_SHORT).show();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapsFragment()).commit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Nie udało się zmienić preferencji...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gets petrol stations list that is passed to PetrolStationsListFragment.
     *
     * @param stations List of found petrol stations.
     */
    @Override
    public void getPetrolsList(List<Petrol> stations) {
        foundStations = stations;
    }

    /**
     * Gets user fuel filter preferences from FilterDialog.
     *
     * @param prefType Preferred fuel type.
     * @param prefFuel Preferred fuel.
     */
    @Override
    public void getUserPrefs(String prefType, String prefFuel) {
        this.prefType = prefType;
        this.prefFuel = prefFuel;
    }

    /**
     * Gets user location from MapsFragment. Then it is passed to PetrolStationsListFragment.
     *
     * @param latLng
     */
    @Override
    public void getUserLocalization(LatLng latLng) {
        userLocalization = latLng;
    }

    /**
     * Refreshes PetrolStationsListFragment whenever sorting preferences are changed.
     */
    @Override
    public void refreshList() {
        ArrayList<Petrol> petrols = new ArrayList<>(foundStations.size());
        petrols.addAll(foundStations);
        Bundle bundle = new Bundle();
        bundle.putSerializable("petrols", petrols);
        bundle.putDouble("lat", userLocalization.latitude);
        bundle.putDouble("lon", userLocalization.longitude);
        bundle.putString("prefFuel", prefFuel);
        bundle.putString("prefType", prefType);
        PetrolStationsListFragment fragment = new PetrolStationsListFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new VehiclesListFragment()).commit();
            }
        }
    }

    /**
     * Gets list of user vehicles from database.
     */
    private void onStartGetUserVehicles() {
        FirebaseFirestore fireStore = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        this.userVehicles = new LinkedList<>();

        CollectionReference userVehiclesCollection = fireStore.collection("users").document(mAuth.getCurrentUser().getUid())
                .collection("vehicles");
        userVehiclesCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot query : queryDocumentSnapshots) {
                    userVehicles.add(new Vehicle(
                            query.getString("name"),
                            query.getDouble("tankCapacity"),
                            query.getDouble("averageFuelConsumption"),
                            Integer.parseInt(query.get("fuelTypeId").toString()),
                            Double.parseDouble(query.get("currentFuelLevel").toString()),
                            Double.parseDouble(query.get("reserveFuelLevel").toString())
                    ));
                }
            }
        });
    }

    /**
     * Updates user's vehicles list with new from VehiclesListFragment.
     *
     * @param userVehicles All users vehicles. Also taking new ones into account.
     */
    @Override
    public void getUserVehicles(List<Vehicle> userVehicles) {
        this.userVehicles = userVehicles;
    }
}