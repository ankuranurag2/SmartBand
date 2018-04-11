package com.ankuranurag2.smartband;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

public class ConnectedActivity extends AppCompatActivity {

    //Views
    ImageView statusIv;
    TextView nameTv, connectBtn;
    ProgressDialog progress;

    //data
    String deviceName, deviceAddress;
    boolean isConnected = false;

    BluetoothAdapter mBluetoothAdapter=null;
    BluetoothSocket btSocket=null;

    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connected);

        deviceName = getIntent().getExtras().getString("name");
        deviceAddress = getIntent().getExtras().getString("add");

        connectBtn = (TextView) findViewById(R.id.connect);
        nameTv = (TextView) findViewById(R.id.name);
        statusIv = (ImageView) findViewById(R.id.status);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        nameTv.setText(deviceName);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected)
                    new ConnectBT().execute();
                else{
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ConnectedActivity.this, "Connecting...", "Please wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isConnected) {
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = mBluetoothAdapter.getRemoteDevice(deviceAddress);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
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
                Toast.makeText(ConnectedActivity.this, "Connection Failed.\nMake sure device is SPP Bluetooth device!", Toast.LENGTH_SHORT).show();
                isConnected=false;
                statusIv.setBackgroundColor(getResources().getColor(R.color.red));
                connectBtn.setText("CONNECT");
            } else {
                Toast.makeText(ConnectedActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                isConnected = true;
                statusIv.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                connectBtn.setText("DISCONNECT");
            }
            progress.dismiss();
        }
    }
}
