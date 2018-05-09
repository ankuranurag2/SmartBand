package com.ankuranurag2.smartband;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ankuranurag2.smartband.Utils.PermissionUtils;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //Views
    TextView gpsBtn, locateBtn, home, connect;
    Button refresh;
    LinearLayout footer;
    BluetoothAdapter mBluetoothAdapter;
    RecyclerView recyclerView;
    ImageView status;

    //Data
    double lati, longi;
    Boolean btStatus = false;

    //GPS
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    //REQUEST CODES
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST_CODE = 901;
    private final static int LOCATION_REQUEST_CODE = 902;
    private final static int CHECK_SETTINGS_REQUEST_CODE = 903;

    //SHARED PREFRENCES KEYS
    public final static String MY_PREFS_NAME = "MyPrefsFile";
    public final static String PREF_CONTACT_LIST = "contactList";
    public final static String PREF_HOME_LAT = "homeLat";
    public final static String PREF_HOME_LONG = "homeLong";
    public final static String PREF_LATEST_LAT = "lat";
    public final static String PREF_LATEST_LONG = "long";
    public final static String PREF_LATEST_ADDRESS = "add";

    //TRACKING VARIABLES
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
        home = (TextView) findViewById(R.id.homee);
        refresh = (Button) findViewById(R.id.refresh);
        footer = (LinearLayout) findViewById(R.id.footer_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recylerview);
        status = (ImageView) findViewById(R.id.status);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null)
            btStatus = mBluetoothAdapter.isEnabled();
        else
            Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();

        if (btStatus) {
            connect.setText("TURN OFF");
            status.setBackgroundColor(getResources().getColor(R.color.material_green));
            connect.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_disabled, 0, 0);
        } else {
            connect.setText("TURN ON");
            status.setBackgroundColor(Color.RED);
            connect.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_connect, 0, 0);
        }


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!btStatus) {
                    footer.setBackgroundColor(getResources().getColor(R.color.material_green));
                    mBluetoothAdapter.enable();
                    btStatus = true;
                    connect.setText("TURN OFF");
                    status.setBackgroundColor(getResources().getColor(R.color.material_green));
                    connect.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_disabled, 0, 0);
                } else {
                    footer.setBackgroundColor(Color.RED);
                    mBluetoothAdapter.disable();
                    btStatus = false;
                    connect.setText("TURN ON");
                    status.setBackgroundColor(Color.RED);
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
                if (btStatus) {
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
                } else
                    Toast.makeText(MainActivity.this, "Please Turn ON Bluetooth.", Toast.LENGTH_SHORT).show();
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                footer.setBackgroundColor(getResources().getColor(R.color.material_blue));
                PermissionUtils.showHomeAlert(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_SETTINGS_REQUEST_CODE && resultCode == RESULT_OK) {
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
        if (mGoogleApiClient != null)
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
                        PLAY_SERVICES_RESOLUTION_REQUEST_CODE).show();
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
                            locationSettingsResult.getStatus().startResolutionForResult(MainActivity.this, CHECK_SETTINGS_REQUEST_CODE);
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
                home.setEnabled(true);
                mLastLocation = location;
                lati = location.getLatitude();
                longi = location.getLongitude();
                String address = "";
                Geocoder coder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> list = coder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                    if (list != null && list.size() > 0) {
                        Address data = list.get(0);
                        String subLocality = data.getSubLocality();
                        String locality = data.getLocality();
                        String postalCode = data.getPostalCode();
                        if (subLocality != null && !subLocality.isEmpty())
                            address += subLocality;
                        if (locality != null && !locality.isEmpty())
                            address += ", " + locality;
                        if (postalCode != null && !postalCode.isEmpty())
                            address += ", " + postalCode;
                        Log.d("TAG", address);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Got your location.", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(PREF_LATEST_LAT, String.valueOf(lati));
                editor.putString(PREF_LATEST_LONG, String.valueOf(longi));
                editor.putString(PREF_LATEST_ADDRESS, String.valueOf(address));
                editor.commit();

                UPDATE_INTERVAL = 20000; // 20 sec
                FATEST_INTERVAL = 10000; // 10 sec
                DISPLACEMENT = 10;  // 10 meters
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
            locateBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_location_off, 0, 0);
            mRequestingLocationUpdates = true;
            startLocationUpdates();
        } else {
            locateBtn.setText("TRACK");
            locateBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_locate, 0, 0);
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

