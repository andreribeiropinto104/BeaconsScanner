package com.northteam.beaconsscanner.adapter.monitor;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.DeviceProfile;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.discovery.eddystone.EddystoneDeviceEvent;
import com.kontakt.sdk.android.ble.filter.eddystone.EddystoneFilters;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.ui.activity.CalibrationActivity;
import com.northteam.beaconsscanner.ui.activity.EddystoneDetailsActivity;
import com.northteam.beaconsscanner.util.Calculate;
import com.northteam.beaconsscanner.ui.activity.RealtimeLineChartActivity;
import com.northteam.beaconsscanner.util.LogFile;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Beacons Scanner - EddystoneDetailsScan, file created on 08/04/2016
 *
 * @author beatrizgomes
 * @author andrepinto
 */
public class EddystoneDetailsScan {

    private static final String TAG = "EddystoneDetailsScan";


    ArrayList<Double> x = new ArrayList();
    ArrayList<Double> y = new ArrayList();
    ArrayList<Double> rssisVal = new ArrayList();

    private double mode;


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


    private Calculate calc = new Calculate("EddystoneDetailsScan");

    /* CHART */
    private LineChart mChart;
    private FeedChart feedChart;


    private LogFile lf = new LogFile("Eddystone");
    private String fileName = null;
    private boolean markingLog = false;
    private Context context;
    private float numOfEvents = 0;
    private int numTotalOfEvents = 0;

    CountDownTimer timerCount = new CountDownTimer(5000, 1000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            if (context == EddystoneDetailsActivity.getContext()) {
                TextView rssiTextView = (TextView) ((Activity) context).findViewById(R.id.eddystone_rssi);
                rssiTextView.setText(Html.fromHtml("<b>" + context.getString(R.string.rssi) + ":</b> &nbsp;&nbsp;<i>" + context.getString(R.string.noSignal) + "...</i>"));
            }
        }
    };

    CountDownTimer timerDataRate = new CountDownTimer(10000, 1000) {

        public void onTick(long millisUntilFinished) {

        }

        public void onFinish() {
            if (context == EddystoneDetailsActivity.getContext()) {
                TextView dataRateTextView = (TextView) ((Activity) context).findViewById(R.id.eddystone_data_rate);
                dataRateTextView.setText(Html.fromHtml("<b>" + context.getString(R.string.data_rate) + ":</b> &nbsp;&nbsp;<i>" + numOfEvents/10 + " rssi/s</i>"));
            }
            timerDataRate.cancel();
            numOfEvents=0;
            timerDataRate.start();
        }
    };
    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    /**
     * Instantiates a new Eddystone details scan.
     *
     * @param context    the context
     * @param identifier the identifier
     */
    public EddystoneDetailsScan(Context context, String identifier, String namespace) {

        this.beaconIdentifier = identifier;
        this.namespaceIdentifier = namespace;
        this.context = context;
        deviceManager = new ProximityManager(context);

    }

    /**
     * @param context
     * @param identifier
     * @param namespace
     * @param lc
     */
    public EddystoneDetailsScan(Context context, String identifier, String namespace, LineChart lc) {

        this.beaconIdentifier = identifier;
        this.namespaceIdentifier = namespace;
        this.context = context;
        this.mChart = lc;
        this.feedChart = new FeedChart(lc, context);

        deviceManager = new ProximityManager(context);


    }

    /**
     * Start scan.
     *
     * @param listener the listener
     */
    public void startScan(final ProximityManager.ProximityListener listener) {
        rssisVal.clear();
        numTotalOfEvents = 0;
        timerDataRate.start();
        deviceManager.initializeScan(getOrCreateScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                deviceManager.attachListener(listener);
            }

            @Override
            public void onConnectionFailure() {
                //Utils.showToast(BeaconsScanActivity.this, "Erro durante conexão");
            }
        });

    }


    /**
     * Gets or create scan context.
     *
     * @return the or create scan context
     */
    public ScanContext getOrCreateScanContext() {

        EddystoneScanContext eddystoneScanContext;

        eddystoneScanContext = new EddystoneScanContext.Builder()
                .setEventTypes(eventTypes)
                .setDevicesUpdateCallbackInterval(250)
                .setUIDFilters(Arrays.asList(
                        EddystoneFilters.newUIDFilter(namespaceIdentifier, beaconIdentifier)
                ))
                .setRssiCalculator(RssiCalculators.DEFAULT)
                .build();

        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setEddystoneScanContext(eddystoneScanContext)
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
            case EDDYSTONE:
                onEddystoneDevicesList((EddystoneDeviceEvent) event);
                break;
        }
    }

    /**
     * @param event
     */
    private void onEddystoneDevicesList(final EddystoneDeviceEvent event) {

        List<IEddystoneDevice> eddystoneDevices = event.getDeviceList();
        TextView distanceTextView = (TextView) ((Activity) context).findViewById(R.id.eddystone_distance);
        TextView rssiTextView = (TextView) ((Activity) context).findViewById(R.id.eddystone_rssi);
        TextView eventNumbersText = (TextView) ((Activity) context).findViewById(R.id.eventNumbers);

        String receivedRssi = "";
        String suavizedRssi = "";




        double distance;

        for (IEddystoneDevice eddystoneDevice : eddystoneDevices) {
            numTotalOfEvents++;
            numOfEvents++;


            if (context == EddystoneDetailsActivity.getContext()) {


                timerCount.cancel();
                timerCount.start();

                distance = calc.calculateDistance(eddystoneDevice.getTxPower(), eddystoneDevice.getRssi());

                distanceTextView.setText(Html.fromHtml("<b>" + context.getString(R.string.distance) + ":</b>&nbsp;&nbsp;"));


                String folderName = "Eddystone";


                if (distance == -1)
                    distanceTextView.append(Html.fromHtml("<i>" + context.getString(R.string.calibrating) + "...</i>"));
                else {
                    distanceTextView.append(String.format("%.2f cm", distance));
                    receivedRssi = String.format("%.2f", eddystoneDevice.getRssi());
                    suavizedRssi = String.format("%.2f", calc.getMovingAverageRssi());
                }

                rssiTextView.setText(Html.fromHtml("<b>RSSI:</b> &nbsp;&nbsp;"));
                rssiTextView.append(String.format("%.2f dBm", eddystoneDevice.getRssi()));

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
                        if (markingLog)
                            buf.append(";YES");
                        buf.newLine();
                        buf.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    */
                    lf.saveLogToFile(fileName, receivedRssi, suavizedRssi, distance, markingLog, eddystoneDevice.getDistance());

                }

            } else if (context == RealtimeLineChartActivity.getContext()) {
                distance = calc.calculateDistance(eddystoneDevice.getTxPower(), eddystoneDevice.getRssi());

                if (distance != -1) {
                    feedChart.addEntry(calc.getMovingAverageRssi(), eddystoneDevice.getRssi());
                    receivedRssi = String.format("%.2f", eddystoneDevice.getRssi());
                    suavizedRssi = String.format("%.2f", calc.getMovingAverageRssi());
                }

                if (fileName != null && distance != -1) {
                    //lf.saveLogToFile(fileName, String.valueOf(eddystoneDevice.getRssi()), String.valueOf(calc.getMovingAverageRssi()), distance, markingLog);
                    lf.saveLogToFile(fileName, receivedRssi, suavizedRssi, distance, markingLog, eddystoneDevice.getDistance());
                }
            } else if (context == CalibrationActivity.getContext()) {
                eventNumbersText.setText("Número de eventos: " + numTotalOfEvents);
                if (numTotalOfEvents < 100) {
                    Log.i(TAG, "num: " + numTotalOfEvents);
                    Log.i(TAG, "rssi: " + eddystoneDevice.getRssi());

                    rssisVal.add(eddystoneDevice.getRssi());
                } else {
                    Log.i(TAG, "finish");
                    setMode(Calculate.mode(rssisVal));
                    CalibrationActivity.teste();

                    deviceManager.finishScan();
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

    public double getMode() {
        return mode;
    }

    public void setMode(double mode) {
        this.mode = mode;
    }
}


