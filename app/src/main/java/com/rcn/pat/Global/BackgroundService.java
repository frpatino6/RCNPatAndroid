package com.rcn.pat.Global;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.rcn.pat.Activities.MainActivity;
import com.rcn.pat.R;

public class BackgroundService extends Service {
    private static final String TAG = "BackgroundService";
    private static final String NOTIFICATION_CHANNEL_ID = "channel_01";
    private final LocationServiceBinder binder = new LocationServiceBinder();
    public static final String SERVICE_RESULT = "com.service.resultBackgroundLocationService";
    public static final String SERVICE_MESSAGE = "com.service.messageBackgroundLocationService";
    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 10;
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

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER, new onLocationChange() {
            @Override
            public void onChange(Float speed) {
                sendResult(String.valueOf(speed));
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        Log.i(TAG, "BackgroundService onCreate");
        locationRepository = new LocationRepository(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification.Builder builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.app_name))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setChannelId(NOTIFICATION_CHANNEL_ID)
                    .setContentText("PAT Está en ejecución")
                    .setOnlyAlertOnce(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(2, notification);

        } else {

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("PAT Está en ejecución")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(1, notification);
        }
        stopSelf();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        Log.i(TAG, "BackgroundService onDestroy");
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
                locationRepository = null;
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners, ignore", ex);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        _intent = intent;
        return START_NOT_STICKY;
    }

    private class LocationListener implements android.location.LocationListener {
        private final String TAG = "LocationListener";
        private onLocationChange _onLocationChange;
        private Location lastLocation = null;
        private Location mLastLocation;

        public LocationListener(String provider, onLocationChange onLocationChange) {
            mLastLocation = new Location(provider);
            _onLocationChange = onLocationChange;
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
                locationRepository.insertLocation(loc);

                if (_onLocationChange != null)
                    _onLocationChange.onChange(location.getSpeed());
            }
//            Toast.makeText(BackgroundService.this, "LAT: " + location.getLatitude() + "\n LONG: " + location.getLongitude(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
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

    public class LocationServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }
}
