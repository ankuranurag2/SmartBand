package com.ankuranurag2.smartband;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;



public class PairedAdapter extends RecyclerView.Adapter<PairedAdapter.PairedViewHolder> {

    ArrayList<String> pairedList=new ArrayList<>();

    public PairedAdapter(ArrayList<String> list){
        this.pairedList=list;
    }

    @Override
    public PairedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.device_row,parent,false);
        return new PairedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PairedViewHolder holder, int position) {
        Log.d("ankur",pairedList.get(position));
        String name=pairedList.get(position);
        holder.deviceName.setText(name);
    }

    @Override
    public int getItemCount() {
        return pairedList.size();
    }

    public class PairedViewHolder extends RecyclerView.ViewHolder {

        TextView deviceName;
        LinearLayout layout;

        public PairedViewHolder(View itemView) {
            super(itemView);
            deviceName=(TextView) itemView.findViewById(R.id.device_text_view);
            layout=(LinearLayout)itemView.findViewById(R.id.device_layout);

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

        }
    }
}
