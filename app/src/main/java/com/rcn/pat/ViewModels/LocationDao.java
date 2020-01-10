package com.rcn.pat.ViewModels;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rcn.pat.Global.MyLocation;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    Long insertLocation(MyLocation location);


    @Query("SELECT * FROM MyLocation")
    LiveData<List<MyLocation>> fetchAllLocation();


    @Query("SELECT * FROM MyLocation WHERE id =:id")
    LiveData<MyLocation> getLocation(int id);

    @Query("SELECT * FROM MyLocation")
    List<MyLocation> getAllSections();

    @Update
    void updateLocation(MyLocation location);


    @Delete
    void deleteLocation(MyLocation location);

    @Query("DELETE FROM MyLocation")
    void deleteAllLocation();
}