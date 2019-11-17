package com.pmsystem.app.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pmsystem.app.BluetoothActivity;
import com.pmsystem.app.PMActivity;
import com.pmsystem.app.R;
import com.pmsystem.app.clases.Constants;
import com.pmsystem.app.clases.Devices;

import java.util.List;

public class BluetoothListAdapter extends RecyclerView.Adapter<BluetoothListAdapter.MyViewHolder> {

    private Context context;
    private List<BluetoothDevice> bluetoothDevicesList;

    public BluetoothListAdapter(List<BluetoothDevice> btlist, Context context) {
        this.context = context;
        this.bluetoothDevicesList = btlist;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_bluetooth,parent,false);


        return new BluetoothListAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.txtDispositivo.setText(bluetoothDevicesList.get(position).getName());
        holder.txtMac.setText(bluetoothDevicesList.get(position).getAddress());

        holder.cvBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final BluetoothDevice devicetemp = bluetoothDevicesList.get(position);
                //((BluetoothActivity)context).getBluetoothAdapter().cancelDiscovery();
                Intent intent = new Intent(context.getApplicationContext(), PMActivity.class);
                intent.putExtra(Constants.EXTRA_DEVICE,devicetemp);
                context.startActivity(intent);
                ((BluetoothActivity)context).finishAffinity();

            }
        });

    }

    @Override
    public int getItemCount() {

        return bluetoothDevicesList.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtDispositivo;
        private TextView txtMac;
        private CardView cvBluetooth;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDispositivo = itemView.findViewById(R.id.txtDispositivo);
            txtMac = itemView.findViewById(R.id.txtMac);
            cvBluetooth = itemView.findViewById(R.id.cv_bluetooth);
        }
    }
}
