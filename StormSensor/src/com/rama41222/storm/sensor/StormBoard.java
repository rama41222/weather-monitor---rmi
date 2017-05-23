/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.sensor;

/**
 *
 * @author Rama41222
 */

//Get updates from Temperature, [pressure, humidity and rainfall sensors to the SensorClass via this interface
public interface StormBoard {
    void sendTemperature(double temp);
    void sendHumidity(double humidity);
    void sendBarometer(double pressure);
    void sendRainfall(double rainfall);
    void authenticate(double rainfall);
}
