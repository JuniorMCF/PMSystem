package com.pmsystem.app.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pmsystem.app.BluetoothActivity;
import com.pmsystem.app.PMActivity;
import com.pmsystem.app.R;
import com.pmsystem.app.clases.Constants;

import java.util.List;

public class BluetoothListPairedAdapter extends RecyclerView.Adapter<BluetoothListPairedAdapter.MyViewHolder> {

    private Context context;
    private List<BluetoothDevice> bluetoothPairedDevicesList;

    public BluetoothListPairedAdapter( List<BluetoothDevice> bluetoothPairedDevicesList,Context context) {
        this.context = context;
        this.bluetoothPairedDevicesList = bluetoothPairedDevicesList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_bluetooth,parent,false);

        return new BluetoothListPairedAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        holder.txtDispositivo.setText(bluetoothPairedDevicesList.get(position).getName());
        holder.txtMac.setText(bluetoothPairedDevicesList.get(position).getAddress());

        holder.cvBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final BluetoothDevice devicetemp = bluetoothPairedDevicesList.get(position);
                //((BluetoothActivity)context).getBluetoothAdapter().cancelDiscovery();
                Intent intent = new Intent(context.getApplicationContext(), PMActivity.class);
                //intent.putExtra("paired",devicetemp);
                context.startActivity(intent);
                //((BluetoothActivity)context).finishAffinity();

            }
        });

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView txtDispositivo;
        private TextView txtMac;
        private CardView cvBluetooth;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            txtDispositivo = itemView.findViewById(R.id.txtDispositivoPaired);
            txtMac = itemView.findViewById(R.id.txtMacPaired);
            cvBluetooth = itemView.findViewById(R.id.cv_bluetooth_paired);


        }
    }
}
