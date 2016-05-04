package com.northteam.beaconsscanner.adapter.monitor;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

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

    private static final String TAG = "IBeaconDetailsScan";


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
    /**
     * The Rssi mode - array used to save the received RSSI and then determine the Mode of the values.
     */
    public ArrayList<Double> rssiMode = new ArrayList<>();
    /**
     * The Rssi array - array used to save the received RSSI - if allowed (x > mode < y)  .
     */
    public ArrayList<Double> rssiArray = new ArrayList<>();
    /**
     * The Count - used to count how manny times a RSSI signal is throwed out.
     */
    int count = 0;
    /**
     * The Context
     */
    private Context context;

    private String fileName = null;

    private boolean markingLog = false;

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
     * @param context the context
     */
    public IBeaconsDetailsScan(Context context) {

        this.context = context;
        deviceManager = new ProximityManager(context);

    }


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

            distance = calculateDistance(iBeaconDevice.getTxPower(), iBeaconDevice.getRssi());


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
                    suavizedRssi = String.format("%.2f", rssiSuavization(iBeaconDevice.getRssi()));
                }

                rssiTextView.setText(Html.fromHtml("<b>RSSI:</b> &nbsp;&nbsp;"));
                rssiTextView.append(String.format("%.2f dBm", iBeaconDevice.getRssi()));

                if (fileName != null && distance != -1) {
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
                            buf.append(";*");
                        buf.newLine();
                        buf.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }

        }

    }

    /**
     * Calculate distance.
     *
     * @param txPower      the tx power
     * @param receivedRssi the received rssi
     */
    public double calculateDistance(int txPower, double receivedRssi) {

        Log.i(TAG, "-----------------------");


        Log.i(TAG, "calculateDistance(): receivedRssi: " + receivedRssi);

        double rssi = rssiSuavization(receivedRssi);
        Log.i(TAG, "calculateDistance(): rssi suavizado: " + rssi);

        if (rssi == 0) {
            //distanceTextView.setText(Html.fromHtml("<b>Distância:</b> &nbsp;&nbsp;"));
            //distanceTextView.append(String.format("a calibrar . . ."));
            return -1.0; // if we cannot determine distance, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
            //distanceTextView.setText(Html.fromHtml("<b>Distância:</b> &nbsp;&nbsp;"));
            //distanceTextView.append(String.format("%.2f cm", Math.pow(ratio, 10)));

        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            //distanceTextView.setText(Html.fromHtml("<b>Distância:</b> &nbsp;&nbsp;"));
            //distanceTextView.append(String.format("%.2f cm", accuracy));
            return accuracy;
        }
    }

    /**
     * Rssi suavization double.
     *
     * @param rssi Parametro obtido do metodo calculateDistance(int txPower, double receivedRssi), que representa o RSSI lido.
     * @return Retorna a media dos RSSI's
     */
    public double rssiSuavization(double rssi) {

        double variation = 0, modeValue = 0;

        if (rssiMode.size() < 15) {

            // Preencher o ArrayList rssiMode() com 15 valores sem qualquer filtro.
            rssiMode.add(rssi);
            count = 0;
        } else { // rssiMode() cheio.

            // modeValue fica com a MODA dos valores de rssi obtidos.
            modeValue = mode();

            // variation fica com a diferença entre o rssi obtido com a MODA dos Rssi's
            variation = Math.abs(rssi) - Math.abs(modeValue);

            // Janela de valores aceites a serem considerados para o calculo da nova MODA.
            if (variation >= 0 && variation <= 5) {
                count = 0;
                rssiMode.remove(0);
                rssiMode.add(rssi);
            } else {
                // Se o rssi for descartado a variável count é incrementada.
                count++;
            }

            // modeValue fica com a nova MODA
            modeValue = mode();

            // variation fica com a diferença entre o rssi obtido com a nova MODA dos Rssi's
            variation = Math.abs(rssi) - Math.abs(modeValue);

            // Janela de valores aceites a serem considerados para o calculo da Media dos Rssi's.
            if (variation >= 0 && variation <= 3) {
                if (rssiArray.size() >= 20)
                    rssiArray.remove(0);

                rssiArray.add(rssi);
            }
        }

        /**
         * Se count == 5, significa que 5 rssi seguidos foram descartados.
         * É muito provável que nos estejamos a deslocar e que estejamos a descartar valores importantes.
         * Vamos retirar metade dos valores dos ArrayList's de maneira a deixar entrar novos valores para o cálculo da nova
         * MODA e Média.
         */
        if (count == 5) {
            Log.i(TAG, "rssiSuavizationMode(): Count =  5 ");
            for (int i = 0; i < 9; i++) {
                rssiMode.remove(0);
                if (rssiArray.size() >= 12)
                    rssiArray.remove(0);
            }
        }

        Log.i(TAG, "rssiSuavizationMode(): mode: " + modeValue);

        // Retorna a média dos Rssi's, valor que irá ser usado para o calculo da distancia.
        return averageRssi();
    }

    /**
     * public double mode() {
     *
     * @return Retorna um double que representa a MODA do conjunto de valores presente no ArrayList<Double> rssiMode.
     */
    public double mode() {
        HashMap<Double, Integer> hm = new HashMap<>();
        double temp = rssiMode.get(rssiMode.size() - 1);
        int count = 0, max = 1;
        for (int i = 0; i < rssiMode.size(); i++) {
            if (hm.get(rssiMode.get(i)) != null) {
                count = hm.get(rssiMode.get(i));
                count++;
                hm.put(rssiMode.get(i), count);
                if (count > max) {
                    max = count;
                    temp = rssiMode.get(i);
                }
            } else {
                hm.put(rssiMode.get(i), 1);
            }
        }
        return temp;
    }

    /**
     * public double averageRssi() {
     *
     * @return Retorna um double que representa a Média do conjunto de valores presente no ArrayList<Double> rssiArray.
     */
    public double averageRssi() {

        if (rssiArray.size() == 0)
            return 0.0;
        double sum = 0;

        for (double val : rssiArray) {
            sum += val;
        }

        return sum / rssiArray.size();
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


