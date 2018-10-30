package com.experiment.mslab.beacondetection;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
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
import android.widget.Toast;

import com.hereapps.ibeacon.IBeacon;
import com.hereapps.ibeacon.IBeaconLibrary;
import com.hereapps.ibeacon.IBeaconListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final int REQUEST_ENABLE_BT = 0xa01;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;

    private String TAG = "BtDetection";
    private TextView tv_title;

    private static ArrayList<IBeacon> iBeacons;
    private ArrayAdapter<IBeacon> iBeaconAdapter;
    private IBeaconLibrary iBeaconLibrary;
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

        if(iBeacons == null)
            iBeacons = new ArrayList<IBeacon>();
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

                textView.setText(beaconDetail);
                textView2.setText(beacon.getUuidHexStringDashed());
                return view;
            }
        };

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
                iBeacons.clear();
                iBeaconAdapter.notifyDataSetChanged();
                scanBeacons();
                break;
        }
    }

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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
//            tv_title.setText("Enter time:" + simpleDateFormat.format(date));
            String text = "Enter time:" + simpleDateFormat.format(date);
            Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        public void beaconExit(IBeacon iBeacon) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
//            tv_title.setText("Exit time:" + simpleDateFormat.format(date));
            String text = "Exit time:" + simpleDateFormat.format(date);
            Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT);
            toast.show();
        }

        @Override
        public void beaconFound(IBeacon iBeacon) {
            Toast toast = Toast.makeText(MainActivity.this, "Found beacon!", Toast.LENGTH_SHORT);
            toast.show();
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
