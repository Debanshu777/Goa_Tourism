package com.example.goatourism.Fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.goatourism.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private  static  final String TAG="MapFragment";
    private  Boolean mLoactionPermissionGranted=false;
    private SearchView mSearchText;
    private ImageView mGps;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private BottomSheetBehavior mBottomSheetBehavior;
    private FloatingActionButton buttonExpandmap;



    private static final String FINE_LOCATION=Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_CODE=1234;
    private static final float DEFAULT_ZOOM=15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_map, container, false);
        getLocationPermission();
        mSearchText=view.findViewById(R.id.input_search);
        buttonExpandmap=view.findViewById(R.id.expand);
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        buttonExpandmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:

                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:

                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:

                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:

                        break;
                    case BottomSheetBehavior.STATE_SETTLING:

                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        mSearchText.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location=mSearchText.getQuery().toString();
                List<Address> addressList=null;
                if(location!=null||!location.equals("")){
                    Geocoder geocoder = new Geocoder(getActivity());
                    List<Address> list = new ArrayList<>();
                    try{
                        list = geocoder.getFromLocationName(location, 1);
                    }catch (IOException e){
                        Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
                    }
                    Address address = list.get(0);
                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                            address.getAddressLine(0));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mGps=view.findViewById(R.id.ic_gps);
        getLocationPermission();
        return view;
    }
    private  void init(){
        Log.d(TAG,"init: initializing");


        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick:clicked gps icon");
                getDeviceLocation();
            }
        });

        hideSoftKeyboard();

    }
    private void getDeviceLocation(){
        Log.d(TAG,"inside get device location ");
        mFusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(getActivity());
        try{
            if(mLoactionPermissionGranted){
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null){
                            Log.d(TAG,"onComplete:found location");
                            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                            moveCamera(latLng,DEFAULT_ZOOM,"My Location");

                        }
                        else{
                            Log.d(TAG,"onComplete:current location is null");
                            Toast.makeText(getContext(), "unable to find the current location", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }

        }catch (SecurityException e){
            Log.d(TAG,"getDeviceLocation: SecurityException"+e.getMessage());
        }
    }
    private void moveCamera(LatLng latLng,float zoom,String title){
        Log.d(TAG,"moveCamera:moving camera to lat:"+latLng.latitude+", lng:"+latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
        if(!title.equals("My Location")) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(options);
        }
        hideSoftKeyboard();

    }
    private void initMap(){
        Toast.makeText(getActivity(), "map is ready", Toast.LENGTH_SHORT).show();
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapFragment.this);

    }
    private  void getLocationPermission(){
        String[] permissions={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),COURSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED){
                mLoactionPermissionGranted=true;

                initMap();
            }
            else {
                ActivityCompat.requestPermissions(getActivity(),permissions,REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),permissions,REQUEST_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLoactionPermissionGranted=false;
        switch (requestCode){
            case REQUEST_CODE:
                if(grantResults.length>0)
                {
                    for (int i=0;i<grantResults.length;i++){
                        if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                            mLoactionPermissionGranted=false;
                            return;
                        }
                    }
                    mLoactionPermissionGranted=true;
                    initMap();
                }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mLoactionPermissionGranted){
            getDeviceLocation();
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        init();
    }
    private  void hideSoftKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

}