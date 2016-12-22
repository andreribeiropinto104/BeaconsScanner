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

    ArrayList<Double> x = new ArrayList();
    ArrayList<Double> y = new ArrayList();


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

    @Bind(R.id.textView3)
    public TextView dist;
    @Bind(R.id.button)
    public Button calBtn;

    int calCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);



        context = this;

        ButterKnife.bind(this);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            calCount = 0;
            y.add(0.25);
            y.add(0.5);
            y.add(1.0);
            y.add(2.0);
            y.add(3.0);
            y.add(4.0);
/*            y.add(5.0);
            y.add(6.0);
            y.add(7.0);
            y.add(8.0);
            y.add(9.0);
            y.add(10.0);
            y.add(12.0);
            y.add(14.0);
            y.add(16.0);
            y.add(18.0);
            y.add(20.0);
            y.add(25.0);
            y.add(30.0);
            y.add(40.0);*/

            dist.setText("Coloque-se a " + this.y.get(calCount) + " cm");

            // Recebe da atividade anterior como parâmetro o dispositivo selecionado
            eddystone = (IEddystoneDevice) extras.get("EDDYSTONE");

            namespaceIdentifier = eddystone.getNamespaceId();
            Log.i(TAG, "name: " + namespaceIdentifier);
            beaconIdentifier = eddystone.getInstanceId();
            eddystoneScan = new EddystoneDetailsScan(context, beaconIdentifier, namespaceIdentifier);

        }


        /*ArrayList<Double> x = new ArrayList();
        ArrayList<Double> y = new ArrayList();

        x.add(0.8039215686);
        x.add(0.8431372549);
        x.add(0.9607843137);
        x.add(1.274509804);
        x.add(1.137254902);
        x.add(1.117647059);
        x.add(1.31372549);
        x.add(1.31372549);
        x.add(1.509803922);
        x.add(1.37254902);
        x.add(1.352941176);
        x.add(1.470588235);
        x.add(1.411764706);
        x.add(1.411764706);
        x.add(1.529411765);
        x.add(1.62745098);
        x.add(1.588235294);
        x.add(1.588235294);
        x.add(1.470588235);
        x.add(1.62745098);


        y.add(0.25);
        y.add(0.5);
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
        y.add(20.0);
        y.add(25.0);
        y.add(30.0);
        y.add(40.0);

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
        Log.i(TAG, "Racio: " + eddystoneScan.getMode()/eddystone.getTxPower());
        this.x.add(eddystoneScan.getMode()/eddystone.getTxPower());
        //calBtn.setVisibility(View.VISIBLE);
//        dist.setText("Coloque-se a " + this.y.get(calCount) + " cm");
        Log.i(TAG, "count: " + calCount);
        Log.i(TAG, "size: " + this.y.size());
        if (calCount == this.y.size()) {
            Dataset dataset = new Dataset(x, y);
            double[] result = getPowerRegression(dataset);
            System.out.println("Result[0]" + result[0]);
            System.out.println("Result[1]" + result[1]);
        } else {
            Log.i(TAG, "Coloque-se a " + this.y.get(calCount) + " cm");
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

    @OnClick(R.id.button)
    public void startScanningCalibration() {
        calCount++;
        eddystoneScan.startScan(CalibrationActivity.this);
        //calBtn.setVisibility(View.INVISIBLE);

        System.out.println("click");


    }
}
