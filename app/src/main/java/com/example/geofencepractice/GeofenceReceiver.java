package com.example.geofencepractice;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;

public class GeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        for(Geofence geofence : geofenceList) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId() + " of " + geofenceList.size() + " triggered geofences");
        }

        switch (geofenceTransition) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                //Turn on do not disturb settings
                Toast.makeText(context,"ENTER TRIGGERED", Toast.LENGTH_LONG).show();
                doNotDisturbSettings(context, NotificationManager.INTERRUPTION_FILTER_NONE);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context,"DWELL TRIGGERED", Toast.LENGTH_LONG).show();
                doNotDisturbSettings(context, NotificationManager.INTERRUPTION_FILTER_NONE);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                //Turn back to normal settings
                Toast.makeText(context,"EXIT TRIGGERED", Toast.LENGTH_LONG).show();
                doNotDisturbSettings(context, NotificationManager.INTERRUPTION_FILTER_ALL);
                break;
        }

//        Intent background = new Intent(context, BackgroundService.class);
//        context.startService(background);
    }

    private void doNotDisturbSettings(Context context, int interruptionFilter) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(mNotificationManager != null)
            if(mNotificationManager.isNotificationPolicyAccessGranted())
                mNotificationManager.setInterruptionFilter(interruptionFilter);
    }
}
