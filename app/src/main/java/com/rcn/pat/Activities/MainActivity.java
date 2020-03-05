package com.rcn.pat.Activities;

import android.Manifest;
import android.Manifest.permission;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.rcn.pat.Global.AlarmReceiver;
import com.rcn.pat.Global.BackgroundLocationUpdateService;
import com.rcn.pat.Global.BackgroundService;
import com.rcn.pat.Global.CustomListViewDialog;
import com.rcn.pat.Global.DataAdapter;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.Global.ServiceRepository;
import com.rcn.pat.Global.SyncDataService;
import com.rcn.pat.Notifications.PatFirebaseService;
import com.rcn.pat.R;
import com.rcn.pat.ViewModels.PausaReasons;
import com.rcn.pat.ViewModels.ServiceInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Intent intent;
    public BackgroundService gpsService;
    public boolean mTracking = false;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiverBackgroundService;
    private BroadcastReceiver broadcastReceiverFirebase;
    private TextView btnPause;
    private TextView lblPause;
    private TextView btnStart;
    private TextView lblStart;
    private TextView btnStop;
    private TextView lblStop;
    private Context ctx;
    private ServiceInfo currentServiceInfo;
    private CustomListViewDialog customDialog;
    private ArrayList<PausaReasons> dataPausaReasons;
    private ProgressDialog dialogo;
    private TextView fontTextView2;
    private TextView lblDescription;
    private TextView lblInitTime;
    private TextView lblNombreSolicitante;
    private TextView lblObservations;
    private TextView lblPhone;
    private SyncDataService mSensorService;
    private Intent mServiceIntent;
    private boolean blockService = false;
    private AlarmManager manager;
    private PendingIntent pendingIntent;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
                if (GlobalClass.getInstance().getCurrentService().isStarted()) {
                    if (!mTracking) {
                        startForegroundServices();
                        gpsService.startTracking();
                        mTracking = true;
                    } else if (mTracking && (!isMyServiceRunning(BackgroundService.class)) || (!isMyServiceRunning(SyncDataService.class))) {
                        startForegroundServices();
                        gpsService.startTracking();
                    }
                }
                toggleButtons();

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                stopTracking();
                gpsService = null;
            }
        }
    };
    private final String TAG = "MainActivity";

    private void startForegroundServices() {

        if (!isMyServiceRunning(BackgroundLocationUpdateService.class))
            startService(new Intent(MainActivity.this, BackgroundLocationUpdateService.class));
        startAlarm();
    }

    private void stopForegroundServices() {

        stopService(new Intent(this, BackgroundLocationUpdateService.class));

    }

    private ServiceRepository serviceRepository;
    private Button startButton;
    private TextView statusTextView;
    private Button stopButton;
    private Typeface typeface;
    private boolean haveActiveService;

    private void asyncListPausaReasons() {

        try {
            dialogo = new ProgressDialog(MainActivity.this);
            dialogo.setMessage("Cargando datos...");
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
                        public void onFinish() {
                            super.onFinish();
                            dialogo.hide();

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

                                DataAdapter dataAdapter = new DataAdapter(dataPausaReasons, new DataAdapter.RecyclerViewItemClickListener() {
                                    @Override
                                    public void clickOnItem(PausaReasons data) {
                                        customDialog.dismiss();

                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


                                        pauseService(sdf);
                                        //stopTracking();


                                    }
                                });
                                customDialog = new CustomListViewDialog(MainActivity.this, dataAdapter);
                                customDialog.show();
                                customDialog.setCanceledOnTouchOutside(false);


                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();

                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pauseService(SimpleDateFormat sdf) {
        GlobalClass.getInstance().getCurrentService().setPaused(true);
        GlobalClass.getInstance().getCurrentService().setStarted(false);
        GlobalClass.getInstance().getCurrentService().setStoped(false);
        GlobalClass.getInstance().getCurrentService().setFechaPausa(sdf.format(new Date()));
        ServiceInfo serviceInfo = serviceRepository.getStartetService();

        if (serviceInfo == null)
            return;

        serviceInfo.setPaused(true);
        serviceInfo.setStarted(false);
        serviceInfo.setStoped(false);
        serviceInfo.setFechaPausa(sdf.format(new Date()));
        serviceRepository.updateService(serviceInfo);
        toggleButtons();

    }

    private void asyncServiceInfoById() {

        dialogo = new ProgressDialog(MainActivity.this);
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
                    public void onFinish() {
                        super.onFinish();
                        dialogo.hide();

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
                            initializaValues();


                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();

                        }
                    }
                }
        );
    }

    private void confirmCausePauseDialog() {
        asyncListPausaReasons();

    }

    private void confirmStartService() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);

        if (GlobalClass.getInstance().getCurrentService().isPaused())
            dlgAlert.setMessage("¿Seguro de Reiniciar el servicio?");
        else
            dlgAlert.setMessage("¿Seguro de iniciar el servicio?");
        dlgAlert.setTitle(getString(R.string.app_name));
        //dlgAlert.setPositiveButton(getString(R.string.Texto_Boton_Ok), null);
        dlgAlert.setPositiveButton(R.string.Texto_Boton_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                startTracking(true);
            }
        });
        dlgAlert.setNegativeButton(R.string.Texto_Boton_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void confirmStopService() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);

        dlgAlert.setMessage("¿Seguro de detener el servicio?");
        dlgAlert.setTitle(getString(R.string.app_name));
        //dlgAlert.setPositiveButton(getString(R.string.Texto_Boton_Ok), null);
        dlgAlert.setPositiveButton(R.string.Texto_Boton_Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                GlobalClass.getInstance().getCurrentService().setPaused(false);
                GlobalClass.getInstance().getCurrentService().setStarted(false);
                GlobalClass.getInstance().getCurrentService().setStoped(true);
                stopTracking();

            }
        });
        dlgAlert.setNegativeButton(R.string.Texto_Boton_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public Context getCtx() {
        return ctx;
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
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

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
                    Log.i("isMyServiceRunning?", true + "");
                    return true;
                }
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

    private void setWidgetIds() {
        startButton = (Button) findViewById(R.id.startButton);
        stopButton = (Button) findViewById(R.id.stopButton);
        statusTextView = (TextView) findViewById(R.id.statusTextView);

        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
    }

    private void startBackgroundServices() {
        toggleButtons();
    }

    public void startTracking(final boolean isStarted) {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        ServiceInfo serviceInfo = null;

                        if (isStarted)
                            GlobalClass.getInstance().getCurrentService().setStarted(true);

                        if (GlobalClass.getInstance().getCurrentService().getIdService() > 0) {
                            GlobalClass.getInstance().getCurrentService().setFechaPausa("");
                            serviceRepository.updateService(GlobalClass.getInstance().getCurrentService());
                        } else
                            serviceRepository.insertService(GlobalClass.getInstance().getCurrentService());

                        startForegroundServices();
                        toggleButtons();
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
    public void cancelAlarm() {
        if (manager != null) {
            manager.cancel(pendingIntent);
            //Toast.makeText(this, "Alarm Canceled", Toast.LENGTH_SHORT).show();
        }
    }
    public void stopTracking() {
        mTracking = false;

        GlobalClass.getInstance().getCurrentService().setStarted(false);
        GlobalClass.getInstance().getCurrentService().setPaused(false);
        GlobalClass.getInstance().getCurrentService().setStoped(true);
        serviceRepository.updateService(GlobalClass.getInstance().getCurrentService());
        serviceRepository.deleteAllService();
        stopBackgroundServices();
        finish();

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
            }
            if (GlobalClass.getInstance().getCurrentService().isStoped()) {
                btnPause.setVisibility(View.INVISIBLE);
                btnStop.setVisibility(View.INVISIBLE);
                btnStart.setVisibility(View.VISIBLE);
            }

            statusTextView.setText((mTracking) ? "TRACKING" : "GPS Ready");
        }
        lblStop.setVisibility(btnStop.getVisibility());
        lblPause.setVisibility(btnPause.getVisibility());
        lblStart.setVisibility(btnStart.getVisibility());

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                startTracking(true);
                break;
            case R.id.stopButton:
                stopTracking();
                break;
        }
    }

    public void startAlarm() {
        manager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        int interval = 10000;
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);
        Toast.makeText(this, "Alarm Set", Toast.LENGTH_SHORT).show();
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

            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {


                }
            };
            broadcastReceiverBackgroundService = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //Valida si la hora actual sobrepasa la hora estimada de finalización del servicio, si es así, envia una notificación
                    // Preguntando al usuario, si desea finalizar el servicio, ya que la hora ha sido superada

                    //Pregunta si el servicio está inactivo, si esta inactivo es porque se encuentra visualizando un servicio que no es el que está activo
                    if (blockService)
                        return;
                    else {
                        ServiceInfo serviceInfo = serviceRepository.getStartetService();
                        ValidateIfEndedService(serviceInfo);

                        try {
                            String speed = intent.getStringExtra(BackgroundLocationUpdateService.SERVICE_MESSAGE);
                            //Si la velocidad es superior a 4 kms/h inicia automaticamente el servicio.
                            if (!speed.equals("")) {
                                if (serviceInfo != null)
                                    if (Float.valueOf(speed) > 4 && serviceInfo.isPaused()) {
                                        serviceInfo.setStarted(true);
                                        serviceInfo.setStoped(false);
                                        serviceInfo.setPaused(false);
                                        serviceRepository.updateService(serviceInfo);
                                        startTracking(true);
                                        sendNotificationEndService("Se ha detectado actividad y el servicio se reanudará automáticamente");
                                    } else {
                                        if (Float.valueOf(speed) < 4 && serviceInfo.isStarted()) {
                                            if (serviceInfo != null)
                                                ValidateIfAutoStartTrace(serviceInfo);
                                        }
                                        if (Float.valueOf(speed) > 4 && serviceInfo.isStarted()) {
                                            serviceInfo.setFechaPausa("");
                                            serviceRepository.updateService(serviceInfo);
                                        }
                                    }
                                toggleButtons();
                            } else {
                                Intent i = new
                                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
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

            initializaData();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void ValidateIfEndedService(ServiceInfo serviceInfo) {
        Date endedServiceDate = GlobalClass.getInstance().getCurrentTime();
        try {
            Date currentTime = GlobalClass.getInstance().getCurrentTime();
            currentServiceInfo = serviceInfo;
            if (currentServiceInfo != null) {
                String srEndServiceTime = currentServiceInfo.getFechaNotification();
                String srNextDateNotification = currentServiceInfo.getFechaUltimaNotification();
                Date endServiceTime = null;
                try {
                    if (serviceInfo.getFechaUltimaNotification() != null)
                        endServiceTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(srNextDateNotification);//Genera varaible de tipo date con la fecha acutal y hora actual
                    else
                        endServiceTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(srEndServiceTime);//Genera varaible de tipo date con la fecha acutal y hora actual
                    endedServiceDate.setHours(endServiceTime.getHours());//A la fecha actual le setea la hora en que finaliza el servicio
                    endedServiceDate.setMinutes(endServiceTime.getMinutes() + serviceInfo.getMinutesAfter());

                    if (endedServiceDate.before(currentTime)) {
                        sendNotificationEndService("El servicio a superado la hora estimada de finalización");
                        showConfirmStopEndedService(serviceInfo);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                        String nextDateNotifications = sdf.format(currentTime.getTime());
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

    private void showConfirmStopEndedService(final ServiceInfo serviceInfo) {
        // Use the Builder class for convenient dialog construction
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Confirmación");
            alert.setMessage("El servicio a superado la hora límite. ¿Desea continiar?");
            alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    serviceInfo.setMinutesAfter(serviceInfo.getMinutesAfter() + 2);
                    serviceRepository.updateService(serviceInfo);
                    dialog.dismiss();
                }
            });

            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    stopTracking();
                    dialog.dismiss();
                }
            });

            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //Valida si se debe iniciar automáticamente la traza
    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void ValidateIfAutoStartTrace(ServiceInfo serviceInfo) {
        Log.i(TAG, "ValidateIfAutoStartTrace ");
        String srPauseDate = serviceInfo.getFechaPausa();
        Date currentDate = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            if (serviceInfo != null) {
                if (serviceInfo.getFechaPausa().equals("")) {
                    serviceInfo.setFechaPausa(sdf.format(new Date()));
                    serviceRepository.updateService(serviceInfo);
                }

                Date dtPauseDate = sdf.parse(srPauseDate);
                long diffInMillies = Math.abs(Objects.requireNonNull(dtPauseDate).getTime() - currentDate.getTime());
                long diff = diffInMillies / (60 * 1000);
                Log.i(TAG, "ValidateIfAutoStartTrace " + diff);
                if (diff >= 1) {
                    Log.i(TAG, "Pausing service ");
                    pauseService(sdf);
                    sendNotificationEndService("No se ha detectado actividad y el servicio ha sido pausado autmaticamente");
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void sendNotificationEndService(String contentText) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

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
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(0, builder.getNotification());
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverBackgroundService),
                new IntentFilter(BackgroundLocationUpdateService.SERVICE_RESULT));

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverFirebase),
                new IntentFilter(PatFirebaseService.SERVICE_RESULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stopBackgroundServices();
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

    private void inactiveAllButtons() {
        btnPause.setVisibility(View.GONE);
        btnStart.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        lblStop.setVisibility(btnStop.getVisibility());
        lblPause.setVisibility(btnPause.getVisibility());
        lblStart.setVisibility(btnStart.getVisibility());
        blockService = true;
    }
}
