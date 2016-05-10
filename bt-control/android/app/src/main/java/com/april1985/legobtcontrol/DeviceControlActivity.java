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

package com.april1985.legobtcontrol;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.le.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.widget.Toast.LENGTH_SHORT;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final long SCAN_PERIOD = 10000;
    private final Handler mHandler = new Handler();
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected = false;
    private TextView mData;
    private Button btnForward;
    private Button btnBackward;
    private Button btnLeft;
    private Button btnRight;
    private Button btnStraight;
    private Button btnPause;
    private boolean mScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private ArrayList<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        public final UUID UUID_NOTIFY =
                UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
        public final UUID UUID_SERVICE =
                UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    gatt.disconnect();
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            findService(services);
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
                            mGatt.setCharacteristicNotification(gattCharacteristic, true);
                            mConnected = true;
                            return;
                        }
                    }
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            String data = characteristic.toString();
            Log.i("onCharacteristicRead", data);
            mData.setText(data);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();

            mGatt = btDevice.connectGatt(DeviceControlActivity.this, true, gattCallback);
            mLEScanner.stopScan(mScanCallback);
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        btnForward = (Button) findViewById(R.id.btnForward);
        btnBackward = (Button) findViewById(R.id.btnBackward);
        btnLeft = (Button) findViewById(R.id.btnLeft);
        btnRight = (Button) findViewById(R.id.btnRight);
        btnStraight = (Button) findViewById(R.id.btnStraight);
        btnPause = (Button) findViewById(R.id.btnPause);

        setCommand(btnForward, "F 255");
        setCommand(btnBackward, "B 255");
        setCommand(btnLeft, "S 0");
        setCommand(btnStraight, "S 90");
        setCommand(btnRight, "S 180");
        setCommand(btnPause, "P");

        mData = (TextView) findViewById(R.id.data_value);

    }

    private void setCommand(Button buton, final String command) {
        buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mConnected) return;

                mNotifyCharacteristic.setValue((command + "\r").getBytes());
                mGatt.writeCharacteristic(mNotifyCharacteristic);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGatt.disconnect();
        Log.d(TAG, "We are in destroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                searchDevice();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_disconnect:
                mGatt.disconnect();
                mConnected = false;
                invalidateOptionsMenu();
                return true;
            case android.R.id.home:
                if (mConnected) {
                    mGatt.disconnect();
                    mConnected = false;
                }
                onBackPressed();
                invalidateOptionsMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void searchDevice() {
        requestPermission();

        scanLeDevice();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void scanLeDevice() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, LENGTH_SHORT).show();
            finish();
            return;
        }

        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<>();

        mLEScanner.stopScan(mScanCallback);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mScanning) {
                    mScanning = false;
                    mLEScanner.stopScan(mScanCallback);
                    invalidateOptionsMenu();
                }
            }
        }, SCAN_PERIOD);

        mScanning = true;
        mLEScanner.startScan(filters, settings, mScanCallback);

        invalidateOptionsMenu();
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                        }
                    }


                });
                builder.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                    finish();
                }
            }
        }
    }
}
