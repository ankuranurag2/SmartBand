package com.ankuranurag2.smartband;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;


public class PairedAdapter extends RecyclerView.Adapter<PairedAdapter.PairedViewHolder> {


    ArrayList<BTDevice> pairedList = new ArrayList<>();
    BluetoothAdapter adapter;
    private static final UUID myUUID = UUID.fromString("f07fc500-6289-405c-ab37-b015033a6bca");

    public PairedAdapter(ArrayList<BTDevice> list, BluetoothAdapter adapter) {
        this.pairedList = list;
        this.adapter = adapter;
        ArrayList<String> pairedList = new ArrayList<>();
    }

    @Override
    public PairedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater
                .inflate(R.layout.device_row, parent, false);
        return new PairedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PairedViewHolder holder, int position) {
        String name = pairedList.get(position).getDeviceName();
        holder.deviceName.setText(name);
    }

    @Override
    public int getItemCount() {
        return pairedList.size();
    }

    public class PairedViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;
        LinearLayout layout;

        public PairedViewHolder(final View itemView) {
            super(itemView);
            deviceName = (TextView) itemView.findViewById(R.id.device_text_view);
            layout = (LinearLayout) itemView.findViewById(R.id.device_layout);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            BluetoothSocket socket = null;
                            BluetoothDevice device = adapter.getRemoteDevice(pairedList
                                    .get(getAdapterPosition()).getDeviceAddr());
                            try {
                                socket = device.createRfcommSocketToServiceRecord(myUUID);
                                adapter.cancelDiscovery();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {

                                socket.connect();

                            } catch (IOException e) {
                                try {
                                    socket = (BluetoothSocket) device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[]{UUID.class}).invoke(device, myUUID);
                                    socket.connect();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }
                    };
                    thread.start();
                }
            });

        }
    }
}
