package com.northteam.beaconsscanner.ui.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.EddystoneDetailsScan;
import com.northteam.beaconsscanner.adapter.monitor.IBeaconsDetailsScan;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DistanceRangeActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG = "DistanceRangeActivity";

    @Bind(R.id.distance_range)
    TextView distanceRangeTextView;

    public static Context context;


    IEddystoneDevice eddystone;
    IBeaconDevice ibeacon;
    IBeaconsDetailsScan ibeaconScan;
    EddystoneDetailsScan eddystoneScan;

    public static Context getContext() {
        return context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance_range);

        context = this;

        ButterKnife.bind(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            distanceRangeTextView.setText(Html.fromHtml("<b>Distância:</b> &nbsp;&nbsp;<i>a calibrar . . .</i>"));


            // Recebe da atividade anterior como parâmetro o dispositivo selecionado
            if (extras.get("EDDYSTONE") != null) {
                eddystone = (IEddystoneDevice) extras.get("EDDYSTONE");


                eddystoneScan = new EddystoneDetailsScan(this, eddystone.getInstanceId());
                eddystoneScan.startScan(DistanceRangeActivity.this);
            } else {
                ibeacon = (IBeaconDevice) extras.get("IBEACON");

                ibeaconScan = new IBeaconsDetailsScan(this, ibeacon.getName());
                ibeaconScan.startScan(DistanceRangeActivity.this);
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

    @Override
    protected void onResume() {
        super.onResume();

        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        } else {
            if (eddystone != null)
                eddystoneScan.startScan(DistanceRangeActivity.this);
            else if (ibeacon != null)
                ibeaconScan.startScan(DistanceRangeActivity.this);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (eddystone != null)
            eddystoneScan.deviceManager.finishScan();
        else if (ibeacon != null)
            ibeaconScan.deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eddystone != null)
            eddystoneScan.deviceManager.finishScan();
        else if (ibeacon != null)
            ibeaconScan.deviceManager.finishScan();
        ButterKnife.unbind(this);
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
                if (eddystone != null)
                    eddystoneScan.onDevicesUpdateEvent(bluetoothDeviceEvent);
                else
                    ibeaconScan.onDevicesUpdateEvent(bluetoothDeviceEvent);
                break;
        }

    }
}
