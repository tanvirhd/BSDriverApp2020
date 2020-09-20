package com.example.BSDriverApp2020.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.BSDriverApp2020.R;
import com.example.BSDriverApp2020.databinding.FragmentHomeBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static android.content.Context.LOCATION_SERVICE;

public class FragmentHome extends Fragment implements OnMapReadyCallback {

    private static String TAG = "FragmentHome";
    private FragmentHomeBinding binding;

    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 17f;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLastLocation;
    private LocationCallback locationCallback;


    public FragmentHome() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        SupportMapFragment mapFragment =(SupportMapFragment)  getActivity().getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastLocation(fusedLocationClient);

        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    moveCamera(location,"locationcallback onLocationResult");
                    updateGEOLocation(location);
                }
            }
        };

        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(getActivity(), "GPS tracking is disabled", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getActivity(), "GPS tracking is enabled", Toast.LENGTH_SHORT).show();
        }



        return binding.getRoot();
    }//end of onCreate

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM));
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
            }
        });
    }

    private void updateGEOLocation(Location location) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("availableBuses");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation("bus02", new GeoLocation(location.getLatitude(), location.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        Log.d(TAG, "onLocationChanged:geoFire.setLocation:onComplete:called ");
                    }
                });


    }

    private void getLastLocation(FusedLocationProviderClient flpc) {
        Log.d(TAG, "getLastLocation: called");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        flpc.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastLocation = location;
                        moveCamera(mLastLocation, "getLastLocation");
                        startLocationUpdates(createLocationRequest());
                        if (location != null) {
                            //todo handle location object when it is null
                        }
                    }
                });
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        Log.d(TAG, "startLocationUpdates: called");
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    //todo research on this
    private void stopLocationUpdate() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private LocationRequest createLocationRequest() {
        Log.d(TAG, "createLocationRequest: called");
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void moveCamera(Location location, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
    }

    public void moveCamera(LatLng latLng, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

}//end of code


 /*DatabaseReference ref = FirebaseDatabase.getInstance().getReference("availableBuses");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation("bus01", new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                Log.d(TAG, "onComplete: location removed");
            }
        });*/