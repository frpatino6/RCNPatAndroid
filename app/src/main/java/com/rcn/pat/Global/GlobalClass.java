package com.rcn.pat.Global;

import android.app.Application;

import com.rcn.pat.ViewModels.ListUserServices;


public class GlobalClass extends Application {
    private static GlobalClass instance;
    private boolean isPaused = false;
    private boolean isStarted = false;
    private boolean isStoped = true;

    private String docNumber;
    private ListUserServices currentService;
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

    public ListUserServices getCurrentService() {
        return currentService;
    }

    public void setCurrentService(ListUserServices currentService) {
        this.currentService = currentService;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean paused) {
        isPaused = paused;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted(boolean started) {
        isStarted = started;
    }

    public boolean isStoped() {
        return isStoped;
    }

    public void setStoped(boolean stoped) {
        isStoped = stoped;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

    }
}
