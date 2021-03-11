package com.rcn.pat.Repository;

import android.content.Context;

import androidx.room.Room;

import com.rcn.pat.Global.MyDataBase;
import com.rcn.pat.ViewModels.LoginViewModel;

public class LoginRepository {
    private String DB_NAME = "location_db";
    private MyDataBase myDataBase;

    public LoginViewModel getLoginByUserName() {
        return myDataBase.loginDao().getLoginByUserName();
    }

    public void updateLogin(LoginViewModel loginViewModel) {
        myDataBase.loginDao().updateLogin(loginViewModel);
    }

    public long insert(LoginViewModel loginViewModel) {
        return myDataBase.loginDao().insertLocation(loginViewModel);
    }

    public void logOut(LoginViewModel loginViewModel) {
        myDataBase.loginDao().LogOut(loginViewModel);
    }

    public LoginRepository(Context context) {
        myDataBase = Room.databaseBuilder(context, MyDataBase.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();
    }
}
