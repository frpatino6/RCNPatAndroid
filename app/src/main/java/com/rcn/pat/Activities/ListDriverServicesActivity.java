package com.rcn.pat.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.Global.ServiceAdapter;
import com.rcn.pat.Global.SortbyDate;
import com.rcn.pat.Global.onClickVIewDetail;
import com.rcn.pat.Notifications.PatFirebaseService;
import com.rcn.pat.R;
import com.rcn.pat.Repository.ServiceRepository;
import com.rcn.pat.ViewModels.PausaReasons;
import com.rcn.pat.ViewModels.ServiceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ListDriverServicesActivity extends AppCompatActivity {
    private ServiceAdapter adapter;
    private BroadcastReceiver broadcastReceiverFirebase;
    private ArrayList<ServiceInfo> data;
    private ArrayList<PausaReasons> dataPausaReasons;
    private String deviceToken;
    private LinearLayoutManager layoutManager;
    private String pws;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (GlobalClass.getInstance().isNetworkAvailable())
            asyncListProductions(true);
    }

    private void asyncListProductions(boolean showProgress) {

        final ProgressDialog dialogo = new ProgressDialog(ListDriverServicesActivity.this);
        if (GlobalClass.getInstance().isNetworkAvailable()) {
            if (showProgress) {
                try {

                    dialogo.setMessage("Cargando servicios...");
                    dialogo.setIndeterminate(false);
                    dialogo.setCancelable(false);
                    dialogo.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String url = GlobalClass.getInstance().getUrlServices() + "ScheduleByDriver?NoDocumento=" + GlobalClass.getInstance().getDocNumber() + "&Token=" + deviceToken + "&Plataforma=android";
            AsyncHttpClient client = new AsyncHttpClient();
            client.setTimeout(60000);
            RequestParams params = new RequestParams();

            client.get(url, new TextHttpResponseHandler() {
                        @Override
                        public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                            dialogo.dismiss();

                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, String res) {
                            // called when response HTTP status is "200 OK"
                            try {

                                int a = 0;
                                TypeToken<List<ServiceInfo>> token = new TypeToken<List<ServiceInfo>>() {
                                };
                                Gson gson = new GsonBuilder().create();
                                // Define Response class to correspond to the JSON response returned
                                data = gson.fromJson(res, token.getType());
                                Collections.sort(data, new SortbyDate());

                                GlobalClass.getInstance().setListServicesDriver(data);
                                adapter = new ServiceAdapter(data, new onClickVIewDetail() {
                                    @Override
                                    public void onClick(ServiceInfo idServicio) {
                                        Log.i(TAG, "SERVICIO SELECCIONADO: " + idServicio.getId().toString());
                                        goDetailService(idServicio);
                                    }
                                });
                                for (ServiceInfo service : data) {
                                    ServiceInfo serviceInfo =serviceRepository.getService(service.getId());
                                    if(serviceInfo!=null){
                                        serviceInfo.setFechaFinal(service.getFechaFinal());
                                        serviceInfo.setIshalfhourNotify(false);
                                        serviceInfo.setIshourNotify(false);


                                        serviceRepository.updateService(serviceInfo);
                                    }
                                }
                                recyclerView.setAdapter(adapter);
                                deleteOldServices();
                                Log.i(TAG, "asyncListProductions Ejecutado con éxito");

                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();

                            }
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            dialogo.dismiss();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
            );
        }
    }

    private void goDetailService(ServiceInfo idServicio) {
        GlobalClass.getInstance().setCurrentService(idServicio);
        Intent intent = null;
        intent = new Intent(ListDriverServicesActivity.this, MainActivity.class);
        startActivityForResult(intent, 1);
    }

    private void deleteOldServices() {
        serviceRepository.deleteOldServiceInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_driver_services);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(ListDriverServicesActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        serviceRepository = new ServiceRepository(getApplicationContext());

        swipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (GlobalClass.getInstance().isNetworkAvailable())
                    asyncListProductions(false);
            }
        });
        broadcastReceiverFirebase = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(PatFirebaseService.SERVICE_MESSAGE);
                //Actualiza la informaci[on del servicio
                if (GlobalClass.getInstance().isNetworkAvailable())
                    asyncListProductions(true);
            }
        };
        getCurrentDeviceToken();


    }

    private String getCurrentDeviceToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {

                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        deviceToken = task.getResult().getToken();
                        Log.d(TAG, deviceToken);
                        if (GlobalClass.getInstance().isNetworkAvailable())
                            asyncListProductions(true);
                        // Log and toast
                        @SuppressLint({"StringFormatInvalid", "LocalSuppress"}) String msg = getString(R.string.msg_token_fmt, deviceToken);
                        Log.d(TAG, msg);
                        //Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
        return "";
    }

    @Override
    protected void onStart() {
        super.onStart();

        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiverFirebase),
                new IntentFilter(PatFirebaseService.SERVICE_RESULT));
    }

    ServiceRepository serviceRepository;
}
