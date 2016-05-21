package com.april1985.legocar.service;

public interface BluetoothListener {
    void onConnected();
    void onDisconnected();
    void onDataArrived(String data);
}
