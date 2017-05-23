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

//updates the StormPI sensor GUI
public interface WritableGUI {
    
    void updateTemperature(double temp);
    void updateHumidity(double humidity);
    void updateBarometer(double pressure);
    void updateRainfall(double rainfall);
    void updateUser(String username, String password);
    void updateLocation(String location);
    void updateLogs(String log);
    
}
