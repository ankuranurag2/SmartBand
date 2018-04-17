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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankuranurag2.smartband.Utils.PermissionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {

    //Views
    ImageView statusIv;
    TextView nameTv, connectBtn, contactBtn, recieveBtn, gpsBtn, locateBtn, home, removeBtn, addressTv;
    TextView lati, longi, address, distance, homeLati, homeLongi;
    ProgressDialog progress;
    Handler handler;

    //data
    String deviceName, deviceAddress;
    boolean isConnected = false;

    final int RECIEVE_MESSAGE = 1;     //used to identify handler message

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    ConnectedThread mConnectedThread;
    StringBuilder builder;

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
        builder = new StringBuilder();

        setData();
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected)
                    new ConnectBT().execute();
                else {
                    try {
                        progress = ProgressDialog.show(ConnectionActivity.this, "Disconnecting...", "Please wait!!!");
                        btSocket.close();
                        isConnected = false;
                        statusIv.setBackgroundColor(Color.RED);
                        connectBtn.setText("CONNECT");
                        connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_connect, 0, 0);
                        progress.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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
                if (isConnected && btSocket != null) {
                    mConnectedThread = new ConnectedThread(btSocket);
                    mConnectedThread.run();
                } else {
                    Toast.makeText(ConnectionActivity.this, "Please connect with device first.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);
                        builder.append(strIncom);
                        int endOfLineIndex = builder.indexOf("\r\n");
                        if (endOfLineIndex > 0) {
                            String sbprint = builder.substring(0, endOfLineIndex);
                            builder.delete(0, builder.length());
//                            txtArduino.setText("Data from Arduino: " + sbprint);
//                            btnOff.setEnabled(true);
//                            btnOn.setEnabled(true);
                        }
                        //Log.d(TAG, "...String:"+ builder.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            }
        };
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
                statusIv.setBackgroundColor(Color.GREEN);
                connectBtn.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_bt_disabled, 0, 0);
                connectBtn.setText("DISCONNECT");
            }
            progress.dismiss();
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;

        private ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    handler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (btSocket != null && btSocket.isConnected())
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onDestroy();
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

//TO SEND DATA
//        public void write(String message) {
//            byte[] msgBuffer = message.getBytes();
//            try {
//                mmOutStream.write(msgBuffer);
//            } catch (IOException e) {
//            }
//        }
