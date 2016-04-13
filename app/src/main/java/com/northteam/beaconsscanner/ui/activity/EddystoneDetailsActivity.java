package com.northteam.beaconsscanner.ui.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.EddystoneDetailsScan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EddystoneDetailsActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 3;

    private static final String TAG = "EddyStoneDetailsActivity";

    /**
     * The constant context.
     */
    public static Context context;

    public String fileName;
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
        toolbar.setTitle(R.string.app_name);

        setSupportActionBar(toolbar);

        context = this;

        ButterKnife.bind(this);

        final Bundle extras = getIntent().getExtras();
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


            Log.i(TAG, "OnCreate(): ");

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (extras != null) {
                    Intent intentRangeActivity = new Intent(EddystoneDetailsActivity.this, DistanceRangeActivity.class);
                    intentRangeActivity.putExtra("EDDYSTONE", eddystone);
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
                    eddystoneScan.startScan(EddystoneDetailsActivity.this);

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
            case PERMISSION_REQUEST_WRITE_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "read storage permission granted");
                    String folderName = "Eddystone";
                    File directory = new File(Environment.getExternalStorageDirectory() + "/Beacons Scanner", folderName);
                    if (!directory.exists()) {
                        try {
                            if (directory.mkdir()) {
                                System.out.println("Directory created");
                            } else {
                                System.out.println("Directory is not created");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    // Alterar texto
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

    //@OnClick({R.id.imageButton_save_log, R.id.textView_save_log})
    @OnClick(R.id.imageButton_save_log)
    public void startSaving() {
        // TODO ...
        Log.i(TAG, "onClick()");
        String folderName = "Eddystone";
        //File dir = Environment.getExternalStorageDirectory();

        Calendar c = Calendar.getInstance();
        fileName = c.get(Calendar.YEAR) + "" + String.format("%02d", c.get(Calendar.MONTH) + 1) + "" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + c.get(Calendar.MINUTE) + ".csv";

        File directory = new File(Environment.getExternalStorageDirectory() + "/Beacons Scanner", folderName);
        System.out.println(directory.getAbsolutePath());
        if (!directory.exists()) {
            try {
                if (directory.mkdir()) {
                    System.out.println("Directory created");
                } else {
                    System.out.println("Directory is not created");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File logFile = new File(directory + "/" + fileName);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append("Date; Time; Received RSSI; Suavized RSSI");
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ImageButton imgBtnSaveLog = (ImageButton) findViewById(R.id.imageButton_save_log);
        ImageButton imgBtnStopSaveLog = (ImageButton) findViewById(R.id.imageButton_stop_save_log);

        if (imgBtnSaveLog.getVisibility() == View.VISIBLE) {
            imgBtnSaveLog.setVisibility(View.INVISIBLE);

            imgBtnStopSaveLog.setVisibility(View.VISIBLE);

            eddystoneScan.setFileName(fileName);

        }


    }

    @OnClick(R.id.imageButton_stop_save_log)
    public void stopSaving() {

        ImageButton imgBtnSaveLog = (ImageButton) findViewById(R.id.imageButton_save_log);
        ImageButton imgBtnStopSaveLog = (ImageButton) findViewById(R.id.imageButton_stop_save_log);

        if (imgBtnStopSaveLog.getVisibility() == View.VISIBLE) {
            imgBtnSaveLog.setVisibility(View.VISIBLE);

            imgBtnStopSaveLog.setVisibility(View.INVISIBLE);

            eddystoneScan.setFileName(null);

        }
    }

}
