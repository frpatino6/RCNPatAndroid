package com.rcn.pat.Activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.Global.BackgroundLocationService;
import com.rcn.pat.Global.CustomListViewDialog;
import com.rcn.pat.Global.DataAdapter;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.Global.SyncDataService;
import com.rcn.pat.Notifications.PatFirebaseService;
import com.rcn.pat.R;
import com.rcn.pat.ViewModels.ListUserServices;
import com.rcn.pat.ViewModels.PausaReasons;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int PERMISSION_REQUEST_CODE = 200;
    public BackgroundLocationService gpsService;
    public boolean mTracking = false;
    Context ctx;
    private AlarmManager alarmManager;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver broadcastReceiverBackgroundService;
    private BroadcastReceiver broadcastReceiverFirebase;
    private TextView btnPause;
    private TextView btnStart;
    private TextView btnStop;
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
    private PendingIntent pendingIntent;
    private Button startButton;
    private TextView statusTextView;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundLocationService")) {
                gpsService = ((BackgroundLocationService.LocationServiceBinder) service).getService();
                startButton.setEnabled(true);
                statusTextView.setText("GPS Ready");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundLocationService")) {
                gpsService = null;
            }
        }
    };
    private Button stopButton;
    private Typeface typeface;

    private void asyncListPausaReasons() {

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


                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();

                        }
                    }
                }
        );
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
                            TypeToken<ListUserServices> token = new TypeToken<ListUserServices>() {
                            };
                            Gson gson = new GsonBuilder().create();
                            // Define Response class to correspond to the JSON response returned
                            ListUserServices data = gson.fromJson(res, token.getType());
                            GlobalClass.getInstance().setCurrentService(data);
                            initializaValues();


                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();

                        }
                    }
                }
        );
    }

    private void cancelAlarm() {
        alarmManager.cancel(pendingIntent);
        Toast.makeText(getApplicationContext(), "Alarm Cancelled", Toast.LENGTH_LONG).show();
    }

    private void confirmCausePauseDialog() {
        DataAdapter dataAdapter = new DataAdapter(dataPausaReasons, new DataAdapter.RecyclerViewItemClickListener() {
            @Override
            public void clickOnItem(PausaReasons data) {
                customDialog.dismiss();
                GlobalClass.getInstance().setPaused(true);
                GlobalClass.getInstance().setStarted(false);
                GlobalClass.getInstance().setStoped(false);
                //stopTracking();
                toggleButtons();

            }
        });
        customDialog = new CustomListViewDialog(MainActivity.this, dataAdapter);

        customDialog.show();
        customDialog.setCanceledOnTouchOutside(false);
    }

    private void confirmStartService() {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(MainActivity.this);

        if (GlobalClass.getInstance().isPaused())
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
                GlobalClass.getInstance().setPaused(false);
                GlobalClass.getInstance().setStarted(false);
                GlobalClass.getInstance().setStoped(true);
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
        lblInitTime.setText(GlobalClass.getInstance().getCurrentService().getFechaInicial() + " " + GlobalClass.getInstance().getCurrentService().getFechaFinal());
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

    private void startAlarm() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, 0, pendingIntent);
        }

    }

    public void startTracking() {
        //check for permission
        if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsService.startTracking();
            mTracking = true;

            GlobalClass.getInstance().setPaused(false);
            GlobalClass.getInstance().setStarted(true);
            GlobalClass.getInstance().setStoped(false);

            //Inicia servicio que se ejecuta cada X tiempo para enviar al backEnd la traza leida hasta el momento
            if (!isMyServiceRunning(mSensorService.getClass())) {
                startService(mServiceIntent);
            }
            toggleButtons();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    public void stopTracking() {
        mTracking = false;
        gpsService.stopTracking();
        stopService(mServiceIntent);
        toggleButtons();
    }

    private void toggleButtons() {


        if (GlobalClass.getInstance().isStarted()) {
            btnPause.setVisibility(View.VISIBLE);
            btnStart.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.VISIBLE);
        }

        if (GlobalClass.getInstance().isPaused()) {
            btnPause.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.VISIBLE);
            btnStop.setVisibility(View.VISIBLE);
        }
        if (GlobalClass.getInstance().isStoped()) {
            btnPause.setVisibility(View.INVISIBLE);
            btnStop.setVisibility(View.INVISIBLE);
            btnStart.setVisibility(View.VISIBLE);
        }

        statusTextView.setText((mTracking) ? "TRACKING" : "GPS Ready");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Servicio " + GlobalClass.getInstance().getCurrentService().getSolicitudNombre());

        //initialize views
        setWidgetIds();
        //prepare service
        final Intent intent = new Intent(this.getApplication(), BackgroundLocationService.class);
        this.getApplication().startService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        initializaControls();
        initializaValues();
        initializaEvents();
        asyncListPausaReasons();
        toggleButtons();
        ctx = this;
        mSensorService = new SyncDataService(getCtx());
        mServiceIntent = new Intent(getCtx(), mSensorService.getClass());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(SyncDataService.SERVICE_MESSAGE);
                // do something here.
            }
        };
        broadcastReceiverBackgroundService= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String speed = intent.getStringExtra(BackgroundLocationService.SERVICE_MESSAGE);

                if (Float.valueOf(speed) > 5 && GlobalClass.getInstance().isPaused())
                    startTracking();
                // do something here.
            }
        };

        broadcastReceiverFirebase= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(PatFirebaseService.SERVICE_MESSAGE);
                //Actualiza la informaci[on del servicio
                asyncServiceInfoById();
            }
        };

    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(SyncDataService.SERVICE_RESULT));

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverBackgroundService),
                new IntentFilter(BackgroundLocationService.SERVICE_RESULT));

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverFirebase),
                new IntentFilter(PatFirebaseService.SERVICE_RESULT));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverBackgroundService);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiverFirebase);
        super.onStop();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startTracking();
            }
        }
    }
}
