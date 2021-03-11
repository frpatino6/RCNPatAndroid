package com.rcn.pat.Dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rcn.pat.ViewModels.LoginViewModel;

@Dao
public interface LoginDao {
    @Insert
    Long insertLocation(LoginViewModel loginViewModel);

    @Query("SELECT * FROM LoginViewModel ")
    LoginViewModel getLoginByUserName();

    @Update
    void updateLogin(LoginViewModel location);

    @Delete
    void LogOut(LoginViewModel location);
}
