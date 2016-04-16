package com.northteam.beaconsscanner.ui.activity;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.IBeaconsDetailsScan;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class IBeaconDetailsActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 3;

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

    public String fileName;


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
            nameTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.device_name) + ":</b> &nbsp;&nbsp;" + ibeacon.getUniqueId()));
            distanceTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.distance) + ":</b> &nbsp;&nbsp; <i>" + this.getString(R.string.calibrating) + "...</i>"));
            majorTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.major) + ":</b> &nbsp;&nbsp;" + ibeacon.getMajor()));
            minorTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.minor) + ":</b> &nbsp;&nbsp;" + ibeacon.getMinor()));
            rssiTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.rssi) + ":</b> &nbsp;&nbsp;" + ibeacon.getRssi() + " dBm"));
            txPowerTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.power) + ":</b> &nbsp;&nbsp;" + ibeacon.getTxPower()));
            batteryTextView.setText(Html.fromHtml("<b>" + this.getString(R.string.battery) + ":</b> &nbsp;&nbsp;" + ibeacon.getBatteryPower() + "%"));
            beaconScan = new IBeaconsDetailsScan(context, ibeacon.getUniqueId());
            //beaconScan.startScan(this);


        }

        Log.i(TAG, "OnCreate(): ");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.limitedPermissions);
                    builder.setMessage(R.string.noBluetoothPermissions);
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
                    String folderName = "IBeacon";
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
                    builder.setTitle(R.string.limitedPermissions);
                    // Alterar texto
                    builder.setMessage(R.string.noStoragePermissions);
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

    @OnClick(R.id.imageButton_save_log_ibeacon)
    public void startSaving() {
        // TODO ...
        Log.i(TAG, "onClick()");
        String folderName = "IBeacon";

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

        ImageButton imgBtnSaveLog = (ImageButton) findViewById(R.id.imageButton_save_log_ibeacon);
        ImageButton imgBtnStopSaveLog = (ImageButton) findViewById(R.id.imageButton_stop_save_log_ibeacon);
        TextView txtSave = (TextView) findViewById(R.id.textView_save_log_ibeacon);


        if (imgBtnSaveLog.getVisibility() == View.VISIBLE) {
            imgBtnSaveLog.setVisibility(View.INVISIBLE);
            txtSave.setText(R.string.stop_save_log_file);

            imgBtnStopSaveLog.setVisibility(View.VISIBLE);

            beaconScan.setFileName(fileName);

        }


    }

    @OnClick(R.id.imageButton_stop_save_log_ibeacon)
    public void stopSaving() {

        ImageButton imgBtnSaveLog = (ImageButton) findViewById(R.id.imageButton_save_log_ibeacon);
        ImageButton imgBtnStopSaveLog = (ImageButton) findViewById(R.id.imageButton_stop_save_log_ibeacon);

        if (imgBtnStopSaveLog.getVisibility() == View.VISIBLE) {
            imgBtnSaveLog.setVisibility(View.VISIBLE);

            imgBtnStopSaveLog.setVisibility(View.INVISIBLE);

            beaconScan.setFileName(null);

            LayoutInflater inflater = getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_layout));
            ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
            imgCSV.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
            TextView text = (TextView) layout.findViewById(R.id.textToShow);
            // Set the Text to show in TextView
            text.setText(R.string.fileSavedIbeacon);

            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 400);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    }
}
