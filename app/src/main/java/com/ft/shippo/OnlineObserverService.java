package com.ft.shippo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ft.shippo.models.User;

/**
 * Created by rafael on 07/03/18.
 */

public class OnlineObserverService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        User.setOnline(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        User.setOffline(this);
        super.onTaskRemoved(rootIntent);
    }
}
