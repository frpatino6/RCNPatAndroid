package com.rcn.pat.Global;


import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import java.util.List;

public class LocationRepository {
    private String DB_NAME = "location_db";
    private MyDataBase myDataBase;

    public LocationRepository(Context context) {
        myDataBase = Room.databaseBuilder(context, MyDataBase.class, DB_NAME).build();
    }


    public void insertLocation(final MyLocation location) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.dao().insertLocation(location);
                List<MyLocation> data = getLocations();
                return null;
            }
        }.execute();
    }

    public void updateLocation(final MyLocation location) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.dao().updateLocation(location);
                return null;
            }
        }.execute();
    }

    public void deleteLocation(final int id) {
        final LiveData<MyLocation> location = getLocation(id);
        if (location != null) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    myDataBase.dao().deleteLocation(location.getValue());
                    return null;
                }
            }.execute();
        }
    }
    public void deleteAllLocation() {


            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    myDataBase.dao().deleteAllLocation();
                    return null;
                }
            }.execute();

    }


    public void deleteLocation(final MyLocation location) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                myDataBase.dao().deleteLocation(location);
                return null;
            }
        }.execute();
    }

    public LiveData<MyLocation> getLocation(int id) {
        return myDataBase.dao().getLocation(id);
    }

    public List<MyLocation> getLocations() {
        return myDataBase.dao().getAllSections();
    }
}
