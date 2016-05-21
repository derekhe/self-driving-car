/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.april1985.legocar.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    public final static String ACTION_SEARCHING = "com.april1985.legocar.ACTION_SEARCHING";
    public final static String ACTION_DISCONNECTED = "com.april1985.legocar.ACTION_DISCONNECTED";
    public final static String ACTION_CONNECTED = "com.april1985.legocar.ACTION_CONNECTED";
    public final static String ACTION_DATA_AVAILABLE = "com.april1985.legocar.ACTION_DATA_AVAILABLE";
    public final static String ACTION_SEARCH_STOP = "com.april1985.legocar.ACTION_SEARCH_STOP";
    public final static String ACTION_DEVICE_FOUND = "com.april1985.legocar.ACTION_DEVICE_FOUND";

    public final static UUID UUID_NOTIFY =
            UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_SERVICE =
            UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    private static final long SCAN_PERIOD = 10000;
    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private final IBinder mBinder = new LocalBinder();
    private final int MAX_RETRY = 5;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    ReentrantLock lock = new ReentrantLock();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private Handler mHandler = new Handler();
    private BluetoothListener btListener;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            Log.i(TAG, "oldStatus=" + status + " NewStates=" + newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_SEARCHING;

                    broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_DISCONNECTED;
                    btListener.onDisconnected();
                    mBluetoothGatt.close();
                    mBluetoothGatt = null;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                findService(gatt.getServices());
            } else {
                if (mBluetoothGatt.getDevice().getUuids() == null)
                    Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                dataReceived(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            dataReceived(characteristic);
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if ("LEGO".equals(device.getName())) {
                        stopScan();
                        connect(device.getAddress());
                        broadcastUpdate(ACTION_DEVICE_FOUND);
                    }
                }
            };

    public BluetoothListener getBtListener() {
        return btListener;
    }

    public void setBtListener(BluetoothListener btListener) {
        this.btListener = btListener;
    }

    public void findService(List<BluetoothGattService> gattServices) {
        Log.i(TAG, "Count is:" + gattServices.size());
        for (BluetoothGattService gattService : gattServices) {
            Log.i(TAG, gattService.getUuid().toString());
            Log.i(TAG, UUID_SERVICE.toString());
            if (gattService.getUuid().toString().equalsIgnoreCase(UUID_SERVICE.toString())) {
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                Log.i(TAG, "Count is:" + gattCharacteristics.size());
                for (BluetoothGattCharacteristic gattCharacteristic :
                        gattCharacteristics) {
                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(UUID_NOTIFY.toString())) {
                        Log.i(TAG, gattCharacteristic.getUuid().toString());
                        Log.i(TAG, UUID_NOTIFY.toString());
                        mNotifyCharacteristic = gattCharacteristic;
                        setCharacteristicNotification(gattCharacteristic, true);
                        broadcastUpdate(ACTION_CONNECTED);
                        btListener.onConnected();
                        return;
                    }
                }
            }
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    private void dataReceived(final BluetoothGattCharacteristic characteristic) {
        String received = new String(characteristic.getValue());
        if (btListener != null) {
            btListener.onDataArrived(received);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to scanAndConnect.");
            return false;
        }

        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void write(String command) {
        lock.lock();
        if (mBluetoothGatt != null && mNotifyCharacteristic != null) {
            for (int i = 0; i < MAX_RETRY; i++) {
                mNotifyCharacteristic.setValue((command + '\r').getBytes());

                boolean sendResult = mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
                Log.e(TAG, "write " + command + " " + sendResult);
                if (sendResult) break;
                SystemClock.sleep(10);
            }
        }
        lock.unlock();
    }

    public void scanAndConnect() {
        broadcastUpdate(ACTION_SEARCHING);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);

        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
        broadcastUpdate(ACTION_SEARCH_STOP);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
}
