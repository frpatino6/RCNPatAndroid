package com.rcn.pat.Global;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Time;

import com.rcn.pat.ViewModels.ServiceInfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class GlobalClass extends Application {
    private static GlobalClass instance;

    private int minSendLocationToDatabase = 15; //Intervalo en segundos para enviar los datos de localización al backend
    private String docNumber;
    private ServiceInfo currentService;
    private ArrayList<ServiceInfo> listServicesDriver;
    private String urlServices = "http://portalterceros.rcntv.com.co/API_Transportes/api/";
    //private String urlServices = "http://172.20.0.153/Intranet/GerenciaTI/API_Transportes/api/";

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

    public String getDateFormat(String inputDate) {
        Date parsed = null;
        String outputDate = "";

        String inputFormat = "yyyy-MM-dd'T'HH:mm:ss";
        String outputFormat = "HH:mm";

        SimpleDateFormat df_input = new SimpleDateFormat(inputFormat, java.util.Locale.getDefault());
        SimpleDateFormat df_output = new SimpleDateFormat(outputFormat, java.util.Locale.getDefault());

        try {
            parsed = df_input.parse(inputDate);
            outputDate = df_output.format(parsed);

        } catch (ParseException e) {

        }

        return outputDate;
    }

    public Date getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String newTime = sdf.format(cal.getTime());
        return cal.getTime();
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
