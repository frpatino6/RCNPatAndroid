package com.rcn.pat.Dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rcn.pat.ViewModels.ServiceInfo;

import java.util.Date;
import java.util.List;

@Dao
public interface ServicesDao {

    @Insert
    Long insertServiceInfo(ServiceInfo serviceInfo);

    @Query("SELECT * FROM ServiceInfo WHERE id =:id")
    ServiceInfo getServiceInfo(int id);

    @Query("SELECT * FROM ServiceInfo WHERE isStarted = 1 or isPaused=1 ")
    ServiceInfo getStartetService();

    @Query("SELECT * FROM ServiceInfo")
    List<ServiceInfo> getAllSections();


    @Update
    void updateServiceInfo(ServiceInfo serviceInfo);


    @Delete
    void deleteServiceInfo(ServiceInfo serviceInfo);

    @Query("DELETE FROM ServiceInfo")
    void deleteAllServiceInfo();


    @Query(" delete FROM ServiceInfo " +
            " where strftime('%d', `FechaFinal`)< strftime('%d', DATE('now','localtime'))")
    void deleteOldServiceInfo();
}
