package com.ankuranurag2.smartband;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankuranurag2.smartband.Utils.PermissionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {

    //Views
    ImageView statusIv;
    TextView nameTv, connectBtn, contactBtn, recieveBtn, gpsBtn, locateBtn, home, removeBtn, addressTv;
    TextView lati, longi, address, distance, homeLati, homeLongi, sendBtn;
    ProgressDialog progress;

    //data
    String deviceName, deviceAddress;
    boolean isConnected = false;

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket btSocket = null;

    //After connection
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    volatile boolean stopWorker;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;

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
                PermissionUtils.showContactAlert(ConnectionActivity.this);
            }
        });

        removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.showDeleteAlert(ConnectionActivity.this);
            }
        });

        recieveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket != null) {
                    if (btSocket.isConnected()) {
                        try {
                            listenForData();
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
                statusIv.setBackgroundColor(Color.GREEN);
                connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_disabled, 0, 0);
                connectBtn.setText("DISCONNECT");
            }
            progress.dismiss();
        }
    }

    private void listenForData() throws IOException {
        mmInputStream=btSocket.getInputStream();
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[256];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
//                                            data
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }

    void sendData() throws IOException {
        mmOutputStream = btSocket.getOutputStream();
        String msg = "#1~";
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
    }

    void closeBT() throws Exception {
        stopWorker = true;
        mmOutputStream.close();
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


