package com.northteam.beaconsscanner.adapter.monitor;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.DeviceProfile;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.ibeacon.IBeaconDeviceEvent;
import com.kontakt.sdk.android.ble.filter.ibeacon.IBeaconFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.ui.activity.IBeaconDetailsActivity;
import com.northteam.beaconsscanner.ui.activity.RealtimeLineChartActivity;
import com.northteam.beaconsscanner.util.Calculate;
import com.northteam.beaconsscanner.util.LogFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Beacons Scanner - IBeaconDetailsScan, file created on 08/04/2016
 *
 * @author beatrizgomes
 * @author andrepinto
 */
public class IBeaconsDetailsScan {


    /**
     * The Device manager.
     */
    public ProximityManager deviceManager;
    /**
     * The Scan context.
     */
    public ScanContext scanContext;
    /**
     * The Beacon identifier.
     */
    public String beaconIdentifier;
    public String namespaceIdentifier;

    private LogFile lf = new LogFile("IBeacon");

    /* CHART */
    private LineChart mChart;
    private FeedChart feedChart;


    /**
     * The Context
     */
    private Context context;

    private String fileName = null;

    private boolean markingLog = false;

    private Calculate calc = new Calculate("IBeaconsDetailsScan");

    /**
     * timerCount to check if connection with beacon was lost
     */
    CountDownTimer timerCount = new CountDownTimer(3000, 1000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {

            if (context == IBeaconDetailsActivity.getContext()) {
                TextView rssiTextView = (TextView) ((Activity) context).findViewById(R.id.rssi);
                rssiTextView.setText(Html.fromHtml("<b>" + context.getString(R.string.rssi) + ":</b> &nbsp;&nbsp;<i>" + context.getString(R.string.noSignal) + "</i>"));
            }
        }
    };
    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    /**
     * Instantiates a new Beacons details scan.
     *
     * @param context    the context
     * @param identifier the identifier
     */
    public IBeaconsDetailsScan(Context context, String identifier) {

        this.beaconIdentifier = identifier;
        this.context = context;
        deviceManager = new ProximityManager(context);
    }

    /**
     * Instantiates a new Beacons details scan.
     *
     * @param context    the context
     * @param identifier the identifier
     * @param lc the Chart
     */
    public IBeaconsDetailsScan(Context context, String identifier, LineChart lc) {

        this.beaconIdentifier = identifier;
        this.context = context;
        deviceManager = new ProximityManager(context);
        this.feedChart = new FeedChart(lc, context);
        this.mChart = lc;
    }

    /**
     * Start scan.
     *
     * @param listener the listener
     */
    public void startScan(final ProximityManager.ProximityListener listener) {

        deviceManager.initializeScan(getOrCreateScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                deviceManager.attachListener(listener);
            }

            @Override
            public void onConnectionFailure() {

            }
        });
    }

    /**
     * Gets or creates scan context.
     *
     * @return the scan context
     */
    public ScanContext getOrCreateScanContext() {

        IBeaconScanContext beaconScanContext;

        beaconScanContext = new IBeaconScanContext.Builder()
                .setEventTypes(eventTypes) // only specified events we be called on callback
                .setIBeaconFilters(Arrays.asList(
                        IBeaconFilters.newUniqueIdFilter(beaconIdentifier)
                ))
                .setRssiCalculator(RssiCalculators.DEFAULT)
                .build();


        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setIBeaconScanContext(beaconScanContext)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.DEFAULT)
                    .setForceScanConfiguration(ForceScanConfiguration.DEFAULT)
                    .build();
        }

        return scanContext;
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
        }
    }

    /**
     * @param event
     */
    private void onIBeaconDevicesList(final IBeaconDeviceEvent event) {

        List<IBeaconDevice> iBeaconDevices = event.getDeviceList();
        TextView distanceTextView = (TextView) ((Activity) context).findViewById(R.id.distance);
        TextView rssiTextView = (TextView) ((Activity) context).findViewById(R.id.rssi);

        double distance;

        for (IBeaconDevice iBeaconDevice : iBeaconDevices) {

            timerCount.cancel();
            timerCount.start();

            distance = calc.calculateDistance(iBeaconDevice.getTxPower(), iBeaconDevice.getRssi());


            if (context == IBeaconDetailsActivity.getContext()) {

                distanceTextView.setText(Html.fromHtml("<b>" + context.getString(R.string.distance) + ":</b> &nbsp;&nbsp; "));

                String receivedRssi = "";
                String suavizedRssi = "";
                String folderName = "IBeacon";

                if (distance == -1)
                    distanceTextView.append(Html.fromHtml("<i>" + context.getString(R.string.calibrating) + "...</i>"));
                else {
                    distanceTextView.append(String.format("%.2f m", distance));
                    receivedRssi = String.format("%.2f", iBeaconDevice.getRssi());
                    suavizedRssi = String.format("%.2f", calc.getMovingAverageRssi());
                }

                rssiTextView.setText(Html.fromHtml("<b>RSSI:</b> &nbsp;&nbsp;"));
                rssiTextView.append(String.format("%.2f dBm", iBeaconDevice.getRssi()));

                if (fileName != null && distance != -1) {
                    /*
                    File dir = Environment.getExternalStorageDirectory();

                    File logFile = new File(dir + "/Beacons Scanner/" + folderName + "/" + fileName);

                    try {
                        //BufferedWriter for performance, true to set append to file flag
                        BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                        Calendar c = Calendar.getInstance();
                        String date = c.get(Calendar.YEAR) + "/" + String.format("%02d", c.get(Calendar.MONTH) + 1) + "/" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + ";" + c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND) + ";";
                        buf.append(date);
                        buf.append(receivedRssi + ";");
                        buf.append(suavizedRssi + ";");
                        buf.append(String.format("%.2f", distance));
                        if (isMarkingLog())
                            buf.append(";YES");
                        buf.newLine();
                        buf.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } */
                    lf.saveLogToFile(fileName, receivedRssi, suavizedRssi, distance, markingLog, iBeaconDevice.getDistance());
                }

            } else if (context == RealtimeLineChartActivity.getContext()) {
                if (distance != -1)
                    feedChart.addEntry(calc.getMovingAverageRssi(), iBeaconDevice.getRssi());
                if (fileName != null && distance != -1) {
                    lf.saveLogToFile(fileName, String.valueOf(iBeaconDevice.getRssi()), String.valueOf(calc.getMovingAverageRssi()), distance, markingLog, iBeaconDevice.getDistance());

                }
            }


        }

    }



    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isMarkingLog() {
        return markingLog;
    }

    public void setMarkingLog(boolean markingLog) {
        this.markingLog = markingLog;
    }
}


