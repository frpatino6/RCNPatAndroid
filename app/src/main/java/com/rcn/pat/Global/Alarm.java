package com.rcn.pat.Global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class Alarm extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.i(Alarm.class.getSimpleName(), "Broadcast Received");
            Log.i(Alarm.class.getSimpleName(), "Restarting Service");
            context.startService(new Intent(context, SyncDataService.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
