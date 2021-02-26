package com.choicely.preassignmentproject.app;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;

public class PreAssignmentApplication extends Application {
    private static final String TAG = "PreAssignmentApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: App Start");
    }
}
