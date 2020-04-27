package com.rcn.pat.Repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.rcn.pat.Global.MyDataBase;
import com.rcn.pat.ViewModels.LoginViewModel;

import java.util.List;

public class LoginRepository {
    private String DB_NAME = "location_db";
    private MyDataBase myDataBase;

    public LoginRepository(Context context) {
        myDataBase = Room.databaseBuilder(context, MyDataBase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }
    public LoginViewModel getLoginByUserName(){
        return myDataBase.loginDao().getLoginByUserName();
    }

    public void updateLogin(LoginViewModel loginViewModel){
        myDataBase.loginDao().updateLogin(loginViewModel);
    }

    public long insert(LoginViewModel loginViewModel){
        return myDataBase.loginDao().insertLocation(loginViewModel);
    }
}
