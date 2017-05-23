/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.server;

/**
 *
 * @author Rama41222
 */

// Helps to update the user interface of the Client (StormAPP)
public interface WritableGUI {
    void writeLogs(String update);
    void updateSensorCount(int count);
    void updateClientCount(int count);
}
