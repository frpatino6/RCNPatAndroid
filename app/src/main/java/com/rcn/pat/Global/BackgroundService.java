package com.rcn.pat.Global;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rcn.pat.R;
import com.rcn.pat.Repository.LocationRepository;
import com.rcn.pat.ViewModels.MyLocation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


@Deprecated
public class BackgroundService extends Service {
    private onLocationDisabled _onLocationDisabled;
    private static final String TAG = "BackgroundService";
    private static final String NOTIFICATION_CHANNEL_ID = "channel_01";
    private final LocationServiceBinder binder = new LocationServiceBinder();
    public static final String SERVICE_RESULT = "com.service.resultBackgroundLocationService";
    public static final String SERVICE_MESSAGE = "com.service.messageBackgroundLocationService";
    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 1;
    private Intent _intent;
    private LocalBroadcastManager localBroadcastManager;
    private LocationRepository locationRepository;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void sendResult(String message) {
        Intent intent = new Intent(SERVICE_RESULT);
        if (message != null)
            intent.putExtra(SERVICE_MESSAGE, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    private void sendError() {
        Intent intent = new Intent(SERVICE_RESULT);

        localBroadcastManager.sendBroadcast(intent);
    }

    public void startTracking() {
        initializeLocationManager();

        if (mLocationListener == null)
            mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER, new onLocationChange() {
                @Override
                public void onChange(Float speed) {
                    sendResult(String.valueOf(speed));
                }
            }, new onLocationDisabled() {
                @Override
                public void onDisabled() {
                    sendError();
                }
            });

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);

        } catch (java.lang.SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

    }

    public void stopTracking() {
        this.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @RequiresApi(api = VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = " com.rcn.pat";
        String channelName = "Pat Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("PAT Está en ejecución")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.i(TAG, "BackgroundService onCreate");
        locationRepository = new LocationRepository(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("PAT Está en ejecución")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }

        localBroadcastManager = LocalBroadcastManager.getInstance(this);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
                locationRepository = null;
                mLocationManager = null;
                mLocationListener = null;
                _intent = null;
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners, ignore", ex);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        _intent = intent;
        return START_STICKY;
    }

    private class LocationListener implements android.location.LocationListener {
        private final String TAG = "LocationListener";
        private onLocationChange _onLocationChange;
        private Location lastLocation = null;
        private Location mLastLocation;

        public LocationListener(String provider, onLocationChange onLocationChange, onLocationDisabled onLocationDisabled) {
            mLastLocation = new Location(provider);
            _onLocationChange = onLocationChange;
            _onLocationDisabled = onLocationDisabled;

        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.i(TAG, "LocationChanged: " + location);
            // Save to local DB
            if (locationRepository != null) {
                MyLocation loc = new MyLocation();
                loc.setLatitude(location.getLatitude());
                loc.setLongitude(location.getLongitude());
                loc.setTimeRead(gettime());
                locationRepository.insertLocation(loc);

                if (_onLocationChange != null)
                    _onLocationChange.onChange(location.getSpeed());
            }
//            Toast.makeText(BackgroundService.this, "LAT: " + location.getLatitude() + "\n LONG: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
            if (provider.equals("gps")) {
                if (_onLocationDisabled != null) {
                    _onLocationDisabled.onDisabled();
                }
            }

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

    private String gettime() {
        SimpleDateFormat sdf = null;
        try {
            sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sdf.format(new Date());
    }

    public class LocationServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
