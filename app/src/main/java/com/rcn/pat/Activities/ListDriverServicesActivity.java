package com.rcn.pat.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.Global.ServiceAdapter;
import com.rcn.pat.Global.onClickVIewDetail;
import com.rcn.pat.R;
import com.rcn.pat.ViewModels.ServiceInfo;
import com.rcn.pat.ViewModels.PausaReasons;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class ListDriverServicesActivity extends AppCompatActivity {

    private ServiceAdapter adapter;
    private ArrayList<ServiceInfo> data;
    private ArrayList<PausaReasons> dataPausaReasons;
    private ProgressDialog dialogo;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;

    private void asyncListProductions() {

        dialogo = new ProgressDialog(ListDriverServicesActivity.this);
        dialogo.setMessage("Cargando servicios...");
        dialogo.setIndeterminate(false);
        dialogo.setCancelable(false);
        dialogo.show();

        String url = GlobalClass.getInstance().getUrlServices() + "ScheduleByDriver?NoDocumento=" + GlobalClass.getInstance().getDocNumber();
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
                            TypeToken<List<ServiceInfo>> token = new TypeToken<List<ServiceInfo>>() {
                            };
                            Gson gson = new GsonBuilder().create();
                            // Define Response class to correspond to the JSON response returned
                            data = gson.fromJson(res, token.getType());
                            adapter = new ServiceAdapter(data, new onClickVIewDetail() {
                                @Override
                                public void onClick(ServiceInfo idServicio) {
                                    goDetailService(idServicio);
                                }
                            });
                            recyclerView.setAdapter(adapter);


                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();

                        }
                    }
                }
        );
    }



    private void goDetailService(ServiceInfo idServicio) {
        GlobalClass.getInstance().setCurrentService(idServicio);
        Intent intent = null;
        intent = new Intent(ListDriverServicesActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_driver_services);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);


        layoutManager = new LinearLayoutManager(ListDriverServicesActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        asyncListProductions();
    }
}
