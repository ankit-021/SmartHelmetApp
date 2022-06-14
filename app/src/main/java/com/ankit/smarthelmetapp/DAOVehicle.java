package com.ankit.smarthelmetapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DAOVehicle {
    private DatabaseReference databaseReference;

    public DAOVehicle(){
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(Vehicle.class.getSimpleName());
    }

    public Task<Void> add(Vehicle vehicle){
        return databaseReference.push().setValue(vehicle);
    }
}
