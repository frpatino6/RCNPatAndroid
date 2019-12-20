package com.rcn.pat.Global;

import android.app.Application;


public class GlobalClass extends Application {
    private static GlobalClass instance;
    private String docNumber;
    private String urlServices = "http://portalterceros.rcntv.com.co/API_Transportes/api/";

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

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }
}
