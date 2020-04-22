package com.example.geofencepractice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BackgroundService extends Service {

    private boolean isRunning;
    private Context context;
    private Thread backgroundThread;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate()  {
        this.context = this;
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }

    private Runnable myTask = new Runnable() {
        public void run() {
            //dostuff
            System.out.println("THE BACKGROUND SERVICE IS RUNNING");

            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stopSelf();
        }
    };

    @Override
    public void onDestroy() {
        this.isRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!this.isRunning) {
            this.isRunning = true;
            this.backgroundThread.start();
        }
        return START_STICKY;
    }
}
