package com.example.cabbooking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DriverSettingActivity extends AppCompatActivity {

    private EditText edName, edPhone, edCar;

    private Button btnBack, btnConfirm;

    private FirebaseAuth fireAuth;
    private DatabaseReference driverRef;

    String userId;
    String name, phone, car;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setting);

        edName = findViewById(R.id.edName);
        edPhone = findViewById(R.id.edPhone);
        edCar = findViewById(R.id.edCar);

        btnBack = findViewById(R.id.btnBack);
        btnConfirm = findViewById(R.id.btnConfirm);

        fireAuth = FirebaseAuth.getInstance();
        userId = fireAuth.getCurrentUser().getUid();
        driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers")
                .child(userId);

        getUserInfo();
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInfo();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });


    }

    private void saveUserInfo() {
        name = edName.getText().toString();
        phone = edPhone.getText().toString();
        car = edCar.getText().toString();

        Map userInfo = new HashMap();
        userInfo.put("name", name);
        userInfo.put("phone", phone);
        userInfo.put("car", car);
        driverRef.updateChildren(userInfo);
        finish();
    }

    private void getUserInfo() {
        driverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        name = map.get("name").toString();
                        edName.setText(name);
                    }
                    if (map.get("phone") != null) {
                        phone = map.get("phone").toString();
                        edPhone.setText(phone);
                    }
                    if (map.get("car") != null) {
                        car = map.get("car").toString();
                        edCar.setText(car);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}