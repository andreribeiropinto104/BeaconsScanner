package com.northteam.beaconsscanner.ui.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.IBeaconsDetailsScan;

import butterknife.Bind;
import butterknife.ButterKnife;

public class IBeaconDetailsActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "IBeaconDetailsActivity";


    public static Context context;
    @Bind(R.id.ibeacon_name)
    public TextView nameTextView;
    @Bind(R.id.power)
    public TextView txPowerTextView;
    @Bind(R.id.major)
    public TextView majorTextView;
    @Bind(R.id.minor)
    public TextView minorTextView;
    @Bind(R.id.rssi)
    public TextView rssiTextView;
    @Bind(R.id.distance)
    public TextView distanceTextView;
    @Bind(R.id.battery)
    public TextView batteryTextView;
    protected IBeaconDevice ibeacon;
    protected IBeaconsDetailsScan beaconScan;
    @Bind(R.id.toolbar)
    Toolbar toolbar;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ibeacon_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        context = this;

        ButterKnife.bind(this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Recebe da atividade anterior como parâmetro o dispositivo selecionado
            ibeacon = (IBeaconDevice) extras.get("IBEACON");
            nameTextView.setText(Html.fromHtml("<b>Nome:</b> &nbsp;&nbsp;" + ibeacon.getUniqueId()));
            distanceTextView.setText(Html.fromHtml("<b>Distância:</b> &nbsp;&nbsp; <i>a calibrar . . .</i>"));
            majorTextView.setText(Html.fromHtml("<b>Major:</b> &nbsp;&nbsp;" + ibeacon.getMajor()));
            minorTextView.setText(Html.fromHtml("<b>Minor:</b> &nbsp;&nbsp;" + ibeacon.getMinor()));
            rssiTextView.setText(Html.fromHtml("<b>RSSI:</b> &nbsp;&nbsp;" + ibeacon.getRssi() + " dBm"));
            txPowerTextView.setText(Html.fromHtml("<b>Tx Power:</b> &nbsp;&nbsp;" + ibeacon.getTxPower()));
            batteryTextView.setText(Html.fromHtml("<b>Bateria:</b> &nbsp;&nbsp;" + ibeacon.getBatteryPower() + "%"));
            beaconScan = new IBeaconsDetailsScan(context, ibeacon.getName());
            //beaconScan.startScan(this);


        }

        Log.i(TAG, "OnCreate(): ");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extras != null) {
                    Intent intentRangeActivity = new Intent(IBeaconDetailsActivity.this, DistanceRangeActivity.class);
                    intentRangeActivity.putExtra("IBEACON", ibeacon);
                    startActivity(intentRangeActivity);
                } else
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
                }
                return;
            }
        }
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {

    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {

        Log.i(TAG, "onEvent()");
        switch (bluetoothDeviceEvent.getEventType()) {
            case DEVICES_UPDATE:
                beaconScan.onDevicesUpdateEvent(bluetoothDeviceEvent);
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

        beaconScan.startScan(IBeaconDetailsActivity.this);

    }

    @Override
    protected void onPause() {
        super.onPause();

        beaconScan.deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconScan.deviceManager.disconnect();
        beaconScan.deviceManager = null;
        ButterKnife.unbind(this);
    }
}
