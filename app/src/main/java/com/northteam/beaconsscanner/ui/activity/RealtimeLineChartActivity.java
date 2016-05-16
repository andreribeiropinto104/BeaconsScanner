package com.northteam.beaconsscanner.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.util.BluetoothUtils;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.EddystoneDetailsScan;

public class RealtimeLineChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener,
        ProximityManager.ProximityListener {

    private static Context context;

    private LineChart mChart;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_WRITE_STORAGE = 3;
    public String beaconIdentifier;
    public String namespaceIdentifier;
    /**
     * The Eddystone scan.
     */
    EddystoneDetailsScan eddystoneScan;
    IEddystoneDevice eddystone;

    /**
     * The constant context.
     */
    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        RealtimeLineChartActivity.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_line_chart);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        setContext(this);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);


        final Bundle extras = getIntent().getExtras();

        if (extras != null) {

            eddystone = (IEddystoneDevice) extras.get("EDDYSTONE");
            namespaceIdentifier = eddystone.getNamespaceId();
            beaconIdentifier = eddystone.getInstanceId();
            eddystoneScan = new EddystoneDetailsScan(getContext(), beaconIdentifier, namespaceIdentifier, mChart);
            System.out.println("EXTRA");
        }


        // no description text
        mChart.setDescription("lalalala");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        // l.setPosition(LegendPosition.LEFT_OF_CHART);
        l.setForm(Legend.LegendForm.LINE);
        l.setTypeface(tf);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTypeface(tf);
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(-40f);
        leftAxis.setAxisMinValue(-100f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
        //addEntry();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    /*
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.realtime, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            switch (item.getItemId()) {
                case R.id.actionAdd: {
                    //addEntry();
                    break;
                }
                case R.id.actionClear: {
                    mChart.clearValues();
                    Toast.makeText(this, "Chart cleared!", Toast.LENGTH_SHORT).show();
                    break;
                }
                case R.id.actionFeedMultiple: {
                    //feedMultiple();
                    break;
                }
            }
            return true;
        }
    */
    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
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
        System.out.println("onResume()");
        eddystoneScan.startScan(RealtimeLineChartActivity.this);
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

    }
}


