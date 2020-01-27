package com.rcn.pat.Activities;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public BackgroundService gpsService;
    public boolean mTracking = false;
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
    private TextView fontTextView2;
    private TextView lblDescription;
    private TextView lblInitTime;
    private TextView lblNombreSolicitante;
    private TextView lblObservations;
    private TextView lblPhone;
    private SyncDataService mSensorService;
    private Intent mServiceIntent;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();


            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                gpsService = null;
            }
        }
    };
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
                                        GlobalClass.getInstance().getCurrentService().setPaused(true);
                                        GlobalClass.getInstance().getCurrentService().setStarted(false);
                                        GlobalClass.getInstance().getCurrentService().setStoped(false);
                                        serviceRepository.updateService(GlobalClass.getInstance().getCurrentService());
                                        //stopTracking();
                                        toggleButtons();

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
                startTracking();

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
        fontTextView2 = (TextView) findViewById(R.id.fontTextView2);

        btnPause.setTypeface(typeface);
        btnStart.setTypeface(typeface);
        btnStop.setTypeface(typeface);
        fontTextView2.setTypeface(typeface);
        serviceRepository = new ServiceRepository(getApplicationContext());

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
        lblInitTime.setText(GlobalClass.getInstance().getCurrentService().getFechaInicial() + " a " + GlobalClass.getInstance().getCurrentService().getFechaFinal());
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
        textView.setTextColor(getResources().getColor(R.color.cardViewHeaderColor));
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
        final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
        // this.getApplication().startService(intent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            this.getApplication().startForegroundService(intent);
        else
            this.getApplication().startService(intent);

        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        mSensorService = new SyncDataService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(SyncDataService.SERVICE_MESSAGE);

            }
        };
        broadcastReceiverBackgroundService = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String speed = intent.getStringExtra(BackgroundService.SERVICE_MESSAGE);

                if (Float.valueOf(speed) > 5 && GlobalClass.getInstance().getCurrentService().isPaused())
                    startTracking();

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
    }

    public void startTracking() {


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
                        gpsService.startTracking();
                        mTracking = true;
                        if (GlobalClass.getInstance().getCurrentService().getIdService() > 0)
                            serviceRepository.updateService(GlobalClass.getInstance().getCurrentService());
                        else
                            serviceRepository.insertService(GlobalClass.getInstance().getCurrentService());

                        GlobalClass.getInstance().getCurrentService().setPaused(false);
                        GlobalClass.getInstance().getCurrentService().setStarted(true);
                        GlobalClass.getInstance().getCurrentService().setStoped(false);
                        toggleButtons();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();


        //Inicia servicio que se ejecuta cada X tiempo para enviar al backEnd la traza leida hasta el momento
        if (!isMyServiceRunning(mSensorService.getClass())) {
            startService(mServiceIntent);
        }


    }

    private void stopBackgroundServices() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverBackgroundService);
        final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
        this.getApplication().stopService(intent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();

    }

    public void stopTracking() {
        mTracking = false;
        //
        stopService(mServiceIntent);
        toggleButtons();
        stopBackgroundServices();
        new asyncDeleteService().execute();

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                startTracking();
                break;
            case R.id.stopButton:
                stopTracking();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            setTitle("Servicio " + GlobalClass.getInstance().getCurrentService().getSolicitudNombre());

            //initialize views
            setWidgetIds();
            //prepare service
            ButterKnife.bind(this);
            initializaControls();
            initializaValues();
            initializaEvents();
            ctx = this;
            startBackgroundServices();
            new asyncGetServiceById().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(SyncDataService.SERVICE_RESULT));

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverBackgroundService),
                new IntentFilter(BackgroundService.SERVICE_RESULT));

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverFirebase),
                new IntentFilter(PatFirebaseService.SERVICE_RESULT));
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    class asyncDeleteService extends AsyncTask {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Object doInBackground(Object[] objects) {

            serviceRepository.deleteAllService();
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {


            gpsService.stopTracking();

            super.onPostExecute(o);
        }

        @Override
        protected void onPreExecute() {


        }
    }

    class asyncGetServiceById extends AsyncTask {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Object doInBackground(Object[] objects) {

            int id = GlobalClass.getInstance().getCurrentService().getId();
            currentServiceInfo = serviceRepository.getService(id);
            return true;
        }

        @Override
        protected void onPostExecute(Object o) {

            if (currentServiceInfo != null) {
                GlobalClass.getInstance().setCurrentService(currentServiceInfo);
                toggleButtons();
                //startTracking();

            }
            super.onPostExecute(o);
        }

        @Override
        protected void onPreExecute() {


        }
    }

}
