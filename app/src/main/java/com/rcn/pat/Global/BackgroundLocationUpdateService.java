package com.rcn.pat.Global;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.Activities.MainActivity;
import com.rcn.pat.R;
import com.rcn.pat.ViewModels.LocationViewModel;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by Ketan Ramani on 05/11/18.
 */

public class BackgroundLocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public int counter = 0;
    private final String TAG = "BackgroundLocationUpdateService";
    private final String TAG_LOCATION = "TAG_LOCATION";
    private Context context;
    private String latitude = "0.0", longitude = "0.0";
    /* Declare in manifest
    <service android:name=".BackgroundLocationUpdateService"/>
    */
    private LocalBroadcastManager localBroadcastManager;
    private LocationRepository locationRepository;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private SettingsClient mSettingsClient;
    private List<MyLocation> result;
    private boolean stopService = false;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        locationRepository = new LocationRepository(getApplicationContext());
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        StartForeground();
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {

            @RequiresApi(api = VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (!stopService) {
                        handler.postDelayed(this, TimeUnit.SECONDS.toMillis(10));
                    }
                }
            }
        };
        handler.postDelayed(runnable, 2000);

        buildGoogleApiClient();
        startTimer();
        return START_STICKY;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onDestroy() {
        Log.e(TAG, "Service Stopped");
        stopService = true;
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Log.e(TAG_LOCATION, "Location Update Callback Removed");
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /* For Google Fused API */

    private void StartForeground() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

        String CHANNEL_ID = "channel_location";
        String CHANNEL_NAME = "channel_location";

        NotificationCompat.Builder builder = null;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            builder.setChannelId(CHANNEL_ID);
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }

        builder.setContentText("PAT Está en ejecución");
        builder.setContentTitle(getString(R.string.app_name));
        Uri notificationSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notificationSound);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(101, notification);
    }

    protected synchronized void buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        connectGoogleClient();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // Log.e(TAG_LOCATION, "Location Received");
                mCurrentLocation = locationResult.getLastLocation();
                onLocationChanged(mCurrentLocation);
            }
        };
    }

    private void connectGoogleClient() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(context);
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Log.e(TAG_LOCATION, "Location Changed Latitude : " + location.getLatitude() + " Longitude : " + location.getLongitude());

        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

        if (latitude.equalsIgnoreCase("0.0") && longitude.equalsIgnoreCase("0.0")) {
            requestLocationUpdate();
        } else {
            if (locationRepository != null) {
                MyLocation loc = new MyLocation();
                loc.setLatitude(location.getLatitude());
                loc.setLongitude(location.getLongitude());
                loc.setTimeRead(gettime());
                locationRepository.insertLocation(loc);

                Intent intent = new Intent(SERVICE_RESULT);
                intent.putExtra(SERVICE_MESSAGE, String.valueOf(location.getSpeed()));
                localBroadcastManager.sendBroadcast(intent);
            }
            Log.e(TAG_LOCATION, "Latitude : " + location.getLatitude() + " Longitude : " + location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdate() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
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

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, GlobalClass.getInstance().getMinSendLocationToDatabase() * 1000); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                locationRepository = new LocationRepository(getApplicationContext());
                try {
                    result = locationRepository.getLocations();

                    if (GlobalClass.getInstance().isNetworkAvailable())
                        if (result != null && result.size() > 0)
                            asyncLocations();
                    if (result != null && result.size() == 0) {
                        Intent intent = new Intent(SERVICE_RESULT);
                        intent.putExtra(SERVICE_MESSAGE, "0");
                        localBroadcastManager.sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10 * 1000);
        mLocationRequest.setSmallestDisplacement(2);
        mLocationRequest.setFastestInterval(2 * 1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();

        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.e(TAG_LOCATION, "GPS Success");
                        requestLocationUpdate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                        sendResult("");

                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.e(TAG_LOCATION, "Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                }
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                Log.e(TAG_LOCATION, "checkLocationSettings -> onCanceled");
            }
        });
    }

    private void sendResult(String message) {
        Intent intent = new Intent(SERVICE_RESULT);
        if (message != null)
            intent.putExtra(SERVICE_MESSAGE, message);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectGoogleClient();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void asyncLocations() {
        Log.i("Enviando posiciones", "in timer ++++  ");

        String url = GlobalClass.getInstance().getUrlServices() + "SaveGPS";
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        String tipo = "application/json";

        StringEntity entity;
        Gson json = new Gson();

        ArrayList<LocationViewModel> locationViewModels = new ArrayList<>();

        for (MyLocation myLocation : result) {
            locationViewModels.add(new LocationViewModel(String.valueOf(myLocation.getLatitude()), String.valueOf(myLocation.getLongitude()), myLocation.getTimeRead(), GlobalClass.getInstance().getCurrentService().getId()));

        }
        String resultJson = json.toJson(locationViewModels);

        entity = new StringEntity(resultJson, StandardCharsets.UTF_8);
        client.post(context, url, entity, tipo, new TextHttpResponseHandler() {

            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {


            }

            @SuppressLint("LongLogTag")
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                Log.i(TAG, "ERROR Locations sended: " + responseBody + "  " + error.getMessage());
            }

            @SuppressLint({"RestrictedApi", "LongLogTag"})
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, "Sended locations " + result.size());
                locationRepository.deleteAllLocation();
            }
        });
    }

    public static final String SERVICE_RESULT = "com.service.resultBackgroundLocationService";
    public static final String SERVICE_MESSAGE = "com.service.messageBackgroundLocationService";
    /* For Google Fused API */
    protected GoogleApiClient mGoogleApiClient;
    protected LocationSettingsRequest mLocationSettingsRequest;

    class asynGeoLocation extends AsyncTask {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Object doInBackground(Object[] objects) {

            locationRepository = new LocationRepository(getApplicationContext());
            result = locationRepository.getLocations();
            return true;
        }

        @Override
        protected void onPreExecute() {


        }

        @RequiresApi(api = VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Object o) {

            if (result.size() > 0)
                asyncLocations();

            super.onPostExecute(o);
        }
    }

}