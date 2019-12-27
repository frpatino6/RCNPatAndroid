package com.rcn.pat.Activities;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.BuildConfig;
import com.rcn.pat.Global.BackgroundLocationService;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.R;
import com.rcn.pat.ViewModels.PausaReasons;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int PERMISSION_REQUEST_CODE = 200;
    public BackgroundLocationService gpsService;
    public boolean mTracking = false;
    Button startButton;
    Button stopButton;
    TextView statusTextView;
    private ArrayList<PausaReasons> dataPausaReasons;
    private ProgressDialog dialogo;
    private TextView lblDescription;
    private TextView lblInitTime;
    private TextView lblNombreSolicitante;
    private TextView lblObservations;
    private TextView lblPhone;
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

    private void initializaControls() {
        lblNombreSolicitante = findViewById(R.id.lblNombreSolicitante);
        lblPhone = findViewById(R.id.lblPhone);
        lblInitTime = findViewById(R.id.lblInitTime);
        lblDescription = findViewById(R.id.lblDescription);
        lblObservations = findViewById(R.id.lblObservations);
    }

    private void initializaValues() {
        lblNombreSolicitante.setText(GlobalClass.getInstance().getCurrentService().getNombreUsuarioSolicitante());
        lblPhone.setText(GlobalClass.getInstance().getCurrentService().getCelularSolicitante());
        lblInitTime.setText(GlobalClass.getInstance().getCurrentService().getFechaInicial() + " " + GlobalClass.getInstance().getCurrentService().getFechaFinal());
        lblDescription.setText(GlobalClass.getInstance().getCurrentService().getDescripcionRecorrido());
        lblObservations.setText(GlobalClass.getInstance().getCurrentService().getObservaciones());
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

    public void startTracking() {
        //check for permission
        if (ContextCompat.checkSelfPermission(getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            gpsService.startTracking();
            mTracking = true;
            toggleButtons();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    public void stopTracking() {
        mTracking = false;
        gpsService.stopTracking();
        toggleButtons();
    }

    private void toggleButtons() {
        startButton.setEnabled(!mTracking);
        stopButton.setEnabled(mTracking);
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
        asyncListPausaReasons();
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
