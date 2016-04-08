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
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.EddystoneDetailsScan;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EddystoneDetailsActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    /**
     * The constant context.
     */
    public static Context context;
    /**
     * The Tx power text view.
     */
    @Bind(R.id.txpower_level)
    public TextView txPowerTextView;
    @Bind(R.id.namespace)
    public TextView namespaceTextView;
    @Bind(R.id.instance_id)
    public TextView instaceTextView;
    @Bind(R.id.eddystone_rssi)
    public TextView rssiTextView;
    @Bind(R.id.eddystone_distance)
    public TextView distanceTextView;

    //@Bind(R.id.eddystone_proximity)
    //public TextView proximityTextView;
    @Bind(R.id.battery_voltage)
    public TextView batteryTextView;
    @Bind(R.id.eddystone_url)
    public TextView urlTextView;

    //@Bind(R.id.eddystone_temperature)
    //public TextView temperatureTextView;
    public ProximityManager deviceManager;
    public String beaconIdentifier;
    public double distance;
    String TAG = "EddyStoneDetailsActivity";
    /**
     * The Eddystone scan.
     */

    EddystoneDetailsScan eddystoneScan;

    /**
     * The Eddystone.
     */
    IEddystoneDevice eddystone;
    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    /**
     * Gets context.
     *
     * @return the context
     */
    public static Context getContext() {
        return context;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eddystone_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            // Recebe da atividade anterior como parâmetro o dispositivo selecionado
            eddystone = (IEddystoneDevice) extras.get("EDDYSTONE");
            //beaconScan = new EddystoneDetailsScan(this, eddystone.getInstanceId());
            //nameTextView.setText(Html.fromHtml("<b>Nome:</b> &nbsp;&nbsp;" + eddystone.getNamespaceId()));
            distanceTextView.setText(Html.fromHtml("<b>Distância:</b>&nbsp;&nbsp;<i>a calibrar...</i>"));

            namespaceTextView.setText(Html.fromHtml("<b>Namespace:</b> &nbsp;&nbsp;" + eddystone.getNamespaceId()));
            instaceTextView.setText(Html.fromHtml("<b>Instance:</b> &nbsp;&nbsp;" + eddystone.getInstanceId()));
            rssiTextView.setText(Html.fromHtml("<b>RSSI:</b> &nbsp;&nbsp;"));
            rssiTextView.append(String.format("%.2f dBm", eddystone.getRssi()));
            txPowerTextView.setText(Html.fromHtml("<b>Tx Power:</b> &nbsp;&nbsp;" + eddystone.getTxPower()));
            batteryTextView.setText(Html.fromHtml("<b>Bateria:</b> &nbsp;&nbsp;" + eddystone.getBatteryVoltage() + "V"));
            //temperatureTextView.setText(Html.fromHtml("<b>Temperatura:</b> &nbsp;&nbsp;" + eddystone.getTemperature() + "ºC"));
            urlTextView.setText(Html.fromHtml("<b>Url:</b> &nbsp;&nbsp;" + eddystone.getUrl()));

            beaconIdentifier = eddystone.getInstanceId();
            eddystoneScan = new EddystoneDetailsScan(context, beaconIdentifier);


            Log.i("EddystoneDetails", "OnCreate(): ");

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        switch (bluetoothDeviceEvent.getEventType()) {
            case DEVICES_UPDATE:
                eddystoneScan.onDevicesUpdateEvent(bluetoothDeviceEvent);
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

        eddystoneScan.startScan(EddystoneDetailsActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        eddystoneScan.deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eddystoneScan.deviceManager.disconnect();
        eddystoneScan.deviceManager = null;
        ButterKnife.unbind(this);
    }

}
