

/* Copyright (C) John Rogers Group - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Jong Yoon Lee <jlee642@illinois.edu>, 08/18/2018
 */
package no.nordicsemi.android.nrftoolbox.nfc_ble_hybrid;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class log {

    private File logFile;
    private long reference;
    private String name;
    private int counter = 0;
    private long startTime;
    private boolean timerStarted;

    public log() {
        timerStarted = false;
    }

    public void MakeNewLog() {
        String textfile = "";
        textfile += new SimpleDateFormat("yyyy_MM_dd_HH_mm_SSS").format(new Date());
        textfile += ".txt";
        logFile = new File(Environment.getExternalStorageDirectory(), textfile);
        name = textfile;

        int i = 2;

        Log.v("log", "Making a New Log");

        try {
            logFile.createNewFile();
            PrintWriter output = new PrintWriter(new FileWriter(logFile, true));
            output.printf("%s\t%8s\t%8s\t%8s\t%8s\t%8s\t%8s\r\n", "Sample", "Time(s)", "x", "y", "z", "a", "battery");
            output.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    public String getname(){
        return name;
    }

    public void appendLog(int[] x,int[] y,int[] z, int[] a, int[] b) {
        double flow1,flow2,flow3,flow4,batteryval,time;
        if (!timerStarted){
            timerStarted=true;
            startTime = SystemClock.elapsedRealtime();
            time = 0.0;
        }
        else{
            time = ((double)(SystemClock.elapsedRealtime() - startTime))/1000;
        }

        try {
            PrintWriter output = new PrintWriter(new FileWriter(logFile, true));

            //String outputstring = "";

            //Log.v("log", "writing to file");


            for(int i = 0; i < 2; i++){
                flow1 = (x[i]);
                flow2 = (y[i]);
                flow3 = (z[i]);
                flow4 = (a[i]);
                batteryval = b[i];
               // v_sup = vdd[i];

                output.printf("%6d\t%8.3f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\t%8.4f\r\n", counter, time,flow1,flow2,flow3,flow4,batteryval);
                counter++;
            }

            output.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.v("log", "log not working");
        }

    }
}