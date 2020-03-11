package com.rcn.pat.Global;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.rcn.pat.ViewModels.LocationDao;
import com.rcn.pat.ViewModels.ServiceInfo;
import com.rcn.pat.ViewModels.ServicesDao;

@Database(entities = {MyLocation.class, ServiceInfo.class}, version = 6, exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {
    public abstract LocationDao dao();
    public abstract ServicesDao servicesDao();
}
