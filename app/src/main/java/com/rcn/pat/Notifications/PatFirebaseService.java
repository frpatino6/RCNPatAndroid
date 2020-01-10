package com.rcn.pat.Notifications;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rcn.pat.R;

import java.util.List;

public class PatFirebaseService extends FirebaseMessagingService {
    private static final String TAG = "PatFirebaseService";
    private static final int ID_NOTIFICACION_CREAR = 1;
    NotificationManager notificationManager;
    public PatFirebaseService() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotification(String remoteMessage, String title) {

        if(notificationManager == null )
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Intent nIntent = new Intent();
        final ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        final List<ActivityManager.RecentTaskInfo> recentTaskInfos = am.getRecentTasks(1024, 0);
        String myPkgNm = getPackageName();

        if (!recentTaskInfos.isEmpty()) {
            final List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(Integer.MAX_VALUE, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

            ActivityManager.RecentTaskInfo recentTaskInfo = null;

            for (int i = 0; i < recentTasks.size(); i++) {
                recentTaskInfo = recentTaskInfos.get(i);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (recentTaskInfo.baseIntent.getComponent().getPackageName().equals(myPkgNm)) {
                        nIntent = recentTaskInfo.baseIntent;
                        nIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                }
            }
        }
        createNotificationChannel();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nIntent, 0);
        Notification notiBuilder = new NotificationCompat.Builder(this, "channel_01")
                .setContentTitle(title)
                .setContentText(remoteMessage)
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).build();

        notiBuilder.flags |= Notification.FLAG_NO_CLEAR;

        notificationManager.notify(ID_NOTIFICACION_CREAR, notiBuilder);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "channel_01",
                    "My Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.icon)
                .setAutoCancel(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        //sendNotification(remoteMessage);
        createNotification(remoteMessage.getData().values().toArray()[1].toString(),remoteMessage.getData().values().toArray()[2].toString());
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        }

        // Check if message contains a notification payload.remoteMessage.getData().values().toArray()[1]
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

    }


}
