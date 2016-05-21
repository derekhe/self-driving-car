package com.april1985.legocar.model;

import android.util.Log;

import com.april1985.legocar.service.BluetoothLeService;
import com.april1985.legocar.service.BluetoothListener;

import java.util.Timer;
import java.util.TimerTask;

public class LegoCar implements BluetoothListener {
    private final int MAX_VOLTAGE = 8;
    private BluetoothLeService btService;
    private String TAG = LegoCar.class.getSimpleName();
    private CarStatusChangeListener carStatusListener;
    private int MIN_VOLTAGE = 6;
    private Timer pingTimer;
    private boolean pingReplied = false;
    private boolean deviceAlive = false;
    private float voltage = MAX_VOLTAGE;

    public LegoCar(BluetoothLeService btService) {
        this.btService = btService;
    }

    @Override
    public void onConnected() {
        startPing();
    }

    @Override
    public void onDisconnected() {
        stopPing();
    }

    private void stopPing() {
        pingTimer.cancel();
        pingTimer.purge();
        pingTimer = null;
    }

    @Override
    public void onDataArrived(String data) {
        Log.e(TAG, data);
        String[] strings = data.split("\r\n");

        for (String command : strings) {
            command = command.trim();
            if (command.startsWith("@")) {
                pingReplied = true;
            } else if (command.startsWith("V")) {
                voltage = Float.parseFloat(command.substring(1));
            }
        }


        if (carStatusListener != null) carStatusListener.onStatusUpdated();
    }

    private void startPing() {
        pingTimer = new Timer();
        pingTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                deviceAlive = pingReplied;
                pingReplied = false;
                if (carStatusListener != null) carStatusListener.onStatusUpdated();
                btService.write("@");
                btService.write("V");
            }
        }, 0, 500);
    }

    public boolean isAlive() {
        return deviceAlive;
    }

    public void setCarStatusListener(CarStatusChangeListener carStatusListener) {
        this.carStatusListener = carStatusListener;
    }

    public int getVoltagePercent() {
        return (int) ((getVoltage() - MIN_VOLTAGE) / (MAX_VOLTAGE - MIN_VOLTAGE) * 100);
    }

    public float getVoltage() {
        return voltage;
    }

    public void steer(int angle) {
        if (btService == null) return;
        angle = Math.abs(angle);
        btService.write("S " + angle);
    }

    public void throttle(float value) {
        if (btService == null) return;
        int throttle = (int) (Math.abs(value * 255));
        if (value > 0) {
            btService.write("F " + throttle);
        } else {
            btService.write("B " + throttle);
        }
    }

    public void release() {
        if (btService == null) return;
        btService.write("P");
    }
}
