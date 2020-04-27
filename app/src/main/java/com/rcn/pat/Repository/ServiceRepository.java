package com.rcn.pat.Repository;


import android.content.Context;
import android.os.AsyncTask;

import androidx.room.Room;

import com.rcn.pat.Global.GlobalClass;
import com.rcn.pat.Global.MyDataBase;
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


    public ServiceInfo insertService(final ServiceInfo serviceInfo) {
        //Valida que no haya un registro con el mismo id
        ServiceInfo data = getService(serviceInfo.getId());
        if (data != null)
            return serviceInfo;
        myDataBase.servicesDao().insertServiceInfo(serviceInfo);
        GlobalClass.getInstance().setCurrentService(serviceInfo);
        data = getService(serviceInfo.getId());
        return data;
    }

    public void updateService(final ServiceInfo ServiceInfo) {
        myDataBase.servicesDao().updateServiceInfo(ServiceInfo);
        GlobalClass.getInstance().setCurrentService(ServiceInfo);
    }

    public void deleteService(final int id) {
        final ServiceInfo ServiceInfo = getService(id);
        if (ServiceInfo != null) {
            myDataBase.servicesDao().deleteServiceInfo(ServiceInfo);
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

    public void deleteOldServiceInfo() {


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.servicesDao().deleteOldServiceInfo();
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
