package com.northteam.beaconsscanner.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.discovery.EventType;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.EddystoneDetailsScan;
import com.northteam.beaconsscanner.util.Dataset;
import com.northteam.beaconsscanner.util.LogFile;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.northteam.beaconsscanner.util.PowerRegression.getPowerRegression;


public class CalibrationActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final String TAG = "CalibrationActivity";
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;

    public static Context context;
    public String beaconIdentifier;
    public String namespaceIdentifier;

    static ArrayList<Double> x = new ArrayList();
    static ArrayList<Double> y = new ArrayList();


    /**
     * The Eddystone scan.
     */
    static EddystoneDetailsScan eddystoneScan;

    /**
     * The Eddystone.
     */
    static IEddystoneDevice eddystone;
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

    /*@Bind(R.id.setDistance)
    public TextView dist;*/
    static TextView dist;
    static Button calBtn;
static TextView modeTxt;
    /*   @Bind(R.id.button)
       public Button calBtn;
   */
    static int calCount;

    static LogFile lf = new LogFile("Eddystone");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        dist = (TextView) findViewById(R.id.setDistance);
        modeTxt = (TextView) findViewById(R.id.modeTxt);
        calBtn = (Button) findViewById(R.id.button);

        context = this;

        ButterKnife.bind(this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            calCount = 0;
            y.add(1.0);
            y.add(2.0);
            y.add(3.0);
            y.add(4.0);
            y.add(5.0);
            y.add(6.0);
            y.add(7.0);
            y.add(8.0);
            y.add(9.0);
            y.add(10.0);
            y.add(12.0);
            y.add(14.0);
            y.add(16.0);
            y.add(18.0);
            /*y.add(20.0);
            y.add(25.0);
            y.add(30.0);
            y.add(40.0);*/


            dist.setText("Coloque-se a " + this.y.get(calCount) + " m");
            // Recebe da atividade anterior como parâmetro o dispositivo selecionado
            eddystone = (IEddystoneDevice) extras.get("EDDYSTONE");

            namespaceIdentifier = eddystone.getNamespaceId();
            Log.i(TAG, "name: " + namespaceIdentifier);
            beaconIdentifier = eddystone.getInstanceId();
            eddystoneScan = new EddystoneDetailsScan(context, beaconIdentifier, namespaceIdentifier);

            lf.createCalibrationFile(eddystone.getTxPower(), "kontakt");
            eddystoneScan.setFileName(lf.getFileName());

        }


        calBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                calCount++;
                eddystoneScan.startScan(CalibrationActivity.this);
                calBtn.setVisibility(View.INVISIBLE);


            }
        });

/*

        Dataset dataset = new Dataset(x, y);
        double[] result = getPowerRegression(dataset);
        System.out.println("Result[0]" + result[0]);
        System.out.println("Result[1]" + result[1]); */


    }

    @Override
    public void onScanStart() {

    }

    @Override
    public void onScanStop() {


    }

    public static void teste() {

        double racio = eddystoneScan.getMode() / eddystone.getTxPower();
        Log.i(TAG, "Racio: " + racio);
        x.add(racio);
        Log.i(TAG, "count: " + calCount);
        Log.i(TAG, "size: " + y.size());
        System.out.println("file: " + lf.getFileName());
        System.out.println("eddy: " + eddystoneScan.getFileName());
        lf.saveCalibrationToFile(eddystoneScan.getFileName(), eddystoneScan.getMode(), y.get(calCount - 1));
        modeTxt.setText("Moda obtida a " + y.get(calCount-1) + " : " +eddystoneScan.getMode());

        if (calCount == y.size()) {
            dist.setText("Calibração terminada");
            Dataset dataset = new Dataset(x, y);
            double[] result = getPowerRegression(dataset);
            System.out.println("Result[0]" + result[0]);
            System.out.println("Result[1]" + result[1]);
            lf.saveConstCalibrationToFile(eddystoneScan.getFileName(), result[0], result[1]);

        } else {
            Log.i(TAG, "Coloque-se a " + y.get(calCount) + " cm");
            dist.setText("Coloque-se a " + y.get(calCount) + " m");
            calBtn.setVisibility(View.VISIBLE);

        }


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
        System.out.println(TAG + " onResume()");
        if (!BluetoothUtils.isBluetoothEnabled()) {
            final Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("PAUSE");
        eddystoneScan.deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        eddystoneScan.deviceManager.disconnect();
        eddystoneScan.deviceManager = null;
    }
/*
    @OnClick(R.id.button)
    public void startScanningCalibration() {
        calCount++;
        eddystoneScan.startScan(CalibrationActivity.this);
        calBtn.setVisibility(View.INVISIBLE);

        System.out.println("click");


    }*/
}
