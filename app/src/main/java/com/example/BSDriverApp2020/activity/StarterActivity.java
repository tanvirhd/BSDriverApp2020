package com.example.BSDriverApp2020.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.BSDriverApp2020.databinding.ActivityStarterBinding;
import com.example.BSDriverApp2020.model.ModelBus;
import com.example.BSDriverApp2020.model.ModelDriver;
import com.example.BSDriverApp2020.model.ModelRideSession;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StarterActivity extends AppCompatActivity {
    private ActivityStarterBinding binding;
    public static String riderid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityStarterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnStartapp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                riderid=binding.driverid.getText().toString();
                startActivity(new Intent(StarterActivity.this, HomeActivity.class));finish();
            }
        });

    }

    private void addBusInfo(){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("registeredbuses");
        ModelBus bus=new ModelBus();
        String key=ref.push().getKey();
        bus.setBusid(key);
        bus.setCompanyname("Bikash");
        bus.setLicense("5755-Dhaka Metro-ka");
        bus.setRoute("Azimpur, Nilkhet, Science Lab, Kolabagan, Dhanmondi, Asad Gate, College Gate, Kollyanpur, Gabtoli");
        ref.child(key).setValue(bus).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(StarterActivity.this, "Bus Added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDriver(){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("registereddriver");
        String key=ref.push().getKey();
        ModelDriver driver=new ModelDriver();
        driver.setDrivername("Kamrul HAsan");
        driver.setPhonenumber("01521314936");
        driver.setRegtrationnumber("22222-55555");
        ref.child("01521314936").setValue(driver).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(StarterActivity.this, "Driver Added", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRideSession(){
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("ridesession");
        String key=ref.push().getKey();
        ModelRideSession rideSession=new ModelRideSession();
        rideSession.setBusId("-MFz5vGzyrtUR0MMtygo");
        rideSession.setDriverId("01774201312");
        rideSession.setSessionId(key);

        ref.child(key).setValue(rideSession).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(StarterActivity.this, "Session added", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

