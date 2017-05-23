/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.sensor.temperature;

import com.rama41222.storm.sensor.StormBoard;
import java.text.DecimalFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Rama41222
 */
public class ThermometerSensor implements Runnable {

    private StormBoard sb;
    private static DecimalFormat df2 = new DecimalFormat(".##");
    private Boolean stop = false;
    private static double temp = ThreadLocalRandom.current().nextInt(10, 29);

    public void setStop(Boolean stop) {
        this.stop = stop;
    }

    //Setting up the delegate
    public ThermometerSensor(StormBoard sb) {
        this.sb = sb;
    }
    //Generates random temperatures in 5 minute intervals

    @Override
    public void run() {
        while (!stop) {
            double no = ThreadLocalRandom.current().nextDouble();
            if (no > 0.5000) {
                temp -= 0.15;

            } else {
                temp += 0.12;
            }

            if (temp < 0) {
                temp = 0;
            }

            if (temp > 40) {
                temp = 38;
            }
            temp = Double.parseDouble(df2.format(temp));
            sb.sendTemperature(temp);
            try {
                //Thread.sleep(1000);
                Thread.sleep(300000);
            } catch (InterruptedException ex) {
                System.err.println(ex.getLocalizedMessage());
            }
        }
    }

}
