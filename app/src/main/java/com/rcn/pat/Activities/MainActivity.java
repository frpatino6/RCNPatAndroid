package com.rcn.pat.Activities;

import android.Manifest;
import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.BuildConfig;
import com.rcn.pat.Global.BackgroundLocationUpdateService;
import com.rcn.pat.Global.BackgroundService;
import com.rcn.pat.Global.CustomListViewDialog;
import com.rcn.pat.Global.DataAdapter;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.Global.NetworkStateReceiver;
import com.rcn.pat.Global.SyncDataService;
import com.rcn.pat.Notifications.PatFirebaseService;
import com.rcn.pat.R;
import com.rcn.pat.Repository.LocationRepository;
import com.rcn.pat.Repository.ServiceRepository;
import com.rcn.pat.ViewModels.LocationViewModel;
import com.rcn.pat.ViewModels.MyLocation;
import com.rcn.pat.ViewModels.PausaReasons;
import com.rcn.pat.ViewModels.ServiceInfo;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, NetworkStateReceiver.NetworkStateReceiverListener {

    private final String TAG = "MainActivity";
    private boolean blockService = false;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiverBackgroundService;
    private BroadcastReceiver broadcastReceiverFirebase;
    private TextView btnPause;
    private TextView btnStart;
    private TextView btnStop;
    private Context ctx;
    private ServiceInfo currentServiceInfo;
    private CustomListViewDialog customDialog;
    private ArrayList<PausaReasons> dataPausaReasons;
    private ProgressDialog dialogo;
    private int endTask = -1;
    private TextView fontTextView2;
    private boolean haveActiveService;
    private TextView lblDescription;
    private TextView lblInitTime;
    private TextView lblNombreSolicitante;
    private TextView lblObservations;
    private TextView lblPause;
    private TextView lblPhone;
    private TextView lblStart;
    private TextView lblStop;
    private LocationRepository locationRepository;
    private SyncDataService mSensorService;
    private Intent mServiceIntent;
    private AlarmManager manager;
    private NetworkStateReceiver networkStateReceiver;
    private PendingIntent pendingIntent;
    private List<MyLocation> result;
    private ServiceRepository serviceRepository;
    private Button startButton;
    private TextView statusTextView;
    private Button stopButton;
    private Typeface typeface;
    public BackgroundService gpsService;
    public Intent intent;
    public boolean mTracking = false;

    public void cancelAlarm() {
        if (manager != null) {
            manager.cancel(pendingIntent);
            //Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    public Context getCtx() {
        return ctx;
    }

    @Override
    public void networkAvailable() {

    }

    @Override
    public void networkUnavailable() {

    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                startTracking(true);
                break;
            case R.id.stopButton:
                stopTracking(false);
                break;
        }
    }

    public void setTitle(String title) {
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView textView = new TextView(this);
        textView.setText(title);
        textView.setTextSize(20);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textView.setTextColor(getResources().getColor(R.color.cardViewHeaderTextColor));
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(textView);
    }

    public void startAlarm() {
        manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        //Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
    }

    public void startTracking(final boolean isStarted) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @RequiresApi(api = VERSION_CODES.KITKAT)
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        ServiceInfo serviceInfo = serviceRepository.getStartetService();
                        if (isStarted) {
                            if (serviceInfo != null) {
                                serviceInfo.setPausedId(1);
                                serviceRepository.updateService(serviceInfo);
                            }
                            GlobalClass.getInstance().getCurrentService().setStarted(true);
                            GlobalClass.getInstance().getCurrentService().setPausedId(1);
                        }
                        if (GlobalClass.getInstance().getCurrentService().getIdService() > 0) {
                            GlobalClass.getInstance().getCurrentService().setFechaPausa("");

                            serviceRepository.updateService(GlobalClass.getInstance().getCurrentService());
                        } else
                            serviceRepository.insertService(GlobalClass.getInstance().getCurrentService());

                        sendLastLocation(serviceInfo, "");
                        if (GlobalClass.getInstance().isNetworkAvailable())
                            asyncLocations();
                        startForegroundServices(false);
                        toggleButtons();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (permissionAccessCoarseLocationApproved) {
            boolean backgroundLocationPermissionApproved =
                    ActivityCompat.checkSelfPermission(this,
                            permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            if (backgroundLocationPermissionApproved) {
                // App can access location both in the foreground and in the background.
                // Start your service that doesn't have a foreground service type
                // defined.
            } else {
                // App can only access location in the foreground. Display a dialog
                // warning the user that your app must have all-the-time access to
                // location in order to function properly. Then, request background
                // location.
                ActivityCompat.requestPermissions(this, new String[]{
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                        5111);
            }
        }


    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    public void stopTracking(boolean isAtomaticStoped) {
        String observations = "";
        mTracking = false;
        ServiceInfo currentServiceInfo = serviceRepository.getStartetService();
        currentServiceInfo.setStarted(false);
        currentServiceInfo.setPaused(false);
        currentServiceInfo.setStoped(true);
        currentServiceInfo.setPausedId(2);
        serviceRepository.updateService(currentServiceInfo);

        if (isAtomaticStoped)
            observations = "Servicio finalizado automáticamente";

        sendLastLocation(currentServiceInfo, observations);

        if (GlobalClass.getInstance().isNetworkAvailable())
            asyncLocations();

        serviceRepository.deleteAllService();
        stopBackgroundServices();
        finish();
    }

    private void asyncListPausaReasons() {

        try {

            dialogo = null;
            dialogo = new ProgressDialog(MainActivity.this);
            dialogo.setMessage("Cargando lista de posibles causas.");
            dialogo.setIndeterminate(false);
            dialogo.setCancelable(false);
            dialogo.show();


            String url = GlobalClass.getInstance().getUrlServices() + "lstPause";
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(60000);
            RequestParams params = new RequestParams();

            client.get(url, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            try {

                                int a = 0;
                                TypeToken<List<PausaReasons>> token = new TypeToken<List<PausaReasons>>() {
                                };
                                Gson gson = new GsonBuilder().create();
                                // Define Response class to correspond to the JSON response returned
                                dataPausaReasons = gson.fromJson(res, token.getType());

                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            setPauseReasonsDialog();
                            dialogo.dismiss();
                            dialogo.hide();
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void asyncLocations() {

        result = locationRepository.getLocations();
        locationRepository.deleteAllLocation();
        String url = GlobalClass.getInstance().getUrlServices() + "SaveGPS";
        AsyncHttpClient client = new AsyncHttpClient();
        client.setTimeout(60000);
        String tipo = "application/json";

        StringEntity entity;
        Gson json = new Gson();

        ArrayList<LocationViewModel> locationViewModels = new ArrayList<>();
        if (result != null)
            for (MyLocation myLocation : result) {
                locationViewModels.add(
                        new LocationViewModel(
                                String.valueOf(myLocation.getLatitude())
                                , String.valueOf(myLocation.getLongitude())
                                , myLocation.getTimeRead()
                                , GlobalClass.getInstance().getCurrentService().getId()
                                , myLocation.getPausedId()
                                , myLocation.getObservaciones()
                        ));
            }
        result.clear();
        String resultJson = json.toJson(locationViewModels);
        entity = new StringEntity(resultJson, StandardCharsets.UTF_8);
        client.post(MainActivity.this, url, entity, tipo, new TextHttpResponseHandler() {

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

            }
        });
    }

    private void asyncServiceInfoById() {
        try {
            final ProgressDialog dialogo = new ProgressDialog(MainActivity.this);
            dialogo.setMessage("Actualizando datos del servicio...");
            dialogo.setIndeterminate(false);
            dialogo.setCancelable(false);
            dialogo.show();

            String url = GlobalClass.getInstance().getUrlServices() + "ScheduleByDriver/" + GlobalClass.getInstance().getCurrentService().getId().toString();
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(60000);
            RequestParams params = new RequestParams();

            client.get(url, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {


                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            try {

                                int a = 0;
                                TypeToken<ServiceInfo> token = new TypeToken<ServiceInfo>() {
                                };
                                Gson gson = new GsonBuilder().create();
                                // Define Response class to correspond to the JSON response returned
                                ServiceInfo data = gson.fromJson(res, token.getType());
                                GlobalClass.getInstance().setCurrentService(data);
                                serviceRepository.updateService(data);
                                initializaValues();


                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();

                            }
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            dialogo.hide();
                            dialogo.dismiss();

                        }
                    }
            );
        } catch (Exception ex) {

        }
    }

    private void confirmCausePauseDialog() {

        if (dataPausaReasons == null)
            if (GlobalClass.getInstance().isNetworkAvailable())
                asyncListPausaReasons();
            else
                showConfirmDialog("No se puede cargar en este momentos los motivos de la pausa.");
        else {
            setPauseReasonsDialog();
        }

    }

    private void confirmStartService() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

        if (GlobalClass.getInstance().getCurrentService().isPaused())
            alert.setMessage("¿Seguro de Reiniciar el servicio?");
        else
            alert.setMessage("¿Seguro de iniciar el servicio?");
        alert.setTitle(getString(R.string.app_name));
        //dlgAlert.setPositiveButton(getString(R.string.Texto_Boton_Ok), null);
        alert.setPositiveButton(R.string.Texto_Boton_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                startTracking(true);

            }
        });
        alert.setNegativeButton(R.string.Texto_Boton_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setCancelable(true);
        alert.create().show();
    }

    private void confirmStopService() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);
        dlgAlert.setCancelable(false);
        dlgAlert.setMessage("¿Seguro de detener el servicio?");
        dlgAlert.setTitle(getString(R.string.app_name));
        //dlgAlert.setPositiveButton(getString(R.string.Texto_Boton_Ok), null);
        dlgAlert.setPositiveButton(R.string.Texto_Boton_Ok, new DialogInterface.OnClickListener() {
            @RequiresApi(api = VERSION_CODES.KITKAT)
            public void onClick(DialogInterface dialog, int id) {
                GlobalClass.getInstance().getCurrentService().setPaused(false);
                GlobalClass.getInstance().getCurrentService().setStarted(false);
                GlobalClass.getInstance().getCurrentService().setStoped(true);
                stopTracking(false);

            }
        });
        dlgAlert.setNegativeButton(R.string.Texto_Boton_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dlgAlert.create().show();
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

    private void inactiveAllButtons() {
        btnPause.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        lblStop.setVisibility(btnStop.getVisibility());
        lblPause.setVisibility(btnPause.getVisibility());
        lblStart.setVisibility(btnStart.getVisibility());
        blockService = true;
    }

    private void initializaControls() {
        lblNombreSolicitante = findViewById(R.id.lblNombreSolicitante);
        lblPhone = findViewById(R.id.lblPhone);
        lblInitTime = findViewById(R.id.lblInitTime);
        lblDescription = findViewById(R.id.lblDescription);
        lblObservations = findViewById(R.id.lblObservations);
        typeface = Typeface.createFromAsset(getAssets(), "fa-solid-900.ttf");
        btnPause = (TextView) findViewById(R.id.btnPause);
        btnStart = (TextView) findViewById(R.id.btnStart);
        btnStop = (TextView) findViewById(R.id.btnStop);
        lblPause = (TextView) findViewById(R.id.lblPause);
        lblStart = (TextView) findViewById(R.id.lblStart);
        lblStop = (TextView) findViewById(R.id.lblStop);

        fontTextView2 = (TextView) findViewById(R.id.fontTextView2);

        btnPause.setTypeface(typeface);
        btnStart.setTypeface(typeface);
        btnStop.setTypeface(typeface);
        fontTextView2.setTypeface(typeface);
        serviceRepository = new ServiceRepository(getApplicationContext());
        locationRepository = new LocationRepository(getApplicationContext());

    }

    private void initializaData() {
        int id = GlobalClass.getInstance().getCurrentService().getId();
        // busca si tiene en la base de datos local un servicio activo o pausado
        ServiceInfo serviceInfo = serviceRepository.getStartetService();
        if (serviceInfo == null) {
            toggleButtons();
            return;
        }
        //Si existe, compara con el que acaba de seleccionar
        if (serviceInfo.getId().equals(id)) {
            GlobalClass.getInstance().setCurrentService(serviceInfo);
            toggleButtons();
        } else {
            inactiveAllButtons();
            return;
        }
        //Valida si el servicio actual está iniciado o pausado para iniciar el servicio en bash
        if (serviceInfo != null && (serviceInfo.isStarted() || serviceInfo.isPaused())) {
            startTracking(serviceInfo.isStarted());
        }
    }

    private void initializaEvents() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmStartService();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmStopService();
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmCausePauseDialog();
            }
        });
    }

    private void initializaValues() {
        lblNombreSolicitante.setText(GlobalClass.getInstance().getCurrentService().getNombreUsuarioSolicitante());
        lblPhone.setText(GlobalClass.getInstance().getCurrentService().getCelularSolicitante());
        lblInitTime.setText(GlobalClass.getInstance().getDateFormat(GlobalClass.getInstance().getCurrentService().getFechaInicial()) + " a " + GlobalClass.getInstance().getDateFormat(GlobalClass.getInstance().getCurrentService().getFechaFinal()));
        lblDescription.setText(GlobalClass.getInstance().getCurrentService().getDescripcionRecorrido());
        lblObservations.setText(GlobalClass.getInstance().getCurrentService().getObservaciones());
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("key")) {
            //do your Stuff
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            setTitle("Servicio " + GlobalClass.getInstance().getCurrentService().getSolicitudNombre());
            setWidgetIds();
            initializaControls();
            initializaValues();
            initializaEvents();
            ctx = this;


            networkStateReceiver = new NetworkStateReceiver();
            networkStateReceiver.addListener(this);
            this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {


                }
            };
            broadcastReceiverBackgroundService = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if(currentServiceInfo == null) currentServiceInfo = serviceRepository.getStartetService();
                    //Valida si la hora actual sobrepasa la hora estimada de finalización del servicio, si es así, envia una notificación
                    // Preguntando al usuario, si desea finalizar el servicio, ya que la hora ha sido superada

                    //Pregunta si el servicio está inactivo, si esta inactivo es porque se encuentra visualizando un servicio que no es el que está activo
                    if (blockService)
                        return;
                    else {
                        try {
                            String speed = intent.getStringExtra(BackgroundLocationUpdateService.SERVICE_MESSAGE);
                            if (speed.equals("EndTask")) {
                                stopTracking(true);
                                return;
                            }
                            if (speed.equals("0")) {
                                validateIfEndedService(currentServiceInfo);
                            }
                            //Si la velocidad es superior a 4 kms/h inicia automaticamente el servicio.
                            if (!speed.equals("")) {
                                if (currentServiceInfo != null)
                                    if (Float.valueOf(speed) > 4 && currentServiceInfo.isPaused()) {
                                        currentServiceInfo.setStarted(true);
                                        currentServiceInfo.setStoped(false);
                                        currentServiceInfo.setPaused(false);
                                        currentServiceInfo.setFechaPausa("");
                                        serviceRepository.updateService(currentServiceInfo);
                                        startTracking(true);
                                        sendNotificationEndService("Se ha detectado actividad y el servicio se reanudará automáticamente", false);
                                    } else {
                                        if (Float.valueOf(speed) < 4 && currentServiceInfo.isStarted()) {
                                            if (currentServiceInfo != null)
                                                validateIfAutoPauseTrace(currentServiceInfo);
                                        }
                                        if (Float.valueOf(speed) > 4 && currentServiceInfo.isStarted()) {
                                            currentServiceInfo.setFechaPausa("");
                                            serviceRepository.updateService(currentServiceInfo);
                                        }
                                    }
                                validateIfServiceReadyToEnd(currentServiceInfo);
                                toggleButtons();
                            } else {
                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                            }
                            //Valida si el servicio ya terminó y aún está activo
                            //SI: Notifica al usuario preguntando si desea seguir con el servicio activo

                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

            broadcastReceiverFirebase = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String s = intent.getStringExtra(PatFirebaseService.SERVICE_MESSAGE);
                    //Actualiza la informaci[on del servicio
                    asyncServiceInfoById();
                }
            };

            Intent intent = getIntent();
            Bundle answerBundle = intent.getExtras();
            if (intent.hasExtra("EndTask")) {
                String ns = Context.NOTIFICATION_SERVICE;
                endTask = answerBundle.getInt("EndTask");
                NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
                nMgr.cancelAll();
            } else
                endTask = -1;

            if (endTask == 1) {
                stopTracking(true);

            }

            initializaData();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverBackgroundService),
                new IntentFilter(BackgroundLocationUpdateService.SERVICE_RESULT));

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverFirebase),
                new IntentFilter(PatFirebaseService.SERVICE_RESULT));
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void pauseService(SimpleDateFormat sdf, int idPause, String obs) {
        GlobalClass.getInstance().getCurrentService().setPaused(true);
        GlobalClass.getInstance().getCurrentService().setStarted(false);
        GlobalClass.getInstance().getCurrentService().setStoped(false);
        GlobalClass.getInstance().getCurrentService().setFechaPausa(sdf.format(new Date()));
        GlobalClass.getInstance().getCurrentService().setPausedId(idPause);
        ServiceInfo serviceInfo = serviceRepository.getStartetService();

        if (serviceInfo == null)
            return;

        serviceInfo.setPaused(true);
        serviceInfo.setStarted(false);
        serviceInfo.setStoped(false);
        serviceInfo.setFechaPausa(sdf.format(new Date()));
        serviceInfo.setPausedId(idPause);
        serviceRepository.updateService(serviceInfo);
        sendLastLocation(serviceInfo, obs);
        if (GlobalClass.getInstance().isNetworkAvailable())
            asyncLocations();

        toggleButtons();

    }

    private void sendLastLocation(ServiceInfo serviceInfo, String obs) {
        try {

            if (serviceInfo != null && serviceInfo.getLastLatitude() != null) {
                MyLocation MyLocation = new MyLocation(Double.valueOf(serviceInfo.getLastLatitude()), Double.valueOf(serviceInfo.getLastLongitude()), serviceInfo.getId());
                MyLocation.setTimeRead(gettime());
                MyLocation.setObservaciones(obs);
                MyLocation.setPausedId(serviceInfo.getPausedId());
                locationRepository.insertLocation(MyLocation);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }

    private void sendNotificationEndService(String contentText, boolean showButtons) {

        Intent intent = new Intent(this, BackgroundLocationUpdateService.class);
        Bundle endBundle = new Bundle();
        endBundle.putInt("EndTask", 1);//This is the value I want to pass
        intent.putExtras(endBundle);
        PendingIntent pendingIntentEnd =
                PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intentContinue = new Intent(this, MainActivity.class);
        Bundle continueBundle = new Bundle();
        continueBundle.putInt("EndTask", 0);//This is the value I want to pass
        intentContinue.putExtras(continueBundle);
        PendingIntent pendingIntentContinue =
                PendingIntent.getActivity(this, 1, intentContinue, PendingIntent.FLAG_ONE_SHOT);


        String CHANNEL_ID = "channel_location";
        String CHANNEL_NAME = "channel_location";

        NotificationCompat.Builder builder;
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            Objects.requireNonNull(notificationManager).createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
            builder.setChannelId(CHANNEL_ID);
            builder.setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
        } else {
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
        }

        builder.setContentText(contentText);
        builder.setContentTitle(getString(R.string.app_name));
        Uri notificationSound = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(notificationSound);
        builder.setAutoCancel(true);
        builder.setSmallIcon(R.drawable.icon);

        if (showButtons) {
            builder.addAction(R.drawable.icon, "Finalizar", pendingIntentEnd);
            builder.addAction(R.drawable.icon, "Continuar", pendingIntentContinue);
        }
        Notification notification = builder.build();
        notificationManager.notify(0, builder.getNotification());
    }

    private void setPauseReasonsDialog() {

        DataAdapter dataAdapter = new DataAdapter(dataPausaReasons, new DataAdapter.RecyclerViewItemClickListener() {
            @RequiresApi(api = VERSION_CODES.KITKAT)
            @Override
            public void clickOnItem(PausaReasons data) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                pauseService(sdf, data.getId(), "");
                customDialog.dismiss();
                dialogo.dismiss();
                dialogo.hide();
            }
        });
        customDialog = new CustomListViewDialog(MainActivity.this, dataAdapter);
        customDialog.show();
        customDialog.setCanceledOnTouchOutside(false);

    }

    private void setWidgetIds() {
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        statusTextView = (TextView) findViewById(R.id.statusTextView);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    private void showConfirmDialog(String contentMessage) {
        // Use the Builder class for convenient dialog construction
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Confirmación");
            alert.setCancelable(false);
            alert.setMessage(contentMessage);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            final AlertDialog alertDialog = alert.create();
            alertDialog.show();
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                }
            };
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    handler.removeCallbacks(runnable);
                }
            });
            handler.postDelayed(runnable,10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showConfirmStopEndedService(final ServiceInfo serviceInfo, String contentMessage) {
        // Use the Builder class for convenient dialog construction
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Confirmación");
            alert.setCancelable(false);
            alert.setMessage(contentMessage);
            alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    serviceInfo.setMinutesAfter(serviceInfo.getMinutesAfter() + 2);
                    serviceRepository.updateService(serviceInfo);
                    dialog.dismiss();
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @RequiresApi(api = VERSION_CODES.KITKAT)
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    serviceRepository.updateService(serviceInfo);
                    stopTracking(true);
                    dialog.dismiss();
                }
            });
            final AlertDialog alertDialog = alert.create();
            alertDialog.show();
            final Handler handler = new Handler();
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    alertDialog.dismiss();
                }
            };
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    handler.removeCallbacks(runnable);
                }
            });
            handler.postDelayed(runnable,10000);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void startBackgroundServices() {
        toggleButtons();
    }

    private void toggleButtons() {

        if (haveActiveService) {
            btnPause.setVisibility(haveActiveService == true ? View.GONE : View.VISIBLE);
            btnStart.setVisibility(haveActiveService == true ? View.GONE : View.VISIBLE);
            btnStop.setVisibility(haveActiveService == true ? View.GONE : View.VISIBLE);
        } else {

            if (GlobalClass.getInstance().getCurrentService().isStarted()) {
                btnPause.setVisibility(View.VISIBLE);
                btnStart.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.VISIBLE);
            }
            if (GlobalClass.getInstance().getCurrentService().isPaused()) {
                btnPause.setVisibility(View.INVISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.VISIBLE);
                lblStart.setText("Reaundar");
                btnStart.setTextColor(Color.parseColor("#23c6c8"));
                lblStart.setTextColor(Color.parseColor("#23c6c8"));
            }
            if (GlobalClass.getInstance().getCurrentService().isStoped()) {
                btnPause.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnStart.setVisibility(View.VISIBLE);
                lblStart.setText("Iniciar");
                lblStart.setTextColor(Color.parseColor("#1ab394"));
                btnStart.setTextColor(Color.parseColor("#1ab394"));
            }
        }
        lblStop.setVisibility(btnStop.getVisibility());
        lblPause.setVisibility(btnPause.getVisibility());
        lblStart.setVisibility(btnStart.getVisibility());

    }

    private void startForegroundServices(boolean paused) {

        if (!isMyServiceRunning(BackgroundLocationUpdateService.class)) {
            intent = new Intent(MainActivity.this, BackgroundLocationUpdateService.class);
            intent.putExtra("SendTrace", paused ? "1" : "0");
            startService(intent);
        }
    }

    private void stopBackgroundServices() {
        try {

            LocalBroadcastManager.getInstance(this.getApplication()).unregisterReceiver(broadcastReceiver);
            LocalBroadcastManager.getInstance(this.getApplication()).unregisterReceiver(broadcastReceiverBackgroundService);

            stopForegroundServices();
            cancelAlarm();
            toggleButtons();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopForegroundServices() {

        stopService(new Intent(this, BackgroundLocationUpdateService.class));

    }

    //Valida si se debe iniciar automáticamente la traza
    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void validateIfAutoPauseTrace(ServiceInfo serviceInfo) {

        String srPauseDate = serviceInfo.getFechaPausa();
        Date currentDate = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            if (serviceInfo != null) {
                if (serviceInfo.getFechaPausa().equals("")) {
                    serviceInfo.setFechaPausa(sdf.format(new Date()));
                    serviceRepository.updateService(serviceInfo);
                }

                if(!srPauseDate.equals("")) {
                    Date dtPauseDate = sdf.parse(srPauseDate);
                    long diffInMillies = Math.abs(Objects.requireNonNull(dtPauseDate).getTime() - currentDate.getTime());
                    long diff = diffInMillies / (60 * 1000);

                    if (diff >= 1) {
                        pauseService(sdf, 3, "Servicio pausado automáticamente");
                        sendNotificationEndService("No se ha detectado actividad y el servicio ha sido pausado automáticamente", false);
                        showConfirmDialog("No se ha detectado actividad y el servicio ha sido pausado automáticamente");
                    }
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void validateIfEndedService(ServiceInfo serviceInfo) {

        try {
            Date currentTime = GlobalClass.getInstance().getCurrentTime();
            currentServiceInfo = serviceInfo;
            if (currentServiceInfo != null) {

                String srNextDateNotification = currentServiceInfo.getFechaUltimaNotification();
                Date endServiceTime = null;
                try {
                    //if (serviceInfo.getFechaUltimaNotification() != null)
                    endServiceTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(srNextDateNotification);//Genera varaible de tipo date con la fecha acutal y hora actual


                    if (endServiceTime.before(currentTime)) {
                        Intent closeIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        MainActivity.this.sendBroadcast(closeIntent);
                        sendNotificationEndService("El servicio a superado la hora límite. ¿Desea continuar?", true);
                        showConfirmStopEndedService(serviceInfo, "El servicio a superado la hora límite. ¿Desea continuar?");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        Date d = new Date();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(d);
                        cal.add(Calendar.MINUTE, serviceInfo.getMinutesAfter());
                        String newTime = sdf.format(cal.getTime());
                        String nextDateNotifications = newTime;
                        serviceInfo.setFechaUltimaNotification(nextDateNotifications);
                        serviceRepository.updateService(serviceInfo);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void validateIfServiceReadyToEnd(ServiceInfo serviceInfo) {
        try {
            if (serviceInfo != null) {
                Date dateEnd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(serviceInfo.getFechaFinal());
                long secondsInMilli = 1000;
                long minutesInMilli = secondsInMilli * 60;
                long different = dateEnd.getTime() - GlobalClass.getInstance().getCurrentTime().getTime();
                long elapsedMinutes = different / minutesInMilli;
                if (elapsedMinutes == 60 && !serviceInfo.isIshourNotify()) {
                    serviceInfo.setIshourNotify(true);
                    showConfirmDialog("El servicio finalizará en una hora");
                    sendNotificationEndService("El servicio finalizará en una hora", false);
                    serviceRepository.updateService(serviceInfo);
                }
                if (elapsedMinutes == 30 && !serviceInfo.isIshalfhourNotify()) {
                    serviceInfo.setIshalfhourNotify(true);
                    showConfirmDialog("El servicio finalizará en  media hora");
                    sendNotificationEndService("El servicio finalizará en media hora", false);
                    serviceRepository.updateService(serviceInfo);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
