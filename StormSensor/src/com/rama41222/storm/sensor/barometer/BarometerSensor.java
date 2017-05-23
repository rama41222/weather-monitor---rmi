/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.sensor.barometer;

import com.rama41222.storm.sensor.StormBoard;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Rama41222
 */
public class BarometerSensor implements Runnable {

    private StormBoard sb;
    private static final DecimalFormat df2 = new DecimalFormat(".#####");
    private Boolean stop = false;
    private static double atm = 1;

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    //Setting up the delegate
    public BarometerSensor(StormBoard sb) {
        this.sb = sb;
    }

    //Generating random pressures in 5 minute intervals 
    @Override
    public void run() {
        while (!stop) {
            double no = ThreadLocalRandom.current().nextDouble();

            if (no > 0.5) {
                atm -= 0.00005;
            } else {
                atm += 0.00002;
            }

            if (atm < 0.9600) {
                atm = 1.000;
            } else if (atm > 1.16700) {
                atm = 1.0001;
            }

            atm = Double.parseDouble(df2.format(atm));
            sb.sendBarometer(atm);
            try {
                 //Thread.sleep(1000);
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                System.err.println(ex.getLocalizedMessage());
            }
        }
    }

}
