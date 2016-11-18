package com.northteam.beaconsscanner.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by beatrizgomes on 09/05/16.
 */
public class Calculate {

    private String TAG;

    // Variables used in the Moving Average Method
    private int auxMovingAverage = 0;
    private double startTime = 0;
    private double timeBetweenTwoBeacons = 0;
    private double movingAverageRssi = 0.0;
    private static int mps = 2;

    // Variables used in the Mode Method
    /**
     * The ArrayList that stores the rssi mode.
     */
    public ArrayList<Double> rssiMode = new ArrayList<>();

    /**
     * The ArrayList that stores the rssy array.
     */
    public ArrayList<Double> rssiArray = new ArrayList<>();

    /**
     *
     */
    public ArrayList<Double> rssiMovingAverage = new ArrayList<>();

    /**
     * number of elements for the moving average
     */
    private static int numElementsMovingAverage = 3;

    /**
     * The Count.
     */
    private int count = 0; // variavel usada no metodo calculateDistance();


    public Calculate(String TAG) {
        this.TAG = TAG;
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
                return accuracy;
            }
        }
    }

    /** SUAVIZATION WITH MOVING AVERAGE **/

    /**
     *
     * @param rssi the received rssi
     * @return
     */
    public double rssiSuavizationMovingAverage(double rssi, int txPower) {

        Calendar c = Calendar.getInstance();
        //auxMovingAverage - if firt time
        if (auxMovingAverage == 0) {
            startTime = c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
            auxMovingAverage = 1;
        } else {
            timeBetweenTwoBeacons = c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND) - startTime;
        }
        // if the time between two events is 0
        // accept the user moves 0,5m
        // 0.75 is 3/4s, user moves two meters per second so we accept a moves 1,50cm.
        if (timeBetweenTwoBeacons < 1)
            timeBetweenTwoBeacons = 0.75;

        //distMax it is the maximum distance that the user can walk in a time period
        double distMax = (timeBetweenTwoBeacons * mps) * 100;

        Log.d(TAG, "Time:" + timeBetweenTwoBeacons);
        Log.d(TAG, "distMax: " + distMax);

        //distance it is a distance calculate whith rssi recived
        double distance = getDistance(rssi, txPower);
        //distInterval - as is the user in theory moved
        double distInterval = Math.abs(getMovingAverageRssi() - distance);
        Log.d(TAG, "DistInterval: " + distInterval);
        //if plausible
        if (distInterval < distMax) {
            if (rssiMovingAverage.size() < numElementsMovingAverage) {
                rssiMovingAverage.add(rssi);
            } else {
                rssiMovingAverage.remove(0);
                rssiMovingAverage.add(rssi);
            }
            //Updates time for the new entry in the arrayList
            startTime = c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
        } else
            Log.d(TAG, "discard");

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

    public double getMovingAverageRssi() {
        return movingAverageRssi;
    }
}
