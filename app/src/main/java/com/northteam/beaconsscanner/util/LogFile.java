package com.northteam.beaconsscanner.util;

import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by beatrizgomes on 16/05/16.
 */
public class LogFile {

    String folderName;
    private String fileName;


    public LogFile(String folderName) {
        this.folderName = folderName;
    }

    public void createLogFile(int txPower) {
        Calendar c = Calendar.getInstance();
        setFileName(c.get(Calendar.YEAR) + "" + String.format("%02d", c.get(Calendar.MONTH) + 1) + "" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + "_" + c.get(Calendar.HOUR_OF_DAY) + "" + String.format("%02d",c.get(Calendar.MINUTE)) + ".csv");

        java.io.File directory = new java.io.File(Environment.getExternalStorageDirectory() + "/Beacons Scanner", folderName);
        if (!directory.exists()) {
            try {
                if (directory.mkdir()) {
                    System.out.println("Directory created");
                } else {
                    System.out.println("Directory is not created");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        java.io.File logFile = new java.io.File(directory + "/" + getFileName());

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, false));
            buf.append("Device Model;");
            buf.append(Build.MODEL + ";");
            buf.append("Android Version;");
            buf.append(Build.VERSION.RELEASE);
            buf.newLine();
            buf.append("Beacon Profile;");
            buf.append(folderName);
            buf.newLine();
            buf.append("Tx Power;");
            buf.append("" + txPower);
            buf.newLine();
            buf.append("Date; Time; Received RSSI; Suavized RSSI; Distance; Near");
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveLogToFile(String filename, String receivedRssi, String suavizedRssi, double distance, boolean markingLog) {

        File dir = Environment.getExternalStorageDirectory();
        File logFile = new File(dir + "/Beacons Scanner/" + folderName + "/" + filename);

        Calendar c = Calendar.getInstance();

        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            String date = c.get(Calendar.YEAR) + "/" + String.format("%02d", c.get(Calendar.MONTH) + 1) + "/" + String.format("%02d", c.get(Calendar.DAY_OF_MONTH)) + ";" + c.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", c.get(Calendar.MINUTE)) + ":" + String.format("%02d", c.get(Calendar.SECOND)) + ";";
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


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
