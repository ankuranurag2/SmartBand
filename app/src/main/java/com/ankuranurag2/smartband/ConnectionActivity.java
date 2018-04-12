package com.ankuranurag2.smartband;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectionActivity extends AppCompatActivity {

    //Views
    ImageView statusIv;
    TextView nameTv, connectBtn;
    ProgressDialog progress;
    Handler handler;

    //data
    String deviceName, deviceAddress;
    boolean isConnected = false;

    final int RECIEVE_MESSAGE = 1;     //used to identify handler message

    BluetoothAdapter mBluetoothAdapter = null;
    BluetoothSocket btSocket = null;
    StringBuilder sb;

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
        sb = new StringBuilder();

        nameTv.setText(deviceName);

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
                        connectBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_bt_connect,0,0);
                        progress.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

//        bluetoothIn = new Handler() {
//            public void handleMessage(android.os.Message msg) {
//                if (msg.what == handlerState) {										//if message is what we want
//                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
//                    recDataString.append(readMessage);      								//keep appending to string until ~
//                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
//                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
//                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
//                        txtString.setText("Data Received = " + dataInPrint);
//                        int dataLength = dataInPrint.length();							//get length of data received
//                        txtStringLength.setText("String Length = " + String.valueOf(dataLength));
//
//                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
//                        {
//                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
//                            String sensor1 = recDataString.substring(6, 10);            //same again...
//                            String sensor2 = recDataString.substring(11, 15);
//                            String sensor3 = recDataString.substring(16, 20);

//                            sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "V");	//update the textviews with sensor values
//                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V");
//                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V");
//                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
//                        }
//                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
//                        dataInPrint = " ";
//                    }
//                }
//            }
//        };


        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECIEVE_MESSAGE:                                                   // if receive massage
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                            sb.delete(0, sb.length());                                      // and clear
//                            txtArduino.setText("Data from Arduino: " + sbprint);            // update TextView
//                            btnOff.setEnabled(true);
//                            btnOn.setEnabled(true);
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
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
                Toast.makeText(ConnectionActivity.this, "Connection Failed.Make sure device is SPP Bluetooth device!!!", Toast.LENGTH_SHORT).show();
                isConnected = false;
                statusIv.setBackgroundColor(Color.RED);
                connectBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_bt_connect,0,0);
                connectBtn.setText("CONNECT");
            } else {
                Toast.makeText(ConnectionActivity.this, "Connected.", Toast.LENGTH_SHORT).show();
                isConnected = true;
                statusIv.setBackgroundColor(getResources().getColor(R.color.material_green));
                connectBtn.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.ic_bt_disabled,0,0);
                connectBtn.setText("DISCONNECT");
            }
            progress.dismiss();
        }
    }

    public void sendSMS(String phoneNo, String msg) {
        SharedPreferences pref=getSharedPreferences(MainActivity.MY_PREFS_NAME,MODE_PRIVATE);
        String lat=pref.getString("lat","0");
        String lon=pref.getString("long","0");
        String address=pref.getString("add","");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    handler.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            byte[] msgBuffer = message.getBytes();
            try {
                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
            }
        }
    }
}
