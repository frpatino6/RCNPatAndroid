package com.rcn.pat.Global;


import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.rcn.pat.ViewModels.ServiceInfo;

import java.util.List;

public class ServiceRepository {
    private String DB_NAME = "location_db";
    private MyDataBase myDataBase;

    public ServiceRepository(Context context) {
        myDataBase = Room.databaseBuilder(context, MyDataBase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }


    public void insertService(final ServiceInfo serviceInfo) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.servicesDao().insertServiceInfo(serviceInfo);
                ServiceInfo data = getService(serviceInfo.getId());
                return null;
            }
        }.execute();
    }

    public void updateService(final ServiceInfo ServiceInfo) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.servicesDao().updateServiceInfo(ServiceInfo);
                return null;
            }
        }.execute();
    }

    public void deleteService(final int id) {
        final ServiceInfo ServiceInfo = getService(id);
        if (ServiceInfo != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    myDataBase.servicesDao().deleteServiceInfo(ServiceInfo);
                    return null;
                }
            }.execute();
        }
    }

    public void deleteAllService() {


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.servicesDao().deleteAllServiceInfo();
                return null;
            }
        }.execute();

    }


    public void deleteService(final ServiceInfo ServiceInfo) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if (ServiceInfo != null)
                    myDataBase.servicesDao().deleteServiceInfo(ServiceInfo);
                return null;
            }
        }.execute();
    }

    public ServiceInfo getStartetService() {
        return myDataBase.servicesDao().getStartetService();
    }

    public ServiceInfo getService(int id) {
        return myDataBase.servicesDao().getServiceInfo(id);
    }

    public List<ServiceInfo> getService() {
        return myDataBase.servicesDao().getAllSections();
    }
}
