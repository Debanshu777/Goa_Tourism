package com.example.goatourism;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.goatourism.Fragments.CameraFragment;
import com.example.goatourism.Fragments.MapFragment;
import com.example.goatourism.Fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment fragment;
    private FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        fab=findViewById(R.id.fab);

        loadFragment(new MapFragment());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment = new MapFragment();
                loadFragment(fragment);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                switch (menuItem.getItemId()){

                    case R.id.camera:

                        fragment = new CameraFragment();
                        loadFragment(fragment);
                        fab.setVisibility(INVISIBLE);
                        return true;

                    case R.id.map:

                        fragment = new MapFragment();
                        loadFragment(fragment);
                        fab.setVisibility(VISIBLE);
                        return true;


                    case R.id.profile:

                        fragment = new ProfileFragment();
                        loadFragment(fragment);
                        return true;


                }
                return true;
            }
        });
    }
    private void loadFragment(Fragment fragment){

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.screens,fragment).commit();

    }
}
