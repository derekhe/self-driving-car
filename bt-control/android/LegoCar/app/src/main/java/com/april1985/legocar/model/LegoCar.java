package com.april1985.legocar.model;

import android.util.Log;

import com.april1985.legocar.service.BluetoothListener;
import com.april1985.legocar.service.BluetoothLeService;

import java.util.Timer;
import java.util.TimerTask;

public class LegoCar implements BluetoothListener {
    private BluetoothLeService btService;
    private String TAG = LegoCar.class.getSimpleName();

    public LegoCar(BluetoothLeService btService) {
        this.btService = btService;
    }

    private Timer pingTimer;
    private boolean pingReplied = false;
    private boolean deviceAlive = false;

    private void startPing() {
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                deviceAlive = pingReplied;
                pingReplied = false;
                Log.e(TAG, "Device alive: " + deviceAlive);
                btService.write("@");
            }
        }, 0, 500);
    }

    private void stopPing() {
        pingTimer.cancel();
        pingTimer.purge();
        pingTimer = null;
    }

    @Override
    public void onConnected() {
        startPing();
    }

    @Override
    public void onDisconnected() {
        stopPing();
    }

    @Override
    public void onDataArrived(String data) {
        String command = data.trim();
        switch (command) {
            case "@":
                pingReplied = true;
                break;
        }
    }

    public boolean isAlive() {
        return deviceAlive;
    }
}
