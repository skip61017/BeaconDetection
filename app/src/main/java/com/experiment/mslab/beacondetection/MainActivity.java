package com.experiment.mslab.beacondetection;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.experiment.mslab.beacondetection.R;
import com.hereapps.ibeacon.IBeacon;
import com.hereapps.ibeacon.IBeaconLibrary;
import com.hereapps.ibeacon.IBeaconListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final int REQUEST_ENABLE_BT = 0xa01;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;

    private String TAG = "BtDetection";

    private static ArrayList<IBeacon> iBeacons;
    private ArrayAdapter<IBeacon> iBeaconAdapter;
    private IBeaconLibrary iBeaconLibrary;
    private ArrayAdapter<String> mAdapter;
    private static BluetoothAdapter mBtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn_scan = (Button)findViewById(R.id.button_scan);
        ListView listView = (ListView)findViewById(R.id.listView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

//        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(mReceiver, filterFound);
//
//        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//        registerReceiver(mReceiver, filterStart);
//
//        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        registerReceiver(mReceiver, filterFinish);

        if(iBeacons == null)
            iBeacons = new ArrayList<IBeacon>();
//        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);
        iBeaconAdapter = new ArrayAdapter<IBeacon>(this, android.R.layout.simple_list_item_1, android.R.id.text1, iBeacons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                TextView textView = (TextView)findViewById(R.id.textView);
                TextView textView2 = (TextView)findViewById(R.id.textView2);

                IBeacon beacon = iBeacons.get(position);
                String beaconDetail =
                        " Major: " + beacon.getMajor() +
                                " Minor: " + beacon.getMinor() +
                                " Distance: " + beacon.getProximity() + "m.";

                textView.setText(beacon.getUuidHexStringDashed());
                textView2.setText(beaconDetail);
                return view;
            }
        };

//        listView.setAdapter(mAdapter);
        listView.setAdapter(iBeaconAdapter);
        iBeaconLibrary = IBeaconLibrary.getInstance();
        iBeaconLibrary.setListener(iBeaconListener);

        btn_scan.setOnClickListener(this);

        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_scan:
//                discovery();
                iBeacons.clear();
                iBeaconAdapter.notifyDataSetChanged();
                scanBeacons();
                break;
        }
    }

//    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                Log.d(TAG, "Discovery started...");
//            }
//
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                Log.d(TAG, "Device found.");
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (device != null) {
//                    mAdapter.add("Device: " + device.getName() + "\nAddress: " + device.getAddress());
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                Log.d(TAG, "Discovery finished.");
//            }
//        }
//    };

    private void init() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Log.d(TAG, "Cannot support bluetooth service.");
            return;
        }

        if (!mBtAdapter.isEnabled()) {
            Log.d(TAG, "Please turn on bluetooth.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else {
            Log.d(TAG, "Bluetooth is already on.");
        }
        iBeaconLibrary.setBluetoothAdapter(this);
    }
//
//    private void discovery() {
//        if (mBtAdapter == null) {
//            init();
//        }
//        Log.d(TAG, "Method discovery.");
//        mBtAdapter.startDiscovery();
//    }

    private void scanBeacons() {
        Log.i(IBeaconLibrary.LOG_TAG,"Scanning");

        if (!IBeaconLibrary.setBluetoothAdapter(this)) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (iBeaconLibrary.isScanning())
                iBeaconLibrary.stopScan();
            iBeaconLibrary.reset();
            iBeaconLibrary.startScan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(mReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                scanBeacons();
            }
        }
    }

    private IBeaconListener iBeaconListener = new IBeaconListener() {
        @Override
        public void beaconEnter(IBeacon iBeacon) {

        }

        @Override
        public void beaconExit(IBeacon iBeacon) {

        }

        @Override
        public void beaconFound(IBeacon iBeacon) {
            iBeacons.add(iBeacon);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    iBeaconAdapter.notifyDataSetChanged();
                }
            });
        }

        @Override
        public void scanState(int state) {

        }

        @Override
        public void operationError(int status) {
            Log.i(IBeaconLibrary.LOG_TAG, "Bluetooth error: " + status);
        }
    };
}
