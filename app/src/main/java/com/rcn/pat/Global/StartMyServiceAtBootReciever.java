package com.rcn.pat.Global;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.rcn.pat.Activities.MainActivity;
import com.rcn.pat.Repository.ServiceRepository;

public class StartMyServiceAtBootReciever extends BroadcastReceiver {
    Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        c = context;
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (isCurrentServiceActive()) {
            context.startActivity(i);
            Toast toast = Toast.makeText(context, "Autostart...", Toast.LENGTH_LONG);
            Log.i("Autostart", "started");
        }
    }

    private boolean isCurrentServiceActive() {
        ServiceRepository serviceRepository = new ServiceRepository(c);
        if (serviceRepository.getStartetService() == null) {
            return false;
        }
        return true;
    }
}
