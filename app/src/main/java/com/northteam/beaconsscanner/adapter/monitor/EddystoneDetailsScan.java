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
import com.northteam.beaconsscanner.ui.activity.EddystoneDetailsActivity;

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
 * Beacons Scanner - EddystoneDetailsScan, file created on 08/04/2016
 *
 * @author beatrizgomes
 * @author andrepinto
 */
public class EddystoneDetailsScan {

    private static final String TAG = "EddystoneDetailsScan";


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

    private ArrayList<Double> rssiAverage = new ArrayList<>();
    /**
     * The Distance.
     */
    //public double distance;
    /**
     * The Rssi mode.
     */
    public ArrayList<Double> rssiMode = new ArrayList<>();
    /**
     * The Rssi array.
     */
    public ArrayList<Double> rssiArray = new ArrayList<>();

    /**
     * number of elements for the moving average
     */
    private static int numElementsMovingAverage = 5;
    /**
     *
     */
    public ArrayList<Double> rssiMovingAverage = new ArrayList<>();

    int auxMovingAverage = 0;
    double startTime = 0;
    double timeBetweenTwoBeacons = 0;
    double movingAverageRssi = 0.0;
    /**
     * The Count.
     */
    int count = 0; // variavel usada no metodo calculateDistance();

    private String fileName = null;
    private boolean markingLog = false;
    private Context context;
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
    private List<EventType> eventTypes = new ArrayList<EventType>() {{
        add(EventType.DEVICES_UPDATE);
    }};

    /**
     * Instantiates a new Eddystone details scan.
     *
     * @param context the context
     */
    public EddystoneDetailsScan(Context context) {

        this.context = context;
        deviceManager = new ProximityManager(context);

    }


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


        double distance;

        for (IEddystoneDevice eddystoneDevice : eddystoneDevices) {

            timerCount.cancel();
            timerCount.start();

            //distance = calculateDistance(eddystoneDevice.getTxPower(), eddystoneDevice.getRssi());
            distance = calculateDistance(eddystoneDevice.getTxPower(), eddystoneDevice.getRssi());

            if (context == EddystoneDetailsActivity.getContext()) {


                distanceTextView.setText(Html.fromHtml("<b>" + context.getString(R.string.distance) + ":</b>&nbsp;&nbsp;"));

                String receivedRssi = "";
                String suavizedRssi = "";
                String folderName = "Eddystone";


                if (distance == -1)
                    distanceTextView.append(Html.fromHtml("<i>" + context.getString(R.string.calibrating) + "...</i>"));
                else {
                    distanceTextView.append(String.format("%.2f cm", distance));
                    receivedRssi = String.format("%.2f", eddystoneDevice.getRssi());
                    //suavizedRssi = String.format("%.2f", rssiSuavizationMode(eddystoneDevice.getRssi()));
                    suavizedRssi = String.format("%.2f", rssiSuavizationMovingAverage(eddystoneDevice.getRssi(), eddystoneDevice.getTxPower()));
                }

                rssiTextView.setText(Html.fromHtml("<b>RSSI:</b> &nbsp;&nbsp;"));
                rssiTextView.append(String.format("%.2f dBm", eddystoneDevice.getRssi()));

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
                        if (markingLog)
                            buf.append(";YES");
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
     * @param rssi
     * @param txPower
     * @return
     */
    public double getDistance(Double rssi, int txPower) {

        if (rssi == 0.0) {

            return -1.0; // if we cannot determine distance, return -1.
        } else {

            double ratio = rssi * 1.0 / txPower;
            if (ratio < 1.0) {
                return Math.pow(ratio, 10);


            } else {
                double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;

                return accuracy;
            }
        }


    }


    /**
     * Calculates the suavized distance
     *
     * @param txPower
     * @param receivedRssi
     * @return the calculated distance
     */
    public double calculateDistance(int txPower, double receivedRssi) {


        Log.i(TAG, "-----------------------");
        Log.i(TAG, "calculateDistance(): receivedRssi: " + receivedRssi);


        //double rssi = rssiSuavizationMode(receivedRssi);
        double rssi = rssiSuavizationMovingAverage(receivedRssi, txPower);
        movingAverageRssi = rssi;
        Log.i(TAG, "calculateDistance(): rssi suavizado: " + rssi);

        if (rssi == 0.0) {
            //distanceTextView.setText(Html.fromHtml("<b>Distância:</b>&nbsp;&nbsp;a calibrar..."));

            return -1.0; // if we cannot determine distance, return -1.
        } else {

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
    }

/** SUAVIZATION WITH MOVING AVERAGE **/

    /**
     * @param rssi the received rssi
     * @return
     */
    public double rssiSuavizationMovingAverage(double rssi, int txPower) {

        Calendar c = Calendar.getInstance();
        double currentTime = 0.0;
        if (auxMovingAverage == 0) {
            startTime = c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
            auxMovingAverage = 1;
        } else {

            timeBetweenTwoBeacons = c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND) - startTime;


        }

        if (timeBetweenTwoBeacons < 1)
            timeBetweenTwoBeacons = 0.15;
        // 2 metros por sec.
        double distMax = (timeBetweenTwoBeacons * 2) * 100;
        Log.d(TAG, "Time:" + timeBetweenTwoBeacons);
        Log.d(TAG, "distMax: " + distMax);

        double dist = getDistance(rssi, txPower);

        double distInterval = Math.abs(movingAverageRssi - dist);
        Log.d(TAG, "DistInterval: " + distInterval);
        if (distInterval < distMax) {
            if (rssiMovingAverage.size() < numElementsMovingAverage) {
                rssiMovingAverage.add(rssi);
            } else {
                rssiMovingAverage.remove(0);
                rssiMovingAverage.add(rssi);
            }
            startTime = c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);

        }

        return averageRssi(1);
    }


    /** SUAVIZATION WITH MODE **/

    /**
     * Rssi suavization double.
     *
     * @param rssi Parametro obtido do metodo calculateDistance(int txPower, double receivedRssi), que representa o RSSI lido.
     * @return Retorna a media dos RSSI's
     */
    public double rssiSuavizationMode(double rssi) {

        double variation = 0, modeValue = 0;

        if (rssiMode.size() < 15) {
            // Preencher o ArrayList rssiMode() com 15 valores sem qualquer filtro.
            rssiMode.add(rssi);
            count = 0;
        } else { // rssiMode() cheio.

            // modeValue fica com a MODA dos valores de rssi obtidos.
            modeValue = mode();

            Log.i("EddystoneDetailsScan", "rssiSuavizationMode(): moda: " + modeValue);


            // variation fica com a diferença entre o rssi obtido com a MODA dos Rssi's
            variation = Math.abs(rssi - modeValue);

            // Janela de valores aceites a serem considerados para o calculo da nova MODA.
            if (variation >= 0 && variation <= 5) {
                count = 0;
                rssiMode.remove(0);
                rssiMode.add(rssi);
            } else {
                Log.i("EddystoneDetailsScan", "rssiSuavizationMode(): receivedRssi > moda_variation (" + variation + ")");
                // Se o rssi for descartado incrementamos a variavel count.
                count++;
                Log.i("EddystoneDetailsScan", "rssiSuavizationMode(): count++");
            }

            // modeValue fica com a nova MODA
            modeValue = mode();

            // variation fica com a diferença entre o rssi obtido com a nova MODA dos Rssi's
            variation = Math.abs(rssi - modeValue);

            // Janela de valores aceites a serem considerados para o calculo da Media dos Rssi's.
            if (variation >= 0 && variation <= 4) {
                if (rssiArray.size() >= 20)
                    rssiArray.remove(0);

                rssiArray.add(rssi);
            } else
                Log.i("EddystoneDetailsScan", "rssiSuavizationMode(): receivedRssi > media_variation (" + variation + ")");
        }
        /**
         * Se count == 5, significa que 5 rssi seguidos foram descartados.
         * É muito provavél que nos estejamos a deslocar e que estejamos a descartar valores importantes.
         * Vamos retirar metade dos valores dos ArrayList's de maneira a deixar entrar novos valores para o calculo da nova
         * MODA e Média.
         */
        if (count == 5) {
            //Log.i("EddystoneDetailsScan", "rssiSuavizationMode(): Count =  5 ");
            for (int i = 0; i < 9; i++) {
                rssiMode.remove(0);
                if (rssiArray.size() >= 12)
                    rssiArray.remove(0);
            }
        }

        //Log.i("EddystoneDetailsScan", "rssiSuavizationMode(): mode: " + modeValue);

        // Retorna a média dos Rssi's, valor que irá ser usado para o calculo da distancia.
        return averageRssi(0);
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
     * @return um double que representa a Média do conjunto de valores presente no ArrayList<Double> rssiArray.
     */
    public double averageRssi(int id) {
        double average = 0.0;
        double sum = 0;

        switch (id) {
            // Method with mode
            case 0:
                if (rssiArray.size() == 0)
                    return 0.0;

                for (double val : rssiArray) {
                    sum += val;
                }
                average = sum / rssiArray.size();
                break;
            // Method with moving average
            case 1:
                if (rssiMovingAverage.size() == 0)
                    return 0.0;

                for (double val : rssiMovingAverage) {
                    sum += val;
                }
                average = sum / rssiMovingAverage.size();
                break;
        }

        return average;

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


