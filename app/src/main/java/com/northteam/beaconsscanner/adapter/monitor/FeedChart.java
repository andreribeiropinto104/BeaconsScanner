package com.northteam.beaconsscanner.adapter.monitor;

import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.northteam.beaconsscanner.R;

import java.util.Calendar;

/**
 * Created by arpinto on 13-05-2016.
 */
public class FeedChart {
    private LineChart mChart;
    private Context context;

    public FeedChart(LineChart mChart, Context c) {
        this.mChart = mChart;
        this.context = c;

    }

    public void addEntry(double suavizedRssi, double receivedRssi) {

        Calendar c = Calendar.getInstance();
        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet setSuavizedRssi = data.getDataSetByIndex(0);
            ILineDataSet setReceivedRssi = data.getDataSetByIndex(1);
            // set.addEntry(...); // can be called as well

            if (setSuavizedRssi == null) {
                setSuavizedRssi = createSetSuavizedRssi();
                data.addDataSet(setSuavizedRssi);
            }
            if (setReceivedRssi == null) {
                setReceivedRssi = createSetReceivedRssi();
                data.addDataSet(setReceivedRssi);
            }

            // add a new x-value first
            String second = String.valueOf(c.get(Calendar.SECOND));
            if (c.get(Calendar.SECOND) < 10)
                second = "0" + second;

            data.addXValue(c.get(Calendar.MINUTE) + ":" + second);
            data.addEntry(new Entry((float) suavizedRssi, setSuavizedRssi.getEntryCount()), 0);
            data.addEntry(new Entry((float) receivedRssi, setReceivedRssi.getEntryCount()), 1);


            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(15);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getXValCount() - 16);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private LineDataSet createSetSuavizedRssi() {

        LineDataSet set = new LineDataSet(null, context.getString(R.string.suavizedRssi));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(11f);
        set.setDrawValues(false);
        return set;
    }

    private LineDataSet createSetReceivedRssi() {

        LineDataSet set = new LineDataSet(null, context.getString(R.string.receivedRssi));
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.parseColor("#ff6600"));
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setColor(Color.parseColor("#ff6600"));
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(11f);
        set.setDrawValues(false);
        return set;
    }

}
