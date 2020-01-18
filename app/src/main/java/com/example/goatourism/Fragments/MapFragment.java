package com.example.goatourism.Fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.goatourism.MainActivity;
import com.example.goatourism.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Locale;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private  static  final String TAG="MapFragment";
    private  Boolean mLoactionPermissionGranted=false;
    private FusedLocationProviderClient mFusedLocationProviderClient;




    private static final String FINE_LOCATION=Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION=Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int REQUEST_CODE=1234;
    private static final float DEFAULT_ZOOM=15f;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_map, container, false);
        getLocationPermission();

        return view;
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
                            moveCamera(latLng,DEFAULT_ZOOM);

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
    private void moveCamera(LatLng latLng,float zoom){
        Log.d(TAG,"moveCamera:moving camera to lat:"+latLng.latitude+", lng:"+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
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
        try {
                        boolean success = googleMap.setMapStyle(
                                MapStyleOptions.loadRawResourceStyle(
                                        getActivity(), R.raw.mystyle2));

                        if (!success) {
                            Log.e("MapActivity", "Style parsing failed.");
                        }
                    } catch (Resources.NotFoundException e) {
                        Log.e("MapActivity", "Can't find style. Error: ", e);
                    }
    }


}
