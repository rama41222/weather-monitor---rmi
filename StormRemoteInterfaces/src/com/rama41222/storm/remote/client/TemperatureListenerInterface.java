/*Gson
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.remote.client;


/**
 *
 * @author Rama41222
 */
public interface TemperatureListenerInterface extends java.rmi.Remote {
    void weatherChanged(String weatehr) throws java.rmi.RemoteException;
    void weatherAlert(String weatehr) throws java.rmi.RemoteException;
    void updateSensorCount(int sensorCount) throws java.rmi.RemoteException;
    void updateClientCount(int clientCount) throws java.rmi.RemoteException;
}
