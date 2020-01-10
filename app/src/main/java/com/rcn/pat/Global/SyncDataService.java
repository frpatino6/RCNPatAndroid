package com.rcn.pat.Global;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.ViewModels.LocationViewModel;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class SyncDataService extends Service {

    public static final String TAG = SyncDataService.class.getSimpleName();
    public int counter = 0;
    Context context;
    long oldTime = 0;
    List<MyLocation> result;
    private LocationRepository locationRepository;
    private Timer timer;
    private TimerTask timerTask;

    public SyncDataService(Context applicationContext) {
        super();
        Log.i(TAG, "here I am!");
        context = applicationContext;
    }

    public SyncDataService() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void asyncListMaterialsByProduction() {


        String url = GlobalClass.getInstance().getUrlServices() + "SaveGPS";
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        String tipo = "application/json";

        StringEntity entity;
        Gson json = new Gson();

        ArrayList<LocationViewModel> locationViewModels = new ArrayList<>();

        for (MyLocation myLocation : result) {
            locationViewModels.add(new LocationViewModel(String.valueOf(myLocation.getLatitude()), String.valueOf(myLocation.getLongitude()), gettime(), GlobalClass.getInstance().getCurrentService().getId()));

        }
        String resultJson = json.toJson(locationViewModels);

        entity = new StringEntity(resultJson, StandardCharsets.UTF_8);

        client.post(context, url, entity, tipo, new TextHttpResponseHandler() {

            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {

            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onFinish() {


            }

            @SuppressLint("RestrictedApi")
            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                locationRepository.deleteAllLocation();
            }
        });
    }

    private String gettime() {
        SimpleDateFormat sdf = null;
        try {
            sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa", Locale.getDefault());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return sdf.format(new Date());
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            public void run() {
                Log.i("in timer", "in timer ++++  " + (counter++));
                locationRepository = new LocationRepository(getApplicationContext());
                result = locationRepository.getLocations();
                if (result != null && result.size() > 0)
                    asyncListMaterialsByProduction();


            }
        };
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 10000); //
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        // send new broadcast when service is destroyed.
        // this broadcast restarts the service.
        Intent broadcastIntent = new Intent("uk.ac.shef.oak.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }
}
