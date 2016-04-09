package com.northteam.beaconsscanner.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.DeviceProfile;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.eddystone.EddystoneDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.BeaconsScanMonitorAdapter;
import com.northteam.beaconsscanner.model.BeaconWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BeaconsScanActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "BeaconsScanActivity";

    private String fileName = "";

    /**
     * The Eddystone name.
     */
    public HashMap eddystoneName;
    /**
     * The Scan context.
     */
    public ScanContext scanContext;
    @Bind(R.id.list_beacons)
    ExpandableListView listBeacons;
    private BeaconsScanMonitorAdapter beaconsAdapter;
    private ProximityManager deviceManager;
    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    private IBeaconScanContext beaconScanContext = new IBeaconScanContext.Builder()
            .setEventTypes(eventTypes) //only specified events we be called on callback
            .setRssiCalculator(RssiCalculators.DEFAULT)
            .build();

    private EddystoneScanContext eddystoneScanContext = new EddystoneScanContext.Builder()
            .setEventTypes(eventTypes)
            .setRssiCalculator(RssiCalculators.DEFAULT)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacons_scan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);


        beaconsAdapter = new BeaconsScanMonitorAdapter(this);

        deviceManager = new ProximityManager(this);

        listBeacons.setAdapter(beaconsAdapter);

         /*
            groupPosition: get index of the main list of the selected child:
                0 - IBeacon
                1 - Eddystone
            childPosition: get index in the list of the selected child
         */
        listBeacons.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                BeaconWrapper child = beaconsAdapter.getChild(groupPosition, childPosition);
                if (com.kontakt.sdk.android.ble.device.DeviceProfile.IBEACON == child.getDeviceProfile()) {
                    final IBeaconDevice ibeacon = child.getBeaconDevice();
                    Log.i("setOnClick", "IBEACON");

                    // Cria intent para a passagem para a atividade seguinte
                    Intent intentDetailsActivity = new Intent(BeaconsScanActivity.this, IBeaconDetailsActivity.class);
                    intentDetailsActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intentDetailsActivity.putExtra("IBEACON", ibeacon);

                    // Inicia a atividade seguinte
                    startActivity(intentDetailsActivity);


                } else if (com.kontakt.sdk.android.ble.device.DeviceProfile.EDDYSTONE == child.getDeviceProfile()) {

                    IEddystoneDevice eddystone = child.getEddystoneDevice();

                    Intent intentDetailsActivity = new Intent(BeaconsScanActivity.this, EddystoneDetailsActivity.class);
                    intentDetailsActivity.putExtra("EDDYSTONE", eddystone);
                    startActivity(intentDetailsActivity);
                    //finish();

                } else {
                    Log.i("setOnClick", "No profile detected");

                }


                return false;
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
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

    /**
     * Start scan.
     */
    public void startScan() {

        Log.i("BeaconsScanActivity", "startScan()");


        deviceManager.initializeScan(getOrCreateScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                deviceManager.attachListener(BeaconsScanActivity.this);
            }

            @Override
            public void onConnectionFailure() {
                Log.i("BeaconsScanActivity", "startScan() Erro na conexão");
            }
        });

    }

    /**
     * Gets or create scan context.
     *
     * @return the or create scan context
     */
    public ScanContext getOrCreateScanContext() {
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setIBeaconScanContext(beaconScanContext)
                    .setEddystoneScanContext(eddystoneScanContext)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                    .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                    .build();
        }

        return scanContext;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceManager.disconnect();
        deviceManager = null;
        ButterKnife.unbind(this);
        finish();
    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {

    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
        Log.i("BaeconsScanActivity", "onEvent()");
        switch (bluetoothDeviceEvent.getEventType()) {
            case DEVICES_UPDATE:
                onDevicesUpdateEvent(bluetoothDeviceEvent);
                break;
        }

    }

    /**
     * On devices update event.
     *
     * @param event the event
     */
    public void onDevicesUpdateEvent(BluetoothDeviceEvent event) {

        DeviceProfile deviceProfile = event.getDeviceProfile();
        switch (deviceProfile) {
            case IBEACON:
                onIBeaconDevicesList((IBeaconDeviceEvent) event);
                break;
            case EDDYSTONE:
                onEddystoneDevicesList((EddystoneDeviceEvent) event);
                break;
        }
    }

    private void onEddystoneDevicesList(final EddystoneDeviceEvent event) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {*/

        beaconsAdapter.replaceEddystoneBeacons(event.getDeviceList());
        /* }
        });*/
    }

    private void onIBeaconDevicesList(final IBeaconDeviceEvent event) {
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {*/
        beaconsAdapter.replaceIBeacons(event.getDeviceList());
            /*}
        });*/
    }


}
