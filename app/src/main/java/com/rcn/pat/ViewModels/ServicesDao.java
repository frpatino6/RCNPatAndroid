package com.rcn.pat.ViewModels;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;



import java.util.List;

@Dao
public interface ServicesDao {

    @Insert
    Long insertServiceInfo(ServiceInfo serviceInfo);

    @Query("SELECT * FROM ServiceInfo WHERE id =:id")
    ServiceInfo getServiceInfo(int id);

    @Query("SELECT * FROM ServiceInfo")
    List<ServiceInfo> getAllSections();

    @Update
    void updateServiceInfo(ServiceInfo serviceInfo);


    @Delete
    void deleteServiceInfo(ServiceInfo serviceInfo);

    @Query("DELETE FROM ServiceInfo")
    void deleteAllServiceInfo();
}
