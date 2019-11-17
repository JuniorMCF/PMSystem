package com.pmsystem.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.app.MediaRouteButton;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.pmsystem.app.adapters.BluetoothListAdapter;
import com.pmsystem.app.adapters.BluetoothListPairedAdapter;
import com.pmsystem.app.clases.Constants;
import com.pmsystem.app.clases.Devices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class BluetoothActivity extends AppCompatActivity {

    private BluetoothListAdapter bluetoothListAdapter;
    private RecyclerView rvDisponibles;

    private BluetoothListPairedAdapter bluetoothListPairedAdapter;
    private RecyclerView rvPaireds;

    private List<BluetoothDevice> btDevicesList;
    private BluetoothAdapter bluetoothAdapter;
    //dispositivos emparejados
    private Set<BluetoothDevice>  pairedDevices;
    private List<BluetoothDevice> btPairedDevicesList;
    private Dialog progressDialog;

    //variables de la vista principal
    private TextView txtTitle;
    private TextView txtTitle2;


    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);


        txtTitle = findViewById(R.id.txtTitleBTA);
        //txtTitle2 = findViewById(R.id.txtTitleBTP);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btPairedDevicesList =  new ArrayList<>();
        pairedDevices = bluetoothAdapter.getBondedDevices();

        //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);

        rvDisponibles = findViewById(R.id.rv_bluetooth_dispo);
        //rvPaireds = findViewById(R.id.rv_paired);
        btDevicesList = new ArrayList<>();

        //adaptador para los nuevos dispositivos (no emparejados)
        bluetoothListAdapter = new BluetoothListAdapter(btDevicesList,this);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 1);
        rvDisponibles.setLayoutManager(mLayoutManager);
        rvDisponibles.setItemAnimator(new DefaultItemAnimator());
        rvDisponibles.setAdapter(bluetoothListAdapter);

        //adaptador para los dispositivos emparejados
        /*bluetoothListPairedAdapter = new BluetoothListPairedAdapter(btPairedDevicesList,this);
        RecyclerView.LayoutManager mLayoutManagerPaired = new GridLayoutManager(this, 1);
        rvPaireds.setLayoutManager(mLayoutManagerPaired);
        rvPaireds.setItemAnimator(new DefaultItemAnimator());
        rvPaireds.setAdapter(bluetoothListPairedAdapter);
    */
        //iniciar dialog
        progressDialog = new Dialog(this);
        progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        progressDialog.setContentView(R.layout.custom_dialog_progress);

        TextView progressTv = progressDialog.findViewById(R.id.progress_tv);
        progressTv.setText(getResources().getString(R.string.title_progress_dialog));
        progressTv.setTextColor(ContextCompat.getColor(this, R.color.colorApp2));
        progressTv.setTextSize(19F);
        if(progressDialog.getWindow() != null)
            progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(300));

        progressDialog.setCancelable(false);
        progressDialog.show();


        startSearchingPaired();
        startSearching();
        // iniciamos la busqueda





    }

    private void startSearchingPaired(){
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                btPairedDevicesList.add(device);
            }

        }
    }

    private void startSearching() {
        if (bluetoothAdapter.startDiscovery()) {
            Log.d("aca","starting");
        } else {
            Log.d("aca","not starting");
            bluetoothAdapter.startDiscovery();
            startSearching();
        }
    }


    private void enableBluetooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
    }

    @Override protected void onStop() {
        super.onStop();
        Log.d(Constants.TAG, "Receiver unregistered");
        unregisterReceiver(mReceiver);
    }

    @Override protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startSearching();
            } else {
                enableBluetooth();
            }
        }

    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                btDevicesList.add(device);


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if(bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();
                }


                btDevicesList = filterDevices(btDevicesList);

                //finalizar progress
                progressDialog.dismiss();

                if(btDevicesList.size()>0){
                    txtTitle.setText("DISPOSITIVOS DISPONIBLES");
                    bluetoothListAdapter.notifyDataSetChanged();
                }else{
                    txtTitle.setText("No hay dispositivos cercanos");
                }


            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                            Log.d("dato","starting 6");
                        break;
                }
            }
        }
    };


    public List<BluetoothDevice> filterDevices(List<BluetoothDevice> list){
        Map <String, BluetoothDevice> map = new LinkedHashMap <String, BluetoothDevice> ();

        for (BluetoothDevice ays : list) {
            map.put(ays.getAddress(), ays);
        }
        list.clear();
        list.addAll(map.values());
        return list;

    }




    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        // Don't forget to unregister the ACTION_FOUND receiver.
    }

}
