package com.example.maps;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.gms.location.places.*;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback{
    //private AutocompleteSupportFragment autocompleteFragment;
    private Button btn_search_ok, btn_search_cancel;
    private ImageView marker;
    private GoogleMap mMap;

    private EditText addressField;
    private ImageButton imageButton;
//    private PlaceAutocompleteAdapter mplaceAutocompleteAdapter;
//    private GoogleApiClient mGoogleApiClient;
//    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
//            new LatLng(-40, -168), new LatLng(71, 136));
//
//    private GeoDataClient mGeoDataClient;
//    private PlaceDetectionClient mPlaceDetectionClient;

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

    private void addEvents() {
        btn_search_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btn_search_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng center = mMap.getCameraPosition().target;
                Toast.makeText(getApplicationContext(),center.toString(),Toast.LENGTH_LONG).show();
            }
        });

    }

    private void addControls() {
        btn_search_cancel = findViewById(R.id.btn_search_cancel);
        btn_search_ok = findViewById(R.id.btn_search_ok);
        marker = findViewById(R.id.marker);
        marker.bringToFront();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_search);
        mapFragment.getMapAsync(this);
        addressField = (EditText ) findViewById(R.id.location_search);

        imageButton = findViewById(R.id.search_address);

//        mplaceAutocompleteAdapter = new PlaceAutocompleteAdapter(
//                this, mGeoDataClient, LAT_LNG_BOUNDS, null);
//
//        addressField.setAdapter(mplaceAutocompleteAdapter);
    }

    public void onClick(View v) {
        String address = addressField.getText().toString();

        List<Address> addressList = null;
        if (!TextUtils.isEmpty(address)) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(address, 6);
                if (addressList != null) {
                    for (int i = 0; i < addressList.size(); i++) {
                        Address userAddress = addressList.get(i);
                        LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
                    }
                }
                else {
                    Toast.makeText(this, "Location not found...", Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(this, "Please write any location name...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng hcmc = new LatLng(10.762622, 106.660172);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hcmc));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
    }
}
