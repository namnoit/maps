package com.example.maps;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener {
    private Toolbar toolbar;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_distance, fab_alarm;
    private FloatingActionsMenu fab_menu;
    private FloatingActionButton loc;
    private ImageButton btnDeleteAlarm;
    private Button btnSrc, btnDst, btnOk;
    private LatLng srcLL, dstLL, alarmLL = null;
    private GoogleMap mMap;
    private FusedLocationProviderClient client;
    private LocationManager locationManager;
    private boolean isring = true;
    private boolean isringtone = true;

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
                                ActivityCompat.requestPermissions(MainActivity.this,
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
            Toast.makeText(MainActivity.this,"Location change",Toast.LENGTH_LONG).show();
            if (alarmLL != null) {
                Toast.makeText(MainActivity.this,"Location change",Toast.LENGTH_LONG).show();

                Location alarmLocation = new Location("Alarm Location");
                alarmLocation.setLatitude(alarmLL.latitude);
                alarmLocation.setLongitude(alarmLL.longitude);
                if (location.distanceTo(alarmLocation) < 200) {
                    alarm();
                }

            }
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

    private void alarm() {
        Toast.makeText(this,"den",Toast.LENGTH_LONG).show();
    }

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
        setContentView(R.layout.activity_main);

        addControls();
        addEvents();
    }


    private void addEvents() {
        fab_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Nothing here", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                showDistanceDialog();
            }
        });
        fab_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearch(SearchActivity.SEARCH_MODE_ALARM);

            }
        });

        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission();
                    return;
                }
                mMap.setMyLocationEnabled(true);
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
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

        btnDeleteAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Xoá vị trí");
                alertDialog.setMessage("Xoá nhắc nhở cho vị trí này?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mMap.clear();
                                alarmLL = null;
                                btnDeleteAlarm.setVisibility(View.INVISIBLE);
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Huỷ",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != SearchActivity.SEARCH_MODE_CANCEL) {
            super.onActivityResult(requestCode, resultCode, data);
            double lat, lng;
            lat = data.getDoubleExtra("lat", 0);
            lng = data.getDoubleExtra("long", 0);
            Geocoder gcd = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(lat, lng, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (requestCode) {
                case SearchActivity.SEARCH_MODE_SOURCE:
                    if (resultCode != SearchActivity.SEARCH_MODE_SUCCESS) break;
                    srcLL = new LatLng(lat,lng);
                    if (addresses.size() > 0) {
                        int maxAddressLine = addresses.get(0).getMaxAddressLineIndex();
                        btnSrc.setText(addresses.get(0).getAddressLine(maxAddressLine));
                    }
                    break;
                case SearchActivity.SEARCH_MODE_DEST:
                    if (resultCode != SearchActivity.SEARCH_MODE_SUCCESS) break;
                     dstLL = new LatLng(lat,lng);
                    if (addresses.size() > 0) {
                        int maxAddressLine = addresses.get(0).getMaxAddressLineIndex();
                        btnDst.setText(addresses.get(0).getAddressLine(maxAddressLine));
                    }
                    break;
                case SearchActivity.SEARCH_MODE_ALARM:
                    alarmLL = new LatLng(lat, lng);
                    processAlarm(alarmLL);
                    break;
                case 0:
                    break;
            }
        }
    }

    private void processAlarm(LatLng ll) {
        alarmLL = ll;
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(alarmLL);
        markerOptions.title("Điểm đến");
        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(alarmLL));
    }

    private void openSearch(int mode) {
        Intent t = new Intent(this, SearchActivity.class);
        startActivityForResult(t, mode);
    }

    private void calculateDistance() {
        if (srcLL != null && dstLL != null) {
            Location srcL = new Location("src");
            Location dstL = new Location("dst");
            srcL.setLongitude(srcLL.longitude);
            srcL.setLatitude(srcLL.latitude);
            dstL.setLongitude(dstLL.longitude);
            dstL.setLatitude(dstLL.latitude);
            NumberFormat formatter = new DecimalFormat("#0.000");

            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Khoảng cách");
            alertDialog.setMessage(formatter.format(srcL.distanceTo(dstL)) + "m");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            srcLL = dstLL = null;
        }
    }

    private void showDistanceDialog() {
        final AlertDialog.Builder distanceDialog = new AlertDialog.Builder(MainActivity.this);
        distanceDialog.setTitle("Chọn 2 điểm");
        LayoutInflater inflater = this.getLayoutInflater();
        View distanceDialogView = inflater.inflate(R.layout.distance, null);
        btnSrc = distanceDialogView.findViewById(R.id.btnSrc);
        btnDst = distanceDialogView.findViewById(R.id.btnDst);

        btnSrc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch(SearchActivity.SEARCH_MODE_SOURCE);
            }
        });

        btnDst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSearch(SearchActivity.SEARCH_MODE_DEST);
            }
        });

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // User clicked the Yes button
                        calculateDistance();
                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        srcLL = null;
                        dstLL = null;
                        break;
                }
            }
        };
        distanceDialog.setPositiveButton("OK", dialogClickListener);
        distanceDialog.setNegativeButton("Cancel", dialogClickListener);

        distanceDialog.setView(distanceDialogView);
        distanceDialog.show();
    }

    private void addControls() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        fab_alarm = findViewById(R.id.fab_alarm);
        fab_distance = findViewById(R.id.fab_distance);
        loc = findViewById(R.id.loc);
        fab_menu = findViewById(R.id.add_menu);
        fab_menu.bringToFront();
        btnDeleteAlarm = findViewById(R.id.btn_delete_alarm);
        client = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_menu) {
            BottomSheetDialog bottomSheet = new BottomSheetDialog();
            bottomSheet.show(getSupportFragmentManager(),"bottomSheetDialog");
            return true;        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);

        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                fab_menu.collapse();
                fab_menu.setVisibility(View.INVISIBLE);
                btnDeleteAlarm.setVisibility(View.VISIBLE);
                return false;
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                fab_menu.setVisibility(View.VISIBLE);
                fab_menu.collapse();
                btnDeleteAlarm.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(MainActivity.this,"Location change",Toast.LENGTH_LONG).show();
        if (alarmLL != null) {
            Toast.makeText(MainActivity.this,"Location change",Toast.LENGTH_LONG).show();

            Location alarmLocation = new Location("Alarm Location");
            alarmLocation.setLatitude(alarmLL.latitude);
            alarmLocation.setLongitude(alarmLL.longitude);
            if (location.distanceTo(alarmLocation) < 200) {
                showAlarm();
            }

        }
    }

    public void Alarm(){

        try {
            FileInputStream fin = openFileInput("myfile");
            int c;
            c = fin.read();
            if(c == '1') isringtone = true;
            else isringtone = false;
            c = fin.read();
            if(c == '1') isring = true;
            else  isring = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlarm() {
        final AlertDialog.Builder AlarmDialog = new AlertDialog.Builder(MainActivity.this);
        AlarmDialog.setTitle("Báo thức");
        LayoutInflater inflater = this.getLayoutInflater();
        View AlarmDialogView = inflater.inflate(R.layout.alarm,null);
        btnOk = AlarmDialogView.findViewById(R.id.btnOk);

        final AlertDialog dialog = AlarmDialog.create();
        startService(new Intent(this.getApplicationContext(), Music.class));
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        long[] mVibratePattern = new long[]{0, 300, 100, 400};


        vibrator.vibrate(mVibratePattern, 3);
        Log.e(MainActivity.class.getSimpleName(),"rung");
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                stopService(new Intent(getApplicationContext(), Music.class));
                vibrator.cancel();
            }
        });

        dialog.setView(AlarmDialogView);
        dialog.show();
    }
}
