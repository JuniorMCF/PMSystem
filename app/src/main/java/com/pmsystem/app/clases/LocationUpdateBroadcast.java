package com.pmsystem.app.clases;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.LocationResult;
import com.pmsystem.app.PMActivity;
import com.pmsystem.app.R;

public class LocationUpdateBroadcast extends BroadcastReceiver {
    public LocationUpdateBroadcast() {
    }
    public static final String ACTION_PROCESS_UPDATE = "com.pmsystem.app.UPDATE_LOCATION";


    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null){
            String action = intent.getAction();
            if(ACTION_PROCESS_UPDATE.equalsIgnoreCase(action)){
                LocationResult result = LocationResult.extractResult(intent);
                if(result != null){
                    Location location = result.getLastLocation();
                    try{

                        Intent intent2 = new Intent(context, PMActivity.class);
                        intent2.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent2.putExtra("reanudado",true);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);

                        int notificationId = 1;
                        String channelId = "channel-01";
                        String channelName = "Channel Name";
                        int importance = NotificationManager.IMPORTANCE_NONE;
                        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            NotificationChannel mChannel = new NotificationChannel(
                                    channelId, channelName, importance);
                            notificationManager.createNotificationChannel(mChannel);
                        }

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                                .setSmallIcon(R.drawable.logo_monitoreo)
                                .setContentTitle("PMASystem")
                                .setContentText("Registrando Activo")
                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                .setAutoCancel(false)
                                .setOngoing(true)
                                .setOnlyAlertOnce(true)
                                .setContentIntent(pendingIntent);
                        //.addAction(R.drawable.ic_launcher_background, "Pausar Proyecto",pendingIntent);

                        NotificationManagerCompat notificationManager2 = NotificationManagerCompat.from(context);
                        notificationManager2.notify(1, builder.build());

                       // MapsActivity.getInstance().setMinimizado();
                        Log.d("list","minimizado = " + location);
                        PMActivity.getInstance().updateData(location);

                    }catch (Exception e){
                        Log.e("broadcastReceiver","e");
                        Toast.makeText(context, "location = "  + location.getLongitude(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

}
