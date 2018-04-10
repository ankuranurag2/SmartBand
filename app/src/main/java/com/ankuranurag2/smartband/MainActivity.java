package com.ankuranurag2.smartband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ImageView btStatus;
    Button toogleButton;
    Button listButton;
    BluetoothAdapter mBluetoothAdapter;
    RecyclerView btRecyclerView;
    ArrayList<BTDevice> pairedList;
    ArrayList<BTDevice> activeList;
    PairedAdapter adapter;
    GPSTracker tracker;
    double lati, longi;

    private byte[] buffer = new byte[8192];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btStatus = (ImageView) findViewById(R.id.bt_image_view);
        toogleButton = (Button) findViewById(R.id.bt_toogle_button);
        listButton = (Button) findViewById(R.id.paired_device_button);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        activeList = new ArrayList<>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                btStatus.setBackgroundResource(R.color.colorPrimary);
                toogleButton.setText("TURN BT OFF");
                listButton.setEnabled(true);
            }
        }

        toogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toogleBT();
            }
        });
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getList();
                setAdapter();
                if (tracker.canGetLocation()) {
                    lati = tracker.getLatitude();
                    longi = tracker.getLongitude();

                } else {
                    tracker.showSettingsAlert();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        tracker = new GPSTracker(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tracker.stopUsingGPS();
        unregisterReceiver(mReceiver);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BTDevice newDevice = new BTDevice(device.getName(), device.getAddress());
                activeList.add(newDevice);
            }
        }
    };

    public void toogleBT() {

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                btStatus.setBackgroundResource(R.color.colorPrimary);
                toogleButton.setText("TURN BT OFF");
                listButton.setEnabled(true);
            } else {
                mBluetoothAdapter.disable();
                btStatus.setBackgroundResource(R.color.red);
                toogleButton.setText("TURN BT ON");
                listButton.setEnabled(false);
                pairedList = new ArrayList<>();
                setAdapter();
            }
        }
    }

    public void getList() {
        pairedList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                BTDevice device = new BTDevice(bt.getName(), bt.getAddress());
                pairedList.add(device);
            }
        }
    }

    public void setAdapter() {
        adapter = new PairedAdapter(pairedList,mBluetoothAdapter);
        btRecyclerView = (RecyclerView) findViewById(R.id.btlist_recycler_view);
        btRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btRecyclerView.setAdapter(adapter);
    }

    public void sendSMS(View view) {
        try {
            SmsManager manager = SmsManager.getDefault();
            manager.sendTextMessage("123", null, "I need your urgent help.\n" +
                    "MY LOCATION: Latitude-" + lati + " \nLongitude-" + longi, null, null);
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

//    private class AcceptData extends Thread {
//        private final BluetoothServerSocket mmServerSocket;
//        private BluetoothSocket socket = null;
//        private InputStream mmInStream;
//        private String device;
//
//        private AcceptData() {
//            BluetoothServerSocket tmp = null;
//            try {
//                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Bluetooth", myUUID);
//            } catch (IOException e) {
//                //
//            }
//            mmServerSocket = tmp;
//            try {
//                socket = mmServerSocket.accept();
//            } catch (IOException e) {
//                //
//            }
//            device = socket.getRemoteDevice().getName();
//            Toast.makeText(getBaseContext(), "Connected to " + device, Toast.LENGTH_SHORT).show();
//            InputStream tmpIn = null;
//            try {
//                tmpIn = socket.getInputStream();
//            } catch (IOException e) {
//                //
//            }
//            mmInStream = tmpIn;
//            int byteNo;
//            try {
//                byteNo = mmInStream.read(buffer);
//                if (byteNo != -1) {
//                    //ensure DATAMAXSIZE Byte is read.
//                    int byteNo2 = byteNo;
//                    int bufferSize = 7340;
//                    while (byteNo2 != bufferSize) {
//                        bufferSize = bufferSize - byteNo2;
//                        byteNo2 = mmInStream.read(buffer, byteNo, bufferSize);
//                        if (byteNo2 == -1) {
//                            break;
//                        }
//                        byteNo = byteNo + byteNo2;
//                    }
//                }
//                if (socket != null) {
//                    try {
//                        mmServerSocket.close();
//                    } catch (IOException e) {
//                        //
//                    }
//                }
//            } catch (Exception e) {
//                // TODO: handle exception
//            }
//        }
//    }

}
