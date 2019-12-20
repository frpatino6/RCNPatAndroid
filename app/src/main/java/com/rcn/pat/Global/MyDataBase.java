package com.rcn.pat.Global;


import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.rcn.pat.ViewModels.LocationDao;

@Database(entities = {MyLocation.class}, version = 1, exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {
    public abstract LocationDao dao();
}
