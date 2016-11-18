package com.northteam.beaconsscanner.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
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
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.northteam.beaconsscanner.R;
import com.northteam.beaconsscanner.adapter.monitor.EddystoneDetailsScan;
import com.northteam.beaconsscanner.adapter.monitor.IBeaconsDetailsScan;
import com.northteam.beaconsscanner.util.LogFile;

import java.io.File;
import java.util.Calendar;

public class RealtimeLineChartActivity extends AppCompatActivity implements
        OnChartValueSelectedListener,
        ProximityManager.ProximityListener {

    private static Context context;



    private LineChart mChart;
    private static final int REQUEST_CODE_ENABLE_BLUETOOTH = 1;
    public String beaconIdentifier;
    public String namespaceIdentifier;
    /**
     * The Eddystone scan.
     */
    EddystoneDetailsScan eddystoneScan;
    IEddystoneDevice eddystone;

    IBeaconsDetailsScan ibeaconScan;
    IBeaconDevice ibeacon;

    FloatingActionMenu menuBlue;

    /**
     * Gets context.
     *
     * @return the context
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

        setContext(this);

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);


        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if (extras.get("EDDYSTONE") != null) {
                eddystone = (IEddystoneDevice) extras.get("EDDYSTONE");
                eddystoneScan = new EddystoneDetailsScan(getContext(), eddystone.getInstanceId(), eddystone.getNamespaceId(), mChart);
                System.out.println("EXTRA");
            } else if (extras.get("IBEACON") != null) {
                ibeacon = (IBeaconDevice) extras.get("IBEACON");
                ibeaconScan = new IBeaconsDetailsScan(getContext(), ibeacon.getUniqueId(), mChart);
            }
        }


        // no description text
        mChart.setDescription("");
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


        menuBlue = (FloatingActionMenu) findViewById(R.id.menu_blue);

        final FloatingActionButton programFab1 = new FloatingActionButton(this);
        programFab1.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab1.setLabelText("texto");
        programFab1.setImageResource(R.drawable.ic_image_white_24dp);
        programFab1.setColorNormal(Color.parseColor("#00b8ff"));

        programFab1.setLabelText(getString(R.string.saveChartImage));

        menuBlue.addMenuButton(programFab1);
        programFab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                System.out.println("onClick 1");
                String fileName = c.get(Calendar.YEAR) + "" + String.format("%02d", c.get(Calendar.MONTH) + 1) + "" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + String.format("%02d", c.get(Calendar.MINUTE));
                mChart.saveToGallery(fileName, 50);
            }
        });


        final FloatingActionButton programFab2 = new FloatingActionButton(this);
        programFab2.setButtonSize(FloatingActionButton.SIZE_MINI);
        programFab2.setLabelText(getString(R.string.save_log_file));
        programFab2.setColorNormal(Color.parseColor("#00b8ff"));
        programFab2.setImageResource(R.drawable.ic_save_white_24dp);
        programFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("onClick 2");

                String text = "";
                boolean saving = false;
                if (eddystoneScan != null && eddystoneScan.getFileName() != null)
                    saving = true;
                if (ibeaconScan != null && ibeaconScan.getFileName() != null)
                    saving = true;
                if (!saving) {
                    programFab2.setLabelText(getString(R.string.stop_save_log_file));


                    if (eddystoneScan != null) {
                        LogFile cf = new LogFile("Eddystone");
                        cf.createLogFile(eddystone.getTxPower());
                        eddystoneScan.setFileName(cf.getFileName());
                    } else if (ibeaconScan != null) {
                        LogFile cf = new LogFile("IBeacon");
                        cf.createLogFile(ibeacon.getTxPower());
                        ibeaconScan.setFileName(cf.getFileName());
                        text = getString(R.string.fileSavingIbeaconChart);
                    }

                    /*
                    LayoutInflater inflater = getLayoutInflater();
                    // Inflate the Layout
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_layout));
                    ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
                    imgCSV.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
                    TextView textToast = (TextView) layout.findViewById(R.id.textToShow);
                    // Set the Text to show in TextView
                    textToast.setText(text);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 400);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
*/
                }
                else {
                    programFab2.setLabelText(getString(R.string.save_log_file));

                    if (eddystoneScan != null) {
                        eddystoneScan.setFileName(null);
                        text = getString(R.string.fileSavingEddystoneChart);
                    }
                    if (ibeaconScan != null) {
                        ibeaconScan.setFileName(null);
                        text = getString(R.string.fileSavingIbeaconChart);

                    }

                    LayoutInflater inflater = getLayoutInflater();
                    // Inflate the Layout
                    View layout = inflater.inflate(R.layout.custom_toast,
                            (ViewGroup) findViewById(R.id.custom_toast_layout));
                    ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
                    imgCSV.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
                    TextView textToast = (TextView) layout.findViewById(R.id.textToShow);
                    // Set the Text to show in TextView
                    textToast.setText(text);
                    Toast toast = new Toast(getApplicationContext());
                    toast.setGravity(Gravity.BOTTOM, 0, 400);
                    toast.setDuration(Toast.LENGTH_LONG);
                    toast.setView(layout);
                    toast.show();
                }

            }
        });
        menuBlue.addMenuButton(programFab2);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabSave);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                /*Intent intentRealtimeChartActivity = new Intent(RealtimeLineChartActivity.this, RealtimeLineChartActivity.class);
//                intentRealtimeChartActivity.putExtra("EDDYSTONE", eddystone);
//                startActivity(intentRealtimeChartActivity); */
//                String text = "";
//
//                if(eddystoneScan != null) {
//                    LogFile cf = new LogFile("Eddystone");
//                    cf.createLogFile(eddystone.getTxPower());
//                    eddystoneScan.setFileName(cf.getFileName());
//                    text = getString(R.string.fileSavingEddystoneChart);
//                } else if(ibeaconScan != null) {
//                    LogFile cf = new LogFile("IBeacon");
//                    cf.createLogFile(ibeacon.getTxPower());
//                    ibeaconScan.setFileName(cf.getFileName());
//                    text = getString(R.string.fileSavingIbeaconChart);
//                }
//
//                LayoutInflater inflater = getLayoutInflater();
//                // Inflate the Layout
//                View layout = inflater.inflate(R.layout.custom_toast,
//                        (ViewGroup) findViewById(R.id.custom_toast_layout));
//                ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
//                imgCSV.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
//                TextView textToast = (TextView) layout.findViewById(R.id.textToShow);
//                // Set the Text to show in TextView
//                textToast.setText(text);
//                Toast toast = new Toast(getApplicationContext());
//                toast.setGravity(Gravity.BOTTOM, 0, 400);
//                toast.setDuration(Toast.LENGTH_LONG);
//                toast.setView(layout);
//                toast.show();
//
//
//            }
//        });

    }

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
                if (eddystoneScan != null)
                    eddystoneScan.onDevicesUpdateEvent(bluetoothDeviceEvent);
                else if (ibeaconScan != null)
                    ibeaconScan.onDevicesUpdateEvent(bluetoothDeviceEvent);
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

        if (eddystoneScan != null)
            eddystoneScan.startScan(RealtimeLineChartActivity.this);
        else if (ibeaconScan != null)
            ibeaconScan.startScan(RealtimeLineChartActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (eddystoneScan != null)
            eddystoneScan.deviceManager.finishScan();
        else if (ibeaconScan != null)
            ibeaconScan.deviceManager.finishScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        String text = "";

        boolean saving = false;
        if (eddystoneScan != null) {
            if (eddystoneScan.getFileName() != null) {

                text = getString(R.string.fileSavedEddystone);
                saving = true;
            }
            eddystoneScan.deviceManager.disconnect();
            eddystoneScan.deviceManager = null;
        } else if (ibeaconScan != null) {
            if (ibeaconScan.getFileName() != null) {
                saving = true;
                text = getString(R.string.fileSavedIbeacon);
            }
            ibeaconScan.deviceManager.disconnect();
            ibeaconScan.deviceManager = null;
        }


        if (saving) {

            LayoutInflater inflater = getLayoutInflater();
            // Inflate the Layout
            View layout = inflater.inflate(R.layout.custom_toast,
                    (ViewGroup) findViewById(R.id.custom_toast_layout));
            ImageView imgCSV = (ImageView) layout.findViewById(R.id.imgToast);
            imgCSV.setImageResource(R.drawable.ic_insert_drive_file_black_48dp);
            TextView textToast = (TextView) layout.findViewById(R.id.textToShow);
            // Set the Text to show in TextView
            textToast.setText(text);
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.BOTTOM, 0, 400);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }

    }
}


