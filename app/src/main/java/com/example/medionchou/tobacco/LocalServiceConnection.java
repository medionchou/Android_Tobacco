package com.example.medionchou.tobacco;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created by medionchou on 2015/8/22.
 */
public class LocalServiceConnection implements ServiceConnection {

    private boolean mBound;
    private LocalService mService;

    public LocalServiceConnection() {
        mBound = false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LocalService.LocalBinder binder = (LocalService.LocalBinder)service;
        mService = binder.getService();
        mBound = true;

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mBound = false;
    }

    public boolean isBound() {
        return mBound;
    }

    public LocalService getService() {
        return mService;
    }

}
