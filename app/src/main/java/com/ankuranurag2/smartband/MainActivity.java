package com.ankuranurag2.smartband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    ImageView btStatus;
    Button btToggle;
    BluetoothAdapter mBluetoothAdapter;
    RecyclerView btRecyclerView;
    ArrayList<String> s;
    PairedAdapter adapter;
    BluetoothSPP bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btStatus = (ImageView) findViewById(R.id.bt_image_view);
        btToggle = (Button) findViewById(R.id.bt_button);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bt = new BluetoothSPP(this);
        s = new ArrayList<String>();

        if (mBluetoothAdapter==null){
            Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
        }else{
            if (mBluetoothAdapter.isEnabled()){
                btStatus.setBackgroundResource(R.color.colorPrimary);
                btToggle.setVisibility(View.GONE);
                getList();
                setAdapter();
            }
        }

        btToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnBtOn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void turnBtOn() {

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported.", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
                btStatus.setBackgroundResource(R.color.colorPrimary);
                btToggle.setVisibility(View.GONE);
                getList();
                setAdapter();
            }
        }
    }

    public void getList(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices){
            s.add(bt.getName());
        }
    }
    public void setAdapter(){
        Log.d("ankur", String.valueOf(s.size()));
        adapter=new PairedAdapter(s,bt);
        btRecyclerView=(RecyclerView)findViewById(R.id.btlist_recycler_view) ;
        btRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        btRecyclerView.setAdapter(adapter);
    }
}
