package com.april1985.legocar;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private TextView status;
    private BluetoothLeService mBluetoothLeService;
    private boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        status = (TextView) findViewById(R.id.btStatus);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_SEARCHING);
        intentFilter.addAction(BluetoothLeService.ACTION_DEVICE_FOUND);
        intentFilter.addAction(BluetoothLeService.ACTION_SEARCH_STOP);
        intentFilter.addAction(BluetoothLeService.ACTION_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_connect).setVisible(!connected);
        menu.findItem(R.id.action_disconnect).setVisible(connected);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_connect:
                mBluetoothLeService.scanAndConnect();
                return true;
            case R.id.action_disconnect:
                mBluetoothLeService.disconnect();
                connected = false;
                return true;
        }

        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            Log.e(TAG, "mBluetoothLeService is okay");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case BluetoothLeService.ACTION_SEARCHING:
                    connected = false;
                    status.setText(R.string.device_scanning);
                    break;
                case BluetoothLeService.ACTION_DEVICE_FOUND:
                    connected = false;
                    status.setText(R.string.device_found);
                    break;
                case BluetoothLeService.ACTION_DISCONNECTED:
                    status.setText(R.string.device_disconnected);
                    connected = false;
                    invalidateOptionsMenu();
                    break;
                case BluetoothLeService.ACTION_CONNECTED:
                    connected = true;
                    Log.e(TAG, "In what we need");
                    status.setText(R.string.connected);
                    invalidateOptionsMenu();
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    Log.e(TAG, "RECV DATA");
                    String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    Log.e(TAG, data);
                    break;
            }
        }
    };
}
