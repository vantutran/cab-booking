package com.example.cabbooking;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
private Button driver, customer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        driver = (Button) findViewById(R.id.driver);
        customer = (Button) findViewById(R.id.customer);
        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, DriverLoginActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });
        customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, CustomerLoginActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });

    }
    public Runnable NameOfRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            while (true)
            {
                // TODO add code to refresh in background
                try
                {

                    Thread.sleep(1000);// sleeps 1 second
                    //Do Your process here.
                } catch (InterruptedException e){
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
    };
}
