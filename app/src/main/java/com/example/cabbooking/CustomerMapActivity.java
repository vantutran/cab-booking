package com.example.cabbooking;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;

    private Button btnLogout, btnRequest, btnSetting, btnSearch;
    private TextView driverName, driverPhone, driverCar;
    private LinearLayout driverInforLayout;

    private LatLng pickUpLocation, destinationLatLng;

    private int radius = 100;
    private Boolean isFindDriver = false;
    private Boolean isRequest = false;
    private String foundDriverId, destination;
    private EditText edDestination;

    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;
    private GeoQuery geoQuery;
    private Marker pickupMarker, driverMarker;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private Marker desMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("TEST", "TESTTT");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Log.d("TEST", "1111111");
        if (mapFragment != null) {
            mapFragment.getMapAsync(CustomerMapActivity.this);
            Toast.makeText(CustomerMapActivity.this, "Map created", Toast.LENGTH_LONG);
            Log.d("TEST", "SUCCESS");
        } else {
            Toast.makeText(CustomerMapActivity.this, "Cant create map", Toast.LENGTH_LONG);
        }
        destinationLatLng = new LatLng(0.0, 0.0);

        driverName = findViewById(R.id.driverName);
        driverPhone = findViewById(R.id.driverPhone);
        driverCar = findViewById(R.id.driverCar);

        btnRequest = findViewById(R.id.btnRequest);
        btnLogout = findViewById(R.id.btnCustomerlogout);
        btnSetting = findViewById(R.id.btnCustomerSetting);
//        btnSearch = findViewById(R.id.btnSearch);
//
//        edDestination = findViewById(R.id.edDestination);

        driverInforLayout = findViewById(R.id.driverInforLayout);


        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(CustomerMapActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });
        btnRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isRequest) {
                    //remove request from database
                    isRequest = false;
                    geoQuery.removeAllListeners();
                    driverLocationRef.removeEventListener(driverLocationRefListener);

                    if (foundDriverId != null) {

//                        DatabaseReference driverRef = FirebaseDatabase.getInstance()
//                                .getReference().child("Users").child("Drivers")
//                                .child(foundDriverId).child("customerRequest");  //p12
                        DatabaseReference driverRef = FirebaseDatabase.getInstance()
                                .getReference().child("Users").child("Drivers")
                                .child(foundDriverId);
                        driverRef.setValue(true);
                        foundDriverId = null;
                    }

                    isFindDriver = false;
                    radius = 1;

                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.removeLocation(userId);

                    //remove pickup marker
                    if (pickupMarker != null) {
                        pickupMarker.remove();
                    }
                    if (driverMarker != null) {
                        driverMarker.remove();
                    }
                    btnRequest.setText("call Cab");

                    driverInforLayout.setVisibility(View.GONE);
                    driverCar.setText("");
                    driverName.setText("");
                    driverPhone.setText("");

                } else {
                    isRequest = true;
                    String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                    GeoFire geoFire = new GeoFire(ref);
                    if (lastLocation != null) {
                        geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

                        pickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("pick up here"));
                    }


                    btnRequest.setText("getting your driver...");

                    getClosestDriver();
                }

            }
        });
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(CustomerMapActivity.this, CustomerSettingActivity.class);
                startActivity(i);
                return;
            }
        });

//        btnSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String address = edDestination.getText().toString();
//                if (address.equals("")) {
//                    getLocationFromAddress(address);
//
//                }
//            }
//        });

        //nov 15


        //auto complete destination place
//        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
//                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

//        SupportPlaceAutocompleteFragment autocompleteFragment = (SupportPlaceAutocompleteFragment)
//                getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                destination = place.getName().toString();
//                destinationLatLng = place.getLatLng();
//            }
//
//            @Override
//            public void onError(Status status) {
//
//            }
//        });


        String apiKey = getString(R.string.api_key);
        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(com.google.android.libraries.places.api.model.Place.Field.ID, com.google.android.libraries.places.api.model.Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getName();
                destinationLatLng = place.getLatLng();

                if(desMarker != null) {
                    desMarker.remove();
                }
                MarkerOptions customerDesMarker = new MarkerOptions().position(destinationLatLng)
                        .title("Your Title")
                        .snippet("Please move the marker if needed.")
                        .draggable(true);
                desMarker = mMap.addMarker(customerDesMarker);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });
    }

    private GeoPoint getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        GeoPoint p1 = null;
        MarkerOptions destinationMarker = new MarkerOptions();

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            LatLng destinationLastLng = new LatLng(location.getLatitude(), location.getLongitude());

            p1 = new GeoPoint((location.getLatitude() * 1E6),
                    (location.getLongitude() * 1E6));
            destinationMarker.position(destinationLastLng);

            destinationMarker.title("destination Marker");

            mMap.moveCamera(CameraUpdateFactory.newLatLng(destinationLastLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        } catch (IOException ex) {

            ex.printStackTrace();
        }
        return p1;
    }

    /**
     * geofire queries closest driver
     */
    private void getClosestDriver() {
        DatabaseReference driverAvailable = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        //remove the listener in order to cancel the request for booking
        GeoFire geoFire = new GeoFire(driverAvailable);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);

        //when driver move , call this method again .--> remove all listener
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.i("keu=yy", key);
                if (!isFindDriver && isRequest) {
                    isFindDriver = true;
                    foundDriverId = key;

                    // create customerid ( request for booking ) in driver -> driver id
                    // get customerid from customerrequest   then bring it to Users  -> Drivers --> sdafgtreCVdrbJbZyfBQsv1s3d5
                    DatabaseReference driverRef = FirebaseDatabase.getInstance()
                            .getReference().child("Users").child("Drivers").child(foundDriverId);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                    //create map of customer
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    map.put("destination", destination);
                    map.put("destinationLat", destinationLatLng.latitude);
                    map.put("destinationLng", destinationLatLng.longitude);

                    driverRef.updateChildren(map);
                    //then getAssignedCustomer in drivermapactivity will be trigger

                    getDriverLocation();
                    getDriverInfor();
                    btnRequest.setText("Looking for Driver Location...");

                }

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            //in crease radius  and call it self to find driver
            @Override
            public void onGeoQueryReady() {
                //if driver not found then increase radius and keep going looking for other driver
                if (!isFindDriver) {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(foundDriverId).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && isRequest) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    btnRequest.setText("driver found");
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLatLng = new LatLng(locationLat, locationLng);
                    if (driverMarker != null) {
                        driverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLongitude(pickUpLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(driverLatLng.latitude);
                    loc2.setLongitude(driverLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance < 100) {
                        btnRequest.setText("Driver is Here");
                    } else {
                        btnRequest.setText("Driver is at " + String.valueOf(distance) + " meter far away");
                    }

                    driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("TEST", "testttttt3");
        mMap = googleMap;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(this)
                    .setTitle("Give me access")
                    .setMessage("Please")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(CustomerMapActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .create()
                    .show();

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        (googleApiClient).connect();
        Log.d("TEST", "testttttt2");
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        Log.d("TEST", "testttttt5");

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Log.d("TEST", "testttttt");
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    //remove driver from available list when
    @Override
    protected void onStop() {
        super.onStop();
    }

    private void getDriverInfor() {
        driverInforLayout.setVisibility(View.VISIBLE);
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(foundDriverId);
        customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        driverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        driverPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("car") != null) {
                        driverCar.setText(map.get("car").toString());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
