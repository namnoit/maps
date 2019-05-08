package com.example.maps;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final int SEARCH_MODE_SOURCE = 1;
    public static final int SEARCH_MODE_DEST = 2;
    public static final int SEARCH_MODE_ALARM = 3;
    public static final int SEARCH_MODE_CANCEL = 0;
    public static final int SEARCH_MODE_SUCCESS = 4;
    private AutocompleteSupportFragment autocompleteFragment;
    private Button btn_search_ok, btn_search_cancel;
    private ImageView marker;
    private FloatingActionButton locSearch;
    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    LocationManager locationManager;

    private Intent intent;

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(SearchActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
            Toast.makeText(getBaseContext(), msg, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        String provider = locationManager.getBestProvider(new Criteria(), false);
                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 400, 1, locationListenerGPS);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        // Initialize Places.
        Places.initialize(getApplicationContext(), "AIzaSyCKtgLCokxsZXdy84x5_7YbRhSdV-PSRco");
        PlacesClient placesClient = Places.createClient(this);
        addControls();

        addEvents();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(SEARCH_MODE_CANCEL, intent);
        finish();
    }

    private void addEvents() {
        btn_search_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(SEARCH_MODE_CANCEL, intent);
                finish();
            }
        });

        btn_search_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng center = mMap.getCameraPosition().target;
//                Toast.makeText(getApplicationContext(),center.toString(),Toast.LENGTH_LONG).show();
                intent.putExtra("lat", center.latitude);
                intent.putExtra("long", center.longitude);
//                intent.putExtra("location",center.toString());
                setResult(SEARCH_MODE_SUCCESS, intent);
                finish();
            }
        });

        locSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(SearchActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission();
                    return;
                }
                mMap.setMyLocationEnabled(true);
                client.getLastLocation().addOnSuccessListener(SearchActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                        }
                    }
                });
            }
        });

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

//                LatLng ll = place.getLatLng();
                Toast.makeText(SearchActivity.this, place.getAddress(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(SearchActivity.this, status.toString(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private void addControls() {
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);
        autocompleteFragment.setCountry("VN");
        btn_search_cancel = findViewById(R.id.btn_search_cancel);
        btn_search_ok = findViewById(R.id.btn_search_ok);
        marker = findViewById(R.id.marker);
        marker.bringToFront();
        locSearch = findViewById(R.id.loc_search);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_search);
        mapFragment.getMapAsync(this);
        intent = getIntent();

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);

        client.getLastLocation().addOnSuccessListener(SearchActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
                }
                else{
                    LatLng hcmc = new LatLng(10.762622, 106.660172);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(hcmc));
                    mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
                }
            }
        });

    }


}
