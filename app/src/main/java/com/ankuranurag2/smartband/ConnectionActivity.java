package com.ankuranurag2.smartband;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankuranurag2.smartband.Utils.PermissionUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {

    //Views
    ImageView statusIv;
    TextView nameTv, connectBtn, contactBtn, recieveBtn, gpsBtn, locateBtn, home, removeBtn, addressTv;
    TextView lati, longi, address, distance, homeLati, homeLongi;
    ProgressDialog progress;

    //data
    String deviceName, deviceAddress;
    boolean isConnected = false;

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket btSocket = null;

    //After connection
    InputStream mmInputStream;
    volatile boolean stopWorker;
    Thread workerThread;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        connectBtn = (TextView) findViewById(R.id.connect);
        contactBtn = (TextView) findViewById(R.id.contact);
        recieveBtn = (TextView) findViewById(R.id.recieve);
        removeBtn = (TextView) findViewById(R.id.remove);
        gpsBtn = (TextView) findViewById(R.id.gps);
        locateBtn = (TextView) findViewById(R.id.locate);
        home = (TextView) findViewById(R.id.homee);
        nameTv = (TextView) findViewById(R.id.name);
        addressTv = (TextView) findViewById(R.id.mac);
        statusIv = (ImageView) findViewById(R.id.status);


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        setData();
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected)
                    new ConnectBT().execute();
                else {
                    progress = ProgressDialog.show(ConnectionActivity.this, "Disconnecting...", "Please wait!!!");

                    try {
                        closeBT();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isConnected = false;
                    statusIv.setBackgroundColor(Color.RED);
                    connectBtn.setText("CONNECT");
                    connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_connect, 0, 0);
                    progress.dismiss();
                }
            }
        });

        contactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences preferences = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);
                final Gson gson = new Gson();
                final LinkedHashMap<Integer, String> contactMap;
                final Type type = new TypeToken<LinkedHashMap<Integer, String>>() {
                }.getType();
                if (preferences.contains(MainActivity.PREF_CONTACT_LIST)) {
                    String jsonString = preferences.getString(MainActivity.PREF_CONTACT_LIST, "");
                    contactMap = gson.fromJson(jsonString, type);
                } else
                    contactMap = new LinkedHashMap<>();

                if (contactMap.size() == 5)
                    Toast.makeText(ConnectionActivity.this, "Maximum five contacts can be added.", Toast.LENGTH_SHORT).show();
                else {
                    Intent i = new Intent(Intent.ACTION_PICK);
                    i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(i, 199);
                }
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                PermissionUtils.showDeleteAlert(ConnectionActivity.this);
            }
        });

        recieveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.sendSMS(ConnectionActivity.this);
                if (btSocket != null) {
                    if (btSocket.isConnected()) {
                        try {
                            listenForData(ConnectionActivity.this);
                            recieveBtn.setEnabled(false);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else
                        Toast.makeText(ConnectionActivity.this, "Please connect with device first.", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(ConnectionActivity.this, "Please connect with device first.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 199 && resultCode == Activity.RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = this.getContentResolver().query(contactUri, null,
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);
                PermissionUtils.showContactAlert(ConnectionActivity.this, number, name);
            }
            if (cursor != null)
                cursor.close();
        }
    }

    @Override
    protected void onDestroy() {
        if (btSocket != null && btSocket.isConnected())
            try {
                closeBT();
            } catch (Exception e) {
                e.printStackTrace();
            }
        super.onDestroy();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ConnectionActivity.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!mBluetoothAdapter.isEnabled())
                        mBluetoothAdapter.enable();
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
                    btSocket = device.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                Toast.makeText(ConnectionActivity.this, "Connection Failed.Make sure bluetooth is ON and devices are paired!!!", Toast.LENGTH_SHORT).show();
                isConnected = false;
                statusIv.setBackgroundColor(Color.RED);
                connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_connect, 0, 0);
                connectBtn.setText("CONNECT");
            } else {
                Toast.makeText(ConnectionActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                isConnected = true;
                recieveBtn.setEnabled(true);
                statusIv.setBackgroundColor(getResources().getColor(R.color.material_green));
                connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_disabled, 0, 0);
                connectBtn.setText("DISCONNECT");
            }
            progress.dismiss();
        }
    }

    private void listenForData(final Context context) throws IOException {
        mmInputStream = btSocket.getInputStream();

        stopWorker = false;
        workerThread = new Thread(new Runnable() {
            public void run() {
                byte[] buffer = new byte[256];
                int bytes;
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
//                        int bytesAvailable = mmInputStream.available();
//                        if (bytesAvailable > 0) {
                        bytes = mmInputStream.read(buffer);
                        if (bytes > 0)
                            PermissionUtils.sendSMS(context);
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void closeBT() throws Exception {
        stopWorker = true;
        mmInputStream.close();
        btSocket.close();
    }

    private void setData() {
        deviceName = getIntent().getExtras().getString("name");
        deviceAddress = getIntent().getExtras().getString("add");

        nameTv.setText(deviceName);
        addressTv.setText(deviceAddress);
        gpsBtn.setVisibility(View.GONE);
        locateBtn.setVisibility(View.GONE);
        home.setVisibility(View.GONE);
        contactBtn.setVisibility(View.VISIBLE);
        recieveBtn.setVisibility(View.VISIBLE);
        removeBtn.setVisibility(View.VISIBLE);

        lati = (TextView) findViewById(R.id.lati);
        longi = (TextView) findViewById(R.id.longi);
        address = (TextView) findViewById(R.id.address);
        distance = (TextView) findViewById(R.id.distance);
        homeLati = (TextView) findViewById(R.id.home_lati);
        homeLongi = (TextView) findViewById(R.id.home_longi);

        SharedPreferences pref = getSharedPreferences(MainActivity.MY_PREFS_NAME, MODE_PRIVATE);

        String lat = "0.0", lon = "0.0", add, homLat = "0.0", homLong = "0.0";

        if (pref.contains(MainActivity.PREF_LATEST_LAT)) {
            lat = pref.getString(MainActivity.PREF_LATEST_LAT, "0");
            lati.append(String.valueOf(lat));
        }
        if (pref.contains(MainActivity.PREF_LATEST_LONG)) {
            lon = pref.getString(MainActivity.PREF_LATEST_LONG, "0");
            longi.append(String.valueOf(lon));
        }
        if (pref.contains(MainActivity.PREF_LATEST_ADDRESS)) {
            add = pref.getString(MainActivity.PREF_LATEST_ADDRESS, "");
            address.append(add);
        }
        if (pref.contains(MainActivity.PREF_HOME_LAT)) {
            homLat = pref.getString(MainActivity.PREF_HOME_LAT, "0");
            homeLati.append(homLat);
        }
        if (pref.contains(MainActivity.PREF_HOME_LONG)) {
            homLong = pref.getString(MainActivity.PREF_HOME_LONG, "0");
            homeLongi.append(homLong);
        }
        double dist = PermissionUtils.getDistanceBetweenTwoPoints(new PointF(Float.valueOf(homLat), Float.valueOf(homLong)), new PointF(Float.valueOf(lat), Float.valueOf(lon)));
        String d = new DecimalFormat("##.00").format(dist / 1000) + " KM";

        distance.append(d);
    }
}


