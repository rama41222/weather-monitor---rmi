/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.sensor.rainguage;

import com.rama41222.storm.sensor.StormBoard;
import com.rama41222.storm.sensor.StormPiManager;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;

/**
 *
 * @author Rama41222
 */
public class RainGuage implements Runnable {

    private StormBoard sb;
    private static final DecimalFormat df2 = new DecimalFormat(".##");
    private Boolean stop = false;
    private static double rainfall = ThreadLocalRandom.current().nextInt(10, 19);

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    //Setting up the delegate
    public RainGuage(StormPiManager sb) {
        this.sb = sb;
    }

    //Generates random rainfalls in 5 minute intervals
    //Rainfall only increases not decrease therefore the algorithm to generate random rainfalls have beeen changed
    @Override
    public void run() {
        while (!stop) {
            double no = ThreadLocalRandom.current().nextDouble();

            if (no > 0.5000) {
                rainfall += 0.015;
            } else {
                rainfall += 0.02;
            }
            if (rainfall < 0) {
                rainfall = 0;
            }

            rainfall = Double.parseDouble(df2.format(rainfall));
            sb.sendRainfall(rainfall);
            try {
               // Thread.sleep(1000);
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                System.err.println(ex.getLocalizedMessage());
            }
        }
    }
}
