package com.ankuranurag2.smartband;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.UUID;


public class PairedAdapter extends RecyclerView.Adapter<PairedAdapter.PairedViewHolder> {


    private ArrayList<BTDevice> pairedList = new ArrayList<>();

    public PairedAdapter(ArrayList<BTDevice> list) {
        this.pairedList = list;
    }

    @Override
    public PairedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_row, parent, false);
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

        public PairedViewHolder(final View itemView) {
            super(itemView);
            deviceName = (TextView) itemView.findViewById(R.id.device_text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(itemView.getContext(),ConnectedActivity.class);
                    intent.putExtra("name",pairedList.get(getAdapterPosition()).getDeviceName());
                    intent.putExtra("add",pairedList.get(getAdapterPosition()).getDeviceAddr());
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
