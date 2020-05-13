package com.rcn.pat.Global;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.rcn.pat.Dao.LocationDao;
import com.rcn.pat.Dao.LoginDao;
import com.rcn.pat.ViewModels.LoginViewModel;
import com.rcn.pat.ViewModels.ServiceInfo;
import com.rcn.pat.Dao.ServicesDao;

@Database(entities = {MyLocation.class, ServiceInfo.class, LoginViewModel.class}, version = 8, exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {
    public abstract LocationDao dao();
    public abstract ServicesDao servicesDao();
    public abstract LoginDao loginDao();
}
