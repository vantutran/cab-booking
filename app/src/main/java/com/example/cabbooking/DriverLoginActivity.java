package com.example.cabbooking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLoginActivity extends AppCompatActivity {
    private EditText email, password;
    private Button btnLogin, btnRegister;
    private FirebaseAuth firebaseAuthen;
    private FirebaseAuth.AuthStateListener authenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnLogin = (Button) findViewById(R.id.Login);
        btnRegister = (Button) findViewById(R.id.Register);


        firebaseAuthen = FirebaseAuth.getInstance();
        authenListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    Intent i = new Intent(DriverLoginActivity.this, DriverMapActivity.class);
                    startActivity(i);
                    finish();
                    return;
                }
            }
        };

        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Aemail = email.getText().toString();
                final String Apassword = password.getText().toString();
                firebaseAuthen.createUserWithEmailAndPassword(Aemail,Apassword).addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(DriverLoginActivity.this, "register error", Toast.LENGTH_LONG).show();
                        } else {
                            String userid = firebaseAuthen.getCurrentUser().getUid();
                            DatabaseReference current_user = FirebaseDatabase.getInstance()
                                    .getReference().child("Users").child("Drivers").child(userid);
                            current_user.setValue(true);

                        }
                    }
                });

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String Aemail = email.getText().toString();
                final String Apassword = password.getText().toString();
                firebaseAuthen.signInWithEmailAndPassword(Aemail, Apassword)
                        .addOnCompleteListener(DriverLoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(DriverLoginActivity.this, "login error", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuthen.addAuthStateListener(authenListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuthen.removeAuthStateListener(authenListener);
    }
}
