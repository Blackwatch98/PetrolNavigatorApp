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

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class NavigationDrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private Toolbar current_toolbar, map_toolbar, list_toolbar, settings_toolbar, vehicles_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);

        Bundle bundle = getIntent().getExtras();

        //DEPRECATED GET RADIUS
        //radius = bundle.getInt("seekBarValue");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        map_toolbar = findViewById(R.id.map_nav_toolbar);
        list_toolbar = findViewById(R.id.list_nav_toolbar);
        vehicles_toolbar = findViewById(R.id.vehicles_nav_toolbar);
        settings_toolbar = findViewById(R.id.settings_nav_toolbar);

        current_toolbar = map_toolbar;
        setSupportActionBar(current_toolbar);

        drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.bringToFront();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawer,map_toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle2 = new ActionBarDrawerToggle(this,drawer,settings_toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle3 = new ActionBarDrawerToggle(this,drawer,list_toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        ActionBarDrawerToggle toggle4 = new ActionBarDrawerToggle(this,drawer,vehicles_toolbar,
                R.string.navigation_drawer_open,R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(toggle2);
        drawer.addDrawerListener(toggle3);
        drawer.addDrawerListener(toggle4);
        toggle.syncState();
        toggle2.syncState();
        toggle3.syncState();
        toggle4.syncState();

        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new MapsFragment()).commit();
            navigationView.setCheckedItem(R.id.maps);
        }
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        current_toolbar.setVisibility(View.GONE);
        switch (menuItem.getItemId())
        {
            case R.id.db:
                Intent intent = new Intent(NavigationDrawerActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.maps:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new MapsFragment()).commit();
                current_toolbar = map_toolbar;
                break;
            case R.id.list:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FuelListFragment()).commit();
                current_toolbar = list_toolbar;
                break;
            case R.id.vehicles:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new VehiclesFragment()).commit();
                current_toolbar = vehicles_toolbar;
                break;
            case R.id.settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new SettingsFragment()).commit();
                current_toolbar = settings_toolbar;
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Wylogowałeś się :)", Toast.LENGTH_SHORT).show();
                finish();
                Intent intent2 = new Intent(NavigationDrawerActivity.this, LoginActivity.class);
                startActivity(intent2);
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        current_toolbar.setVisibility(View.VISIBLE);
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.toolbarmenu, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                break;
            default:
               break;
        }
        return super.onOptionsItemSelected(item);
    }
}