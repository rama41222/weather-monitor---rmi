/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.sensor.humidity;

import com.rama41222.storm.sensor.StormBoard;
import com.rama41222.storm.sensor.StormPiManager;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Rama41222
 */
public class HumiditySensor implements Runnable {

    private StormBoard sb;
    private static final DecimalFormat df2 = new DecimalFormat(".###");
    private Boolean stop = false;
    private static double humidity = 0.20;

    //Setting up the delegate
    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    //Generating humidity values in 5 minute intervals
    public HumiditySensor(StormPiManager sb) {
        this.sb = sb;
    }

    @Override
    public void run() {
        while (!stop) {
            double no = ThreadLocalRandom.current().nextDouble();
            if (no > 0.5) {
                humidity -= 0.01;
            } else {
                humidity += 0.02;
            }

            if (humidity < 0) {
                humidity = 0;
            }

            humidity = Double.parseDouble(df2.format(humidity));
            sb.sendHumidity(humidity);
            try {
                //Thread.sleep(1000);
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                System.err.println(ex.getLocalizedMessage());
            }
        }
    }

}
