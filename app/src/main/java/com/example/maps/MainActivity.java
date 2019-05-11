package com.example.maps;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.common.api.Status;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private Toolbar toolbar;
    private com.getbase.floatingactionbutton.FloatingActionButton fab_distance, fab_alarm;
    private FloatingActionsMenu fab_menu;
    private FloatingActionButton loc;
    private ImageButton btnDeleteAlarm;
    private Button btnSrc, btnDst, btnOk;
    private LatLng srcLL, dstLL, alarmLL = null;
    private GoogleMap mMap;
    private AutocompleteSupportFragment autocompleteFragment;
    private FusedLocationProviderClient client;
    private LocationManager locationManager;
    private boolean ringing = false, isringtone = true;

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
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Places.initialize(getApplicationContext(), getResources().getString(R.string.place_api_key));
        PlacesClient placesClient = Places.createClient(this);
        addControls();
        addEvents();
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
                        restartApp();
                    }

                }
                else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }

    private void restartApp() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static Boolean isLocationEnabled(Context context)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // This is new method provided in API 28
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return lm.isLocationEnabled();
        } else {
        // This is Deprecated in API 28
            int mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                    Settings.Secure.LOCATION_MODE_OFF);
            return  (mode != Settings.Secure.LOCATION_MODE_OFF);

        }
    }

    private void addEvents() {
        fab_distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDistanceDialog();
            }
        });
        fab_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Kiểm tra vị trí có hoạt động không
                if (isLocationEnabled(MainActivity.this)) openSearch(SearchActivity.SEARCH_MODE_ALARM);
                else {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(R.string.app_name)
                            .setMessage("Kiểm tra vị trí thiết bị")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .create()
                            .show();
                }

            }
        });

        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    checkLocationPermission();
                    return;
                }
                if (!mMap.isMyLocationEnabled()) mMap.setMyLocationEnabled(true);
                client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                            mMap.animateCamera( CameraUpdateFactory.zoomTo( 15.0f ) );
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
                                stopNotificationService();
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

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                LatLng ll = place.getLatLng();
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(ll);
                markerOptions.title(place.getAddress());
                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(MainActivity.this, status.toString(), Toast.LENGTH_LONG).show();
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

        Intent notiIntent = new Intent(this, NotificationService.class);
        ContextCompat.startForegroundService(this, notiIntent);
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
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_main);
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteFragment.setTypeFilter(TypeFilter.ADDRESS);
        autocompleteFragment.setCountry("VN");
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        loc = findViewById(R.id.loc);
        fab_alarm = findViewById(R.id.fab_alarm);
        fab_distance = findViewById(R.id.fab_distance);
        fab_menu = findViewById(R.id.add_menu);
        fab_menu.bringToFront();
        btnDeleteAlarm = findViewById(R.id.btn_delete_alarm);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_main);
        mapFragment.getMapAsync(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission();
            return;
        }
        client = LocationServices.getFusedLocationProviderClient(this);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
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
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
                .setTitle(R.string.text_ask_to_exit)
                .setMessage(R.string.text_advice_to_exit)
                .setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        stopNotificationService();
                        finish();
                    }
                })
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
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
            public boolean onMarkerClick(final Marker marker) {
                // Kiểm tra marker có phải là alarmLL hay không
                // Nếu phải, click vào cho phép xoá
                // Nếu không, click vào hỏi muốn đặt làm alarmLL không
                Location srcL = new Location("src");
                srcL.setLongitude(marker.getPosition().longitude);
                srcL.setLatitude(marker.getPosition().latitude);
                Location dstL = new Location("dst");
                if (alarmLL != null) {
                    dstL.setLongitude(alarmLL.longitude);
                    dstL.setLatitude(alarmLL.latitude);
                }

                if (alarmLL != null && srcL.distanceTo(dstL) == 0) {
                    fab_menu.collapse();
                    fab_menu.setVisibility(View.INVISIBLE);
                    btnDeleteAlarm.setVisibility(View.VISIBLE);
                    return false;
                }
                else{
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Đặt nhắc nhở");
                    alertDialog.setMessage("Đặt nhắc nhở cho vị trí này?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    alarmLL = marker.getPosition();
                                    mMap.clear();
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(alarmLL);
                                    markerOptions.title("Điểm đến");
                                    mMap.addMarker(markerOptions);
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
                    return true;
                }
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
        if (alarmLL != null && !ringing) {
            Location alarmLocation = new Location("Alarm Location");
            alarmLocation.setLatitude(alarmLL.latitude);
            alarmLocation.setLongitude(alarmLL.longitude);
            if (location.distanceTo(alarmLocation) < 200) {
                showAlarm();
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


    private void showAlarm() {
        startService(new Intent(this.getApplicationContext(), Music.class));
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        ringing = true;
        final long[] mVibratePattern = new long[]{0, 300, 100, 400};

//        BroadcastReceiver vibrateReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
//                    vibrator.vibrate(mVibratePattern, 3);
//                }
//            }
//        };
//
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(vibrateReceiver, filter);


        vibrator.vibrate(mVibratePattern, 3);

        new AlertDialog.Builder(this)
                .setTitle("Thông báo")
                .setMessage("Sắp tới nơi")
                .setPositiveButton("Thoát", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mMap.clear();
                        dialogInterface.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        stopService(new Intent(getApplicationContext(), Music.class));
                        vibrator.cancel();
                        ringing = false;
                        alarmLL = null;
                        stopNotificationService();
                        btnDeleteAlarm.setVisibility(View.INVISIBLE);
                    }
                })
                .create()
                .show();
    }

    private void stopNotificationService(){
        Intent notiIntent = new Intent(getApplicationContext(), NotificationService.class);
        stopService(notiIntent);
    }
}
