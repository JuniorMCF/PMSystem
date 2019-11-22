package com.pmsystem.app;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.pmsystem.app.clases.BluetoothService;
import com.pmsystem.app.clases.Constants;
import com.pmsystem.app.clases.Data;
import com.pmsystem.app.clases.DataThingSpeak;
import com.pmsystem.app.clases.LocationUpdateBroadcast;
import com.pmsystem.app.services.RetrofitAPI;

import java.io.FileWriter;
import java.io.IOException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PMActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    GoogleMap mMap;

    Location posActual;

    List<MarkerOptions> markers;
    Polyline polyline = null;
    private static final int CHANNEL_ID = 915719;
    private static final String READ_API_KEY = "1JVT4LXT31WJ7WVX";

    private RetrofitAPI retrofitAPI; // retrofit


    public static int ACCESS_COARSE_LOCATION_CODE = 3410;
    public static int ACCESS_FINE_LOCATION_CODE = 3310;
    private static final int REQUEST_RESOLVE_ERROR = 555;
    private static final String DIALOG_ERROR = "dialog_error";
    private boolean mResolvingError = false;

    GoogleApiClient mGoogleApiClient;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothService bluetoothService;

    //variables pm2.5 y pm10
    private TextView etPM25;
    private TextView etPM10;
    TextView etNIVEL;

    private List<LatLng> latLngList;

    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }

    private Handler handler;


    private boolean isGPSChange = false;
    private boolean isGPSLocation, isNetworkLocation;

    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private static int FAST_TIME = 5000; //cambiar para modificar el tiempo de update
    private static int SLOW_TIME = 6000; // el tiempo de arriba + 1000
    private static PMActivity instance;

    private FileWriter writer;

    private Button btnIniciar;
    private Button btnCerrar;
    public static PMActivity getInstance() {
        return instance;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pm);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        instance = this;

        markers = new ArrayList<>();
        latLngList = new ArrayList<>();

        etPM25 = findViewById(R.id.txtPM2);
        etPM10 = findViewById(R.id.txtPM10);

        etNIVEL= findViewById(R.id.txtContanimacion);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnCerrar = findViewById(R.id.btnCerrar);

        myHandler handler = new myHandler(PMActivity.this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mDevice = getIntent().getExtras().getParcelable(Constants.EXTRA_DEVICE);

        bluetoothService = new BluetoothService(handler, mDevice);


        //conectandose a dispositivo
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);

        bluetoothService.connect();

        Log.d(Constants.TAG, "Connecting");

        // Build Google API Client for Location related work
        buildGoogleApiClient();


        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(btnIniciar.getText().toString().equalsIgnoreCase("Enviar datos")){
                    final AlertDialog.Builder builder = new AlertDialog.Builder(PMActivity.this);

                    builder.setTitle("Enviar Datos");
                    builder.setMessage("¿Esta seguro que desea enviar datos?");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            createNOtification();
                            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
                            btnIniciar.setText("Terminar Medición");
                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            builder.create().getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorApp));
                            builder.create().getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorApp));
                        }
                    });

                    builder.show();






                }else if (btnIniciar.getText().toString().equalsIgnoreCase("Terminar Medición")){

                    final AlertDialog.Builder builder = new AlertDialog.Builder(PMActivity.this);

                    builder.setTitle("Terminar Medición");
                    builder.setMessage("¿Esta seguro que desea terminar la medición de datos?");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (ActivityCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            getPendingIntent().cancel();
                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancelAll();
                            if(writer!=null){
                                try {
                                    writer.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            btnIniciar.setText("Enviar datos");
                            //polyline.remove();
                            latLngList.clear();
                            markers.clear();

                            Toast.makeText(PMActivity.this,"Datos guardados en ThingSpeak",Toast.LENGTH_SHORT).show();
                            //enviar data
                            //List<DataThingSpeak> sendData = new ArrayList<>();
                            //sendData = readFile();


                        }
                    });
                    builder.setNegativeButton("Cancel", null);
                    builder.create().setOnShowListener( new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface arg0) {
                            builder.create().getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getColor(R.color.colorApp));
                            builder.create().getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorApp));
                        }
                    });

                    builder.show();





                }




            }
        });



        btnCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(PMActivity.this);

                builder.setTitle("Cerrar PMSystem");
                builder.setMessage("¿Esta seguro que quiere cerrar la aplicación?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ActivityCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        getPendingIntent().cancel();
                        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();

                        finish();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {

                    }
                });

                builder.show();

            }
        });


    }


    // LECTURA DE FICHERO INTERNO
   /* private List<DataThingSpeak> readFile()
    {
        List<DataThingSpeak> myData = new ArrayList<>();
        File myExternalFile = new File("/data/data/com.pmsystem.app/data/","archivo.txt");
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String strLine;

            String[] split;

            while ((strLine = br.readLine()) != null) {
                DataThingSpeak data = new DataThingSpeak();
                split =  strLine.split(",");
                data.setLatitud(split[0]);
                data.setLongitud(split[1]);
                data.setPm25(split[2]);
                data.setPm10(split[3]);
                data.setMac(split[4]);

                myData.add(data);
            }
            br.close();
            in.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myData;
    }
*/
    public void sendingData(HashMap<String,String> data){
        retrofitAPI =  RetrofitAPI.getInstance();
        Call<List<DataThingSpeak>> getData = retrofitAPI.getThingspeakService().getData(READ_API_KEY,data);
            getData.enqueue(new Callback<List<DataThingSpeak>>() {
                @Override
                public void onResponse(Call<List<DataThingSpeak>> call, Response<List<DataThingSpeak>> response) {
                    Log.d("aca","data enviada");
                }

                @Override
                public void onFailure(Call<List<DataThingSpeak>> call, Throwable t) {
                    Log.d("aca","sending data fail");
                }
            });
    }

    private static class myHandler extends Handler {
        private final WeakReference<PMActivity> mActivity;
        public myHandler(PMActivity activity) {
            mActivity = new WeakReference<>(activity);
        }




        @Override
        public void handleMessage(Message msg) {

            final PMActivity activity = mActivity.get();

            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case Constants.STATE_CONNECTED:
                            Toast.makeText(activity,"Conectado",Toast.LENGTH_SHORT).show();
                            byte[] myvar = "1".getBytes();
                                activity.getBluetoothService().write(myvar);
                            break;
                        case Constants.STATE_CONNECTING:

                            break;
                        case Constants.STATE_NONE:

                            break;
                        case Constants.STATE_ERROR:

                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:

                    break;
                case Constants.MESSAGE_READ:
                    String readMessage = (String) msg.obj;
                    String[] parts = readMessage.split("\n");
                    Data pmas = new Data(parts[0],parts[1]);


                    if (readMessage != null) {
                        Location location = activity.posActual;
                        if(location != null){
                            float pm25 = Float.parseFloat(pmas.getPM25());
                            float pm10 = Float.parseFloat(pmas.getPM10());
                            if(pm10 <= 254 || pm25 <= 55.4){
                                //bandera verde
                                activity.etNIVEL.setTextColor(Color.GREEN);
                                activity.etNIVEL.setText("BUENO");
                                activity.mMap.clear();
                                activity.markers.add(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("bueno").icon(activity.bitmapDescriptorFromVector(activity,R.drawable.ic_flag_green)));
                                activity.latLngList.add(new LatLng(location.getLatitude(),location.getLongitude()));
                                for(MarkerOptions marker : activity.markers){
                                    activity.mMap.addMarker(marker);
                                    if(activity.polyline!=null){
                                        activity.polyline.remove();
                                    }
                                    PolylineOptions polylineOptions = new PolylineOptions().addAll(activity.latLngList).color(Color.RED).width(5f).clickable(true);
                                    activity.polyline = activity.mMap.addPolyline(polylineOptions);

                                }


                            }else if ((pm10 >= 255 && pm10 <= 354) || (pm25 >= 55.5 || pm25 <= 150.4)){
                                //bandera azul
                                activity.etNIVEL.setTextColor(Color.BLUE);
                                activity.etNIVEL.setText("MODERADO");
                                activity.mMap.clear();

                                activity.markers.add(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("moderado").icon(activity.bitmapDescriptorFromVector(activity,R.drawable.ic_flag_blue)));

                                activity.latLngList.add(new LatLng(location.getLatitude(),location.getLongitude()));
                                for(MarkerOptions marker : activity.markers){
                                    activity.mMap.addMarker(marker);
                                    if(activity.polyline!=null){
                                        activity.polyline.remove();
                                    }
                                    PolylineOptions polylineOptions = new PolylineOptions().addAll(activity.latLngList).color(Color.RED).width(5f).clickable(true);
                                    activity.polyline = activity.mMap.addPolyline(polylineOptions);

                                }
                                //activity.mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("Actual").icon(activity.bitmapDescriptorFromVector(activity,R.drawable.ic_flag_blue)));

                            }else if ((pm10 >= 355 && pm10 <= 604) || (pm25 >= 150.5 && pm25 <= 500.4) ){
                                //bandera roja
                                activity.etNIVEL.setTextColor(Color.RED);
                                activity.etNIVEL.setText("MALA");
                                activity.mMap.clear();
                                activity.markers.add(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("malo").icon(activity.bitmapDescriptorFromVector(activity,R.drawable.ic_flag_red)));

                                activity.latLngList.add(new LatLng(location.getLatitude(),location.getLongitude()));
                                for(MarkerOptions marker : activity.markers){
                                    activity.mMap.addMarker(marker);
                                    if(activity.polyline!=null){
                                        activity.polyline.remove();
                                    }
                                    PolylineOptions polylineOptions = new PolylineOptions().addAll(activity.latLngList).color(Color.RED).width(5f).clickable(true);
                                    activity.polyline = activity.mMap.addPolyline(polylineOptions);

                                }
                                //activity.mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("Actual").icon(activity.bitmapDescriptorFromVector(activity,R.drawable.ic_flag_red)));

                            }



                        }

                        float pm25 = Float.parseFloat(pmas.getPM25());
                        float pm10 = Float.parseFloat(pmas.getPM10());
                        if(pm10 <= 254 || pm25 <= 55.4){
                            //bandera verde
                            activity.etNIVEL.setTextColor(Color.GREEN);
                            activity.etNIVEL.setText("BUENO");

                        }else if ((pm10 >= 255 && pm10 <= 354) || (pm25 >= 55.5 || pm25 <= 150.4)){
                            //bandera azul
                            activity.etNIVEL.setTextColor(Color.BLUE);
                            activity.etNIVEL.setText("MODERADO");

                        }else if ((pm10 >= 355 && pm10 <= 604) || (pm25 >= 150.5 && pm25 <= 500.4) ){
                            //bandera roja
                            activity.etNIVEL.setTextColor(Color.RED);
                            activity.etNIVEL.setText("MALA");

                        }

                        activity.etPM25.setText(String.valueOf(pm25));
                        activity.etPM10.setText(String.valueOf(pm10));

                        /*if(Integer.parseInt(activity.etPM25.getText().toString())< 50){
                            activity.etNIVEL.setText("Bueno");
                        }*/

                    }

                    break;
                case Constants.MESSAGE_SNACKBAR:


                    break;
            }
        }


    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(PMActivity.this);


        if (ContextCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(PMActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            if (ContextCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(PMActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {

            }

            if (ContextCompat.checkSelfPermission(PMActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PMActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            } else {

            }
            return;
        }
        verificarGPSON();



    }



    public void updateData(final Location location) {
        Log.d("listo","segundo plano location = " + location);
        posActual = location;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),18.0f));
        byte[] myvar = "1".getBytes();
        bluetoothService.write(myvar);


        /*File root = Environment
                .getDataDirectory();
        File dir = new File(root.getAbsoluteFile() + "/data/com.pmsystem.app/data");
        if (!dir.exists()) {
            dir.mkdirs();
            //existeDir = true;
        }



        File file = new File(dir, "archivo" + ".txt");
        */
        String header = location.getLatitude()+","+location.getLongitude()+","+etPM25.getText()+","+etPM10.getText()+","+mDevice.getAddress()+"\n";


        HashMap<String,String> map = new HashMap<>();
        map.put("field1",""+location.getLatitude());
        map.put("field2",""+location.getLongitude());
        map.put("field3",""+etPM25.getText());
        map.put("field4",""+etPM10.getText());
        map.put("field5",""+mDevice.getAddress());

        try{
            sendingData(map);
        }catch (Exception e){
            Log.d("error",e.getMessage());
        }
 /*
            writer = new FileWriter(file, true);
            writer.append(header);

            writer.flush();

            */
        // Log.d("aca", "Saved " + points.size() + " points.");
    }

    private void createNOtification(){
        Intent intent2 = new Intent(this, PMActivity.class);
        intent2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent2.putExtra("reanudado",true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_NONE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo_monitoreo)
                .setContentTitle("PMASystem")
                .setContentText("Registrando Activo")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);
        //.addAction(R.drawable.ic_launcher_background, "Pausar Proyecto",pendingIntent);

        NotificationManagerCompat notificationManager2 = NotificationManagerCompat.from(this);
        notificationManager2.notify(1, builder.build());
    }



    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationUpdateBroadcast.class);
        intent.setAction(LocationUpdateBroadcast.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }



    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void verificarGPSON() {

        if(mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(SLOW_TIME);
            mLocationRequest.setFastestInterval(FAST_TIME);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true); // t

        SettingsClient client = LocationServices.getSettingsClient(PMActivity.this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(PMActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                ubicacionInicial();
            }
        });

        task.addOnFailureListener(PMActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(PMActivity.this,
                                100);
                    } catch (IntentSender.SendIntentException sendEx) {
                        Log.d("aca","error  " + sendEx.getLocalizedMessage());
                        // Ignore the error.
                    }
                }
            }
        });

    }


    private void ubicacionInicial() {


        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        LocationManager mListener = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (mListener != null) {


            isGPSLocation = mListener.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkLocation = mListener.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.e("gps, network", String.valueOf(isGPSLocation + "," + isNetworkLocation));
        }

        if (isGPSLocation || isNetworkLocation) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(PMActivity.this);

            SettingsClient mSettingsClient = LocationServices.getSettingsClient(PMActivity.this);


            mLocationCallback = new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult result) {
                    super.onLocationResult(result);
                    mCurrentLocation = result.getLocations().get(0);

                    if (mCurrentLocation != null) {
                        final Geocoder geocoder = new Geocoder(PMActivity.this);
                        try {

                            final Task location = mFusedLocationProviderClient.getLastLocation();
                            location.addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {

                                        Location currentLocation = (Location) task.getResult();

                                        if (currentLocation != null) {
                                            // mMap.clear();
                                            LatLng inicio = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                                            (PMActivity.this).latLngList.add(inicio);
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inicio, 15));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inicio,15));

                                            posActual = new Location("");
                                            posActual.setLatitude(inicio.latitude);
                                            posActual.setLongitude(inicio.longitude);
                                            byte[] myvar = "1".getBytes();
                                            getBluetoothService().write(myvar);
                                           // ubicInicial = inicio;

                                          //  mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicInicial, 15));

                                        }
                                    } else {
                                        try {
                                            Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        ubicacionInicial();
                                        Toast.makeText(PMActivity.this, "No se puede obtener la ubicacion", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (SecurityException e) {
                            Log.e("aca", "getDeviceLocation: SecurityException: " + e.getMessage());
                        }
                    }
                    mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                }
            };



            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);


            LocationSettingsRequest mLocationSettingsRequest = builder.build();

            final Task<LocationSettingsResponse> locationResponse = mSettingsClient.checkLocationSettings(mLocationSettingsRequest);

            locationResponse.addOnSuccessListener(PMActivity.this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.e("Response", "Successful acquisition of location information!!" + locationResponse.getException());


                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
            });


            locationResponse.addOnFailureListener(PMActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.e("onFailure", "Location environment check");
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Check location setting";
                            Log.e("onFailure", errorMessage);
                    }
                }
            });


        } else {
            verificarGPSON();
        }


    }







    //methods google api
    // Google Api Client is connected
    @Override
    public void onConnected(Bundle bundle) {
        if (mGoogleApiClient.isConnected()) {
            //if connected successfully show user the settings dialog to enable location from settings services
            // If location services are enabled then get Location directly
            // Else show options for enable or disable location services
            settingsrequest();
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case 3310: {
                if (grantResults.length > 0) {
                    for (int i = 0, len = permissions.length; i < len; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            // Show the user a dialog why you need location
                        } else if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                            // get Location
                        } else {
                            this.finish();
                        }
                    }
                }
                return;
            }
        }
    }


    public void settingsrequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        if (mGoogleApiClient.isConnected()) {

                            // check if the device has OS Marshmellow or greater than
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                                if (ActivityCompat.checkSelfPermission(PMActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(PMActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(PMActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
                                } else {
                                    // get Location
                                }
                            } else {
                                // get Location
                            }

                        }
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(PMActivity.this, REQUEST_RESOLVE_ERROR);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == REQUEST_RESOLVE_ERROR) {
            mResolvingError = false;
            switch (resultCode) {
                case Activity.RESULT_OK:
                    // get location method
                    break;
                case Activity.RESULT_CANCELED:
                    this.finish();
                    break;
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GoogleApiAvailability.getErrorDialog()
            showErrorDialog(result.getErrorCode());
            mResolvingError = true;
        }
    }


    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(
                    this.getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((PMActivity) getActivity()).onDialogDismissed();
        }
    }


    // Connect Google Api Client if it is not connected already
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    // Stop the service when we are leaving this activity
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            //mGoogleApiClient.disconnect();
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("aca","conectado");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        reconnect();
                }
            }
        }
    };


    //BLUETOOTH CONEXION

    private void reconnect() {
        bluetoothService.stop();
        bluetoothService.connect();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}
