package com.rcn.pat.Global;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.rcn.pat.ViewModels.ServiceInfo;

import java.util.ArrayList;


public class GlobalClass extends Application {
    private static GlobalClass instance;

    private int minSendLocationToDatabase= 20; //Intervalo en minutos para enviar los datos de localización al backend
    private String docNumber;
    private ServiceInfo currentService;
    private ArrayList<ServiceInfo>listServicesDriver;
    private String urlServices = "http://portalterceros.rcntv.com.co/API_Transportes/api/";
    //private String urlServices = "http://190.24.154.3/API_Transportes/api/";

    public int getMinSendLocationToDatabase() {
        return minSendLocationToDatabase;
    }

    public ArrayList<ServiceInfo> getListServicesDriver() {
        return listServicesDriver;
    }

    public void setListServicesDriver(ArrayList<ServiceInfo> listServicesDriver) {
        this.listServicesDriver = listServicesDriver;
    }

    public static GlobalClass getInstance() {
        return instance;
    }

    public String getUrlServices() {
        return urlServices;
    }

    public void setUrlServices(String urlServices) {
        this.urlServices = urlServices;
    }

    public String getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(String docNumber) {
        this.docNumber = docNumber;
    }

    public ServiceInfo getCurrentService() {
        return currentService;
    }

    public void setCurrentService(ServiceInfo currentService) {
        this.currentService = currentService;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
