package com.ankuranurag2.smartband;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Views
    TextView gpsBtn, locateBtn, refresh, connect;
    LinearLayout footer;
    BluetoothAdapter mBluetoothAdapter;
    RecyclerView recyclerView;

    //Data
    double lati, longi;
    String address;
    Boolean btStatus;

    //GPS
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int LOCATION_REQUEST_CODE = 101;


    private final static int REQUEST_CHECK_SETTINGS = 420;

    //TRACKING
    boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connect = (TextView) findViewById(R.id.connect);
        gpsBtn = (TextView) findViewById(R.id.gps);
        locateBtn = (TextView) findViewById(R.id.locate);
        refresh = (TextView) findViewById(R.id.refresh);
        footer = (LinearLayout) findViewById(R.id.footer_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recylerview);

        refresh.setVisibility(View.VISIBLE);
        connect.setText("TURN ON");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btStatus = mBluetoothAdapter.isEnabled();

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!btStatus) {
                    footer.setBackgroundColor(getResources().getColor(R.color.material_green));
                    mBluetoothAdapter.enable();
                    btStatus=true;
                    connect.setText("TURN OFF");
                    connect.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_disabled, 0, 0);
                } else {
                    footer.setBackgroundColor(Color.RED);
                    mBluetoothAdapter.disable();
                    btStatus=false;
                    connect.setText("TURN ON");
                    connect.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_connect, 0, 0);
                }
            }
        });

        locateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                footer.setBackgroundColor(getResources().getColor(R.color.material_pink));
                togglePeriodicLocationUpdates();
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                footer.setBackgroundColor(getResources().getColor(R.color.material_purple));
                if (checkPlayServices()) {
                    buildGoogleApiClient();
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                footer.setBackgroundColor(getResources().getColor(R.color.material_blue));
                ArrayList<BTDevice> pairedList = new ArrayList<BTDevice>();
                Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                for (BluetoothDevice device : pairedDevices) {
                    BTDevice btDevice = new BTDevice();
                    btDevice.setDeviceName(device.getName());
                    btDevice.setDeviceAddr(device.getAddress());
                    pairedList.add(btDevice);
                }
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.hasFixedSize();
                recyclerView.setAdapter(new PairedAdapter(pairedList));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.disconnect();
        stopLocationUpdates();
    }

    //Methods of Google GPS API
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLocation();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    //Method of LocationListener
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API).build();

            mGoogleApiClient.connect();
        }

        if (mLocationRequest == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        }

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build()).setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                int status = locationSettingsResult.getStatus().getStatusCode();
                switch (status) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        getLocation();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            locationSettingsResult.getStatus().startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });

    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {
                mLastLocation = location;
                lati = location.getLatitude();
                longi = location.getLongitude();

                Geocoder coder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> list = coder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address data = list.get(0);
                        address = data.getSubLocality() + "," + data.getLocality() + "," + data.getPostalCode();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, address, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Fetching Your Location", Toast.LENGTH_SHORT).show();
            }
        } else {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(perms, LOCATION_REQUEST_CODE);
        }
    }


    private void togglePeriodicLocationUpdates() {

        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        if (!mRequestingLocationUpdates) {
            locateBtn.setText("DON'T TRACK");
            locateBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_location_off,0,0);
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } else {
            locateBtn.setText("TRACK");
            locateBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_locate,0,0);
            mRequestingLocationUpdates = false;
            stopLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPermissions(perms, LOCATION_REQUEST_CODE);
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }
}

