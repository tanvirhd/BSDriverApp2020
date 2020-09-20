package com.example.BSDriverApp2020.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.example.BSDriverApp2020.model.ModelRequest;
import com.example.BSDriverApp2020.R;
import com.example.BSDriverApp2020.adapter.AdapterRequestList;
import com.example.BSDriverApp2020.databinding.ActivityHomeBinding;
import com.example.BSDriverApp2020.interfaces.AdapterRequestCallback;
import com.example.BSDriverApp2020.model.ModelRideSession;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterRequestCallback, ZXingScannerView.ResultHandler {
    private static final String TAG = "HomeActivity";
    private final int SCAN_CODE_RESULT=100;
    private ActivityHomeBinding binding;
    private BottomSheetBehavior bottomSheetBehavior;
    private Dialog dialog_loading;
    private boolean sessionFound,isriding;

    private GoogleMap mMap;
    private static final float DEFAULT_ZOOM = 17f;
    private FusedLocationProviderClient fusedLocationClient;
    private Location mLastLocation;
    private LocationCallback locationCallback;
    private Handler mainHandler=new Handler();

    private HashMap<String,Location> pickupcoords;

    List<ModelRequest> requestList;List<String>requestedUserIdList;
    AdapterRequestList adapterRequestList;
    DatabaseReference sessionRef,pickuprequestRef;
    ValueEventListener pickuprequestValueEventListener;

    private String busID;
    private ZXingScannerView mScannerView;
    private Dialog dialog_scan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog_scan=initScanDialog(this);
        mScannerView=dialog_scan.findViewById(R.id.zx_scan);
        mScannerView.setResultHandler(this);

        dialog_loading= initLoadingDialog(this);
        sessionRef=FirebaseDatabase.getInstance().getReference("ridesession");
        pickuprequestRef=FirebaseDatabase.getInstance().getReference("pickuprequest");
        checkRideSession();

        bottomSheetBehavior = BottomSheetBehavior.from(binding.appbarHome.bottomsheetContainer);

        setSupportActionBar(binding.appbarHome.toolbar);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.drawerlayout, binding.appbarHome.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white, null));
        binding.drawerlayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        /* todo finish navigation */

        binding.appbarHome.contentHome.iconScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(HomeActivity.this,ScanActivity.class),SCAN_CODE_RESULT);
            }
        });


        binding.appbarHome.switchMaterial.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                startRide(isChecked);
            }
        });
        binding.appbarHome.contentHome.tvrecheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkRideSession();
            }
        });


        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "onCreate: GPS tracking is disabled");
        } else {
            Log.d(TAG, "onCreate: GPS tracking is enabled");
        }

        requestList=new ArrayList<>();requestedUserIdList=new ArrayList<>();
        binding.appbarHome.requestRecyc.setLayoutManager(new LinearLayoutManager(this));
        adapterRequestList = new AdapterRequestList(requestList, getApplicationContext(), this);
        binding.appbarHome.requestRecyc.setAdapter(adapterRequestList);


    }//end of onCreate

    @Override
    protected void onResume() {
        super.onResume();
        if(isriding){
            binding.appbarHome.bottomsheetContainer.setVisibility(View.VISIBLE);
            binding.appbarHome.contentHome.iconScan.setVisibility(View.VISIBLE);
        }else {
            binding.appbarHome.bottomsheetContainer.setVisibility(View.GONE);
            binding.appbarHome.contentHome.iconScan.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_CODE_RESULT) {
            if(resultCode == RESULT_OK) {
                String scanedUserID = data.getStringExtra("ScanResult");
                if(requestedUserIdList.contains(scanedUserID)){
                    pickuprequestRef.child(busID).child(scanedUserID).child("pickupstatus").setValue("riding").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "onSuccess: status updated");
                        }
                    });
                }else {
                    Toast.makeText(this, "Not a Passenger", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startLocationUpdates(LocationRequest locationRequest) {
        Log.d(TAG, "startLocationUpdates: called");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void updateGEOLocation(Location location) {
        Log.d(TAG, "updateGEOLocation: called.");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("availableBuses");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(busID, new GeoLocation(location.getLatitude(), location.getLongitude()),
                new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        Log.d(TAG, "updateGEOLocation: key="+key);
                    }
                });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap = googleMap;
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

    private void checkRideSession(){
        dialog_loading.show();
        sessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    ModelRideSession rideSession=ds.getValue(ModelRideSession.class);
                    if(rideSession.getDriverId().equals(StarterActivity.riderid)){
                        busID=rideSession.getBusId();
                        sessionFound=true;
                        break;
                    }
                }
                if(sessionFound){

                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(HomeActivity.this);

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(HomeActivity.this);
                    Thread GetLastLocationThread = new Thread(new GetLocationRunable(fusedLocationClient));
                    GetLastLocationThread.start();

                    //getLastLocation(fusedLocationClient,false);

                    locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult == null) {
                                return;
                            }
                            for (Location location : locationResult.getLocations()) {

                                Log.d(TAG, "onLocationResult: location updating...");
                                moveCamera(location, "locationcallback onLocationResult");

                               if(isriding){
                                   updateGEOLocation(location);
                               }

                                if(pickupcoords!=null&&!pickupcoords.isEmpty()){
                                    for(Map.Entry me: pickupcoords.entrySet()){
                                        if(location.distanceTo((Location) me.getValue())<200){
                                            binding.appbarHome.contentHome.pickupnearby.setVisibility(View.VISIBLE);
                                        }else {
                                            binding.appbarHome.contentHome.pickupnearby.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        }
                    };

                    binding.appbarHome.contentHome.group.setVisibility(View.GONE);
                    binding.appbarHome.contentHome.mapcontainer.setVisibility(View.VISIBLE);
                    binding.appbarHome.switchMaterial.setEnabled(sessionFound);
                    dialog_loading.dismiss();
                }else {
                    binding.appbarHome.contentHome.group.setVisibility(View.VISIBLE);
                    binding.appbarHome.contentHome.mapcontainer.setVisibility(View.GONE);
                    binding.appbarHome.switchMaterial.setEnabled(sessionFound);
                    dialog_loading.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void startRide(boolean isChecked) {
        pickupcoords=new HashMap<>();isriding=isChecked;
        if (isChecked){
            binding.appbarHome.bottomsheetContainer.setVisibility(View.VISIBLE);
            binding.appbarHome.contentHome.iconScan.setVisibility(View.VISIBLE);
            startLocationUpdates(createLocationRequest());

            pickuprequestValueEventListener=pickuprequestRef.child("-MFz5gSoDqzebLRn1lQF").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    requestList.clear();

                   if(snapshot.exists()){
                       for(DataSnapshot ds:snapshot.getChildren()){
                            ModelRequest pickupRequest=ds.getValue(ModelRequest.class);
                            if(!pickupRequest.isIsrequestAccepted() && pickupRequest.getPickupstatus().equals("pickmeup")){
                                requestList.add(pickupRequest);
                            }else if(pickupRequest.isIsrequestAccepted()){
                                Location location=new Location(pickupRequest.getUserid());location.setLatitude(Double.valueOf(pickupRequest.getLat()));location.setLongitude(Double.valueOf(pickupRequest.getLang()));
                                pickupcoords.put(pickupRequest.getUserid(),location);
                            }
                       }

                       binding.appbarHome.totalpickuprequest.setText(requestList.size()+"");
                       adapterRequestList.notifyDataSetChanged();
                   }else{
                       requestList.clear();
                       binding.appbarHome.totalpickuprequest.setText(requestList.size()+"");
                       adapterRequestList.notifyDataSetChanged();
                   }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });



        } else {
            binding.appbarHome.bottomsheetContainer.setVisibility(View.GONE);
            binding.appbarHome.contentHome.iconScan.setVisibility(View.GONE);
            stopLocationUpdate();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("availableBuses");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(busID, new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    Log.d(TAG, "onComplete: location removed");
                }
            });
        }
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

    @Override
    public void handleResult(Result rawResult) {
        String qr=rawResult.getBarcodeFormat().toString();
        Log.d(TAG, "handleResult: QR="+qr);
    }

    private Dialog initScanDialog(Activity activity) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.dialog_scan);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }




    @Override
    public void onClickAccept(int position,String userid) {
        requestedUserIdList.add(userid);
        pickuprequestRef.child(busID).child(userid).child("isrequestAccepted").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HomeActivity.this, "Pick Request Accepted", Toast.LENGTH_SHORT).show();
            }
        });
        pickuprequestRef.child(busID).child(userid).child("pickupstatus").setValue("waitingforpickup").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: status updated");
            }
        });

    }

    @Override
    public void onClickReject(int position,String userId) {
        pickuprequestRef.child(busID).child(userId).child("ispickuprequestRejected").setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(HomeActivity.this, "Pick Request Rejected", Toast.LENGTH_SHORT).show();
            }
        });
        pickuprequestRef.child(busID).child(userId).child("pickupstatus").setValue("pickuprejected").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: status updated");
            }
        });

    }

    private Dialog initLoadingDialog(Activity activity) {
        Dialog dialog = new Dialog(activity, R.style.Theme_Dialog);
        dialog.setContentView(R.layout.layout_dialog_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    public void moveCamera(Location location, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
    }

    public void moveCamera(LatLng latLng, String caller) {
        Log.d(TAG, "moveCamera: called by " + caller);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
    }

    class GetLocationRunable implements Runnable {
        FusedLocationProviderClient fusedLocationProviderClient;

        public GetLocationRunable(FusedLocationProviderClient fusedLocationProviderClient) {
            this.fusedLocationProviderClient = fusedLocationProviderClient;
        }

        @Override
        public void run() {
            getLastLocation(fusedLocationProviderClient);
        }

        private void getLastLocation(FusedLocationProviderClient flpc) {
            Log.d(TAG, "getLastLocation: called");
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            flpc.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(final Location location) {
                            if (location == null) {
                                //todo handle location object when it is null
                                Log.d(TAG, "onSuccess: location is null");
                                getLastLocation(fusedLocationClient);
                            } else {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mLastLocation = location;
                                        moveCamera(location, "getLastLocation");
                                        //binding.homeAppbar.marker2.setImageResource(R.drawable.dot_selected_blue);
                                        //binding.homeAppbar.searchStartLocation.setText("Your Location");

                                        startLocationUpdates(createLocationRequest());
                                    }
                                });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: error=" + e.getMessage());
                        }
                    });
        }
    }

}//end of code


/*binding.viewpager.setAdapter(new ViewPagerAdapter(new FragmentHome()));
        TabLayoutMediator tabLayoutMediator=new TabLayoutMediator(binding.tabLayout, binding.viewpager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                           switch (position){
                               case 1:
                                   tab.setText("Trip Details");
                                   break;
                               default:
                                   tab.setText("Driver Profile");
                           }
                    }
                });
        tabLayoutMediator.attach();*/

//Teliver.init(this,"ff96673e68ef21aed136fe5d9d9c677d");
//Teliver.startTrip(new TripBuilder("bus01").build());
   /*private void getLastLocation(FusedLocationProviderClient flpc, final boolean startLocationUpdate) {
        Log.d(TAG, "getLastLocation: called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        flpc.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        mLastLocation = location;
                        moveCamera(mLastLocation, "getLastLocation");
                        if(startLocationUpdate){
                            startLocationUpdates(createLocationRequest());
                        }
                        if (location != null) {
                            //todo handle location object when it is null
                        }
                    }
                });
    }*/

