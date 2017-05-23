/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.client;

/**
 *
 * @author Rama41222
 */
/*
Interface used to update the client
*/
public interface WritableGUI {
    void weatherUpdate(WeatherData json);
    void weatherAlert(WeatherData json);
    void writeLogs(String log);
    
}

