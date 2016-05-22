package com.april1985.legocar.ui;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.april1985.legocar.R;
import com.april1985.legocar.model.CarStatusChangeListener;
import com.april1985.legocar.model.LegoCar;
import com.april1985.legocar.service.BluetoothLeService;
import com.jmedeisis.bugstick.Joystick;
import com.jmedeisis.bugstick.JoystickListener;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private TextView status;
    private TextView distanceText;
    private BluetoothLeService mBluetoothLeService;
    private ImageView carStatus;
    private boolean connected = false;

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
            }
        }
    };
    private TextView batteryText;
    private LegoCar legoCar;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }

            legoCar = new LegoCar(mBluetoothLeService);
            legoCar.setCarStatusListener(new CarStatusChangeListener() {
                @Override
                public void onStatusUpdated() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (legoCar.isAlive()) {
                                carStatus.setImageResource(R.drawable.device_alive);
                            } else {
                                carStatus.setImageResource(R.drawable.device_dead);
                            }

                            batteryText.setText(String.valueOf(legoCar.getVoltage()));
                            distanceText.setText(String.valueOf(legoCar.getDistance()));
                        }
                    });
                }
            });

            mBluetoothLeService.setBtListener(legoCar);

            Log.e(TAG, "mBluetoothLeService is okay");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        status = (TextView) findViewById(R.id.btStatus);
        carStatus = (ImageView) findViewById(R.id.car_alive);
        carStatus.setImageResource(R.drawable.device_dead);
        batteryText = (TextView) findViewById(R.id.txtVoltage);
        distanceText = (TextView) findViewById(R.id.txtDistance);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        setupSteer();
        setupThrottle();
    }

    private void setupThrottle() {
        Joystick throttle = (Joystick) findViewById(R.id.joystickThrottle);
        throttle.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {
            }

            @Override
            public void onUp() {
                legoCar.throttle(0);
            }

            @Override
            public void onDrag(float degrees, float offset) {
                legoCar.throttle(offset * Math.signum(degrees));
            }
        });
    }

    private void setupSteer() {
        Joystick steer = (Joystick) findViewById(R.id.joystickSteer);
        steer.setJoystickListener(new JoystickListener() {
            @Override
            public void onDown() {

            }

            @Override
            public void onDrag(float degrees, float offset) {
                legoCar.steer((int) (180 - Math.abs(degrees)));
            }

            @Override
            public void onUp() {
                legoCar.steer(90);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
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
                invalidateOptionsMenu();
                return true;
            case R.id.action_disconnect:
                mBluetoothLeService.disconnect();
                connected = false;
                invalidateOptionsMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        if (mBluetoothLeService != null) {
            mBluetoothLeService.disconnect();
            mBluetoothLeService.scanAndConnect();
        }
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
}
