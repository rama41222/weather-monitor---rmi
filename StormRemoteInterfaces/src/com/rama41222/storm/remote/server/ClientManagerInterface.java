/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.remote.server;

import com.rama41222.storm.remote.client.TemperatureListenerInterface;

/**
 *
 * @author Rama41222
 */
public interface ClientManagerInterface extends java.rmi.Remote{
    
    String onDemandWeather(String location) throws java.rmi.RemoteException;
    void addTemperatureListener(TemperatureListenerInterface listener ) throws java.rmi.RemoteException;
    void removeTemperatureListener(TemperatureListenerInterface listener )throws java.rmi.RemoteException;
    boolean authentication(String key) throws java.rmi.RemoteException;
}

