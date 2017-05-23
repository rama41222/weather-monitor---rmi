/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.server;

import com.google.gson.Gson;
import com.rama41222.storm.remote.client.TemperatureListenerInterface;
import com.rama41222.storm.remote.server.ClientManagerInterface;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Rama41222
 */
public class StormServer {

    private static WritableGUI gui;
    private static final StormServer ss = new StormServer();
    //Concurrent maps are used becasue of thread safety
    private static final ConcurrentMap<String, Sensor> onlineSensorList = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, WeatherData> weatherDataList = new ConcurrentHashMap<>();
    // private static final ConcurrentMap<String, WeatherData> weatherAlertList = new ConcurrentHashMap<>();
    //copy on write arraylists are used as normal ArrayLists are not thread safe 
    private static final CopyOnWriteArrayList<TemperatureListenerInterface> onlineMonitoringStationsList = new CopyOnWriteArrayList<>();
    private static ClientHandler clh;

    private static final Gson g = new Gson();
    private static int PORT = 0;
    private static int RMI_PORT = 0;

    public int getRMI_PORT() {
        return RMI_PORT;
    }

    public void setRMI_PORT(int aRMI_PORT) {
        RMI_PORT = aRMI_PORT;
    }

    private StormServer() {

    }

    public void setPORT(int serverPort) {
        PORT = serverPort;
    }

    public void setDelegate(WritableGUI i) {
        gui = i;
    }

    public static StormServer _getInstance() {
        return ss;
    }

    //Starting the rmi interface
    public void startRMIInterface() throws IOException {

        Thread startRMIInterfaceTask = new Thread() {
            @Override
            public void run() {

                try {
                    clh = new ClientHandler(getRMI_PORT());
                    clh.startRMIClientHandler();
                } catch (RemoteException ex) {
                    System.out.println(ex);
                } catch (IOException ex) {
                    System.out.println(ex);
                }

            }

        };

        startRMIInterfaceTask.start();
    }

    public void startSocketServer() throws IOException {
        //Starting the socket interface
        ServerSocket listener = new ServerSocket(PORT);
        gui.writeLogs("Storm Server started successfully at port " + PORT);
        while (true) {
            //for every new client who connects to the server, the serversocket will automatically create
            // a sockt and once the connection is acceptd it will spawn a new thread per every client, which is good in
            //terms of performace since 2 users on a single thread may interrupt each other.
            new SensorHandler(listener.accept()).start();
            //updating the sensor count
            gui.updateSensorCount(getNoOfOnlineSensors());

        }
    }

    //counting the CopyOnWriteArrayList for the size to get the no of connected clients
    private static synchronized int getNoOfOnlineClients() {
        return onlineMonitoringStationsList.size();
    }
    //counting the map for the size to get the no of connected Sensors

    private static synchronized int getNoOfOnlineSensors() {
        return onlineSensorList.size();
    }

    //remote metod used to send the updated count of clients and sensors to the connected monitors and upate sensor gui
    private static void updateCounts() throws RemoteException {
        for (TemperatureListenerInterface i : onlineMonitoringStationsList) {
            i.updateClientCount(getNoOfOnlineClients());
            i.updateSensorCount(getNoOfOnlineSensors());

        }

    }

    //notifying all the monitoring statings when there's a new weather update
    private static void notifyAllMonitoringStations() throws RemoteException {

        for (TemperatureListenerInterface i : onlineMonitoringStationsList) {

            Set mapkeys = weatherDataList.keySet();
            for (Object entry : mapkeys) {

                try {
                    //Differentiating into the weather alerts
                    //if it's a wheter alert, this will be sent via weatherAlert interface to the monitoring stating
                    //where the alert will be displayed on the alertsData table.

                    if (weatherDataList.get(entry).getIsCritical() != true) {
                        i.weatherChanged(g.toJson(weatherDataList.get(entry)));

                    } else if (weatherDataList.get(entry).getIsCritical() == true) {
                        i.weatherAlert(g.toJson(weatherDataList.get(entry)));

                    }

                    // updateCounts();
                } catch (RemoteException ex) {
                    // updateCounts();
                    //Removes the weather data and weather station when ther'es a remote exception
                    weatherDataList.remove(entry);
                    onlineMonitoringStationsList.remove(i);
                }
            }

        }

    }

    private static class ClientHandler extends UnicastRemoteObject implements ClientManagerInterface {

        private final Gson g = new Gson();
        private final int RMI_PORT;

        public void startRMIClientHandler() throws IOException {
            gui.writeLogs("Starting the Strom Weather service");

            try {
                /// Creating a instance of the ClientHandler
                ClientHandler srv = new ClientHandler(this.RMI_PORT);
                //starting the rmi registry on the given port
                Registry reg = LocateRegistry.createRegistry(this.RMI_PORT);
                gui.writeLogs("RMI is up and running on port " + this.RMI_PORT);
                //rebidning the srv object unser the storm_server name so that rmi client can lookup for storm_server name and
                //accress the srv object
                reg.rebind("storm_server", srv);
                gui.writeLogs("Rebinded the RMI Interface Implementation");
                
                notifyAllMonitoringStations();
                
            } catch (RemoteException re) {
                System.out.println(re + re.toString() + re.getLocalizedMessage() + re.getMessage());
            } catch (Exception e) {
                System.err.println("Error - " + e);

            }

        }

        ClientHandler(int RMI_PORT) throws RemoteException {
            this.RMI_PORT = RMI_PORT;
        }
        //adding a new weather listener
        @Override
        public void addTemperatureListener(TemperatureListenerInterface tli) throws RemoteException {
            gui.writeLogs("New Monitoring Station Added");
            gui.updateClientCount(getNoOfOnlineClients());
            onlineMonitoringStationsList.add(tli);
             updateCounts();
            

        }
        //remiving a temperature listener when disconnected or authentication failed
        @Override
        public void removeTemperatureListener(TemperatureListenerInterface tli) throws RemoteException {
            gui.writeLogs("Monitoring Station Removed");
            
            onlineMonitoringStationsList.remove(tli);
            updateCounts();
            gui.updateClientCount(getNoOfOnlineClients());

        }
        //remote method which authenticates a client
        @Override
        public boolean authentication(String key) throws RemoteException {
            if ("123".equals(key)) {
                gui.writeLogs("New Monitoring System Weather request Authenticated");
                return true;
            } else {
                gui.writeLogs("New Monitoring System Weather Request Denied");
                return false;
            }
        }
        //latest weather json will be sent  when clicking on demand wheter, this remote method is invked by the rmi client
        @Override
        public String onDemandWeather(String location) throws RemoteException {
            if (weatherDataList.containsKey(location)) {
                String weather = g.toJson(weatherDataList.get(location));
                return weather;
            }

            return "{\"location\":\"Kandy\",\"temperature\":\"Sensor Offline\",\"rainfall\":\"Sensor Offline\",\"pressure\":\"Sensor Offline\",\"humidity\":\"Sensor Offline\",\"isCritical\":false}";
        }

    }

    private static class SensorHandler extends Thread {

        private String authString;
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private final Sensor s = new Sensor();
        private final Weather w = new Weather();
        private WeatherData wd;
        Gson g = new Gson();

        //setting the socket
        private SensorHandler(Socket s) {
            this.socket = s;
            gui.writeLogs(socket.getInetAddress().getHostAddress() + " joined the server!\n");
        }

        @Override
        public void run() {
            try {
                //getting the input stream from the connected sensor
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                //setting the outpiut stream to the sensor
                out = new PrintWriter(socket.getOutputStream(), true);
                //login logic
                OUTER:
                while (true) {
                    gui.writeLogs("login request sent to " + socket.getInetAddress().getHostAddress() + "\n");
                    out.println("login");
                    s.setUsername(in.readLine());
                    s.setPassword(in.readLine());
                    String res = validateUser(s.getUsername(), s.getPassword());
                    if (null != res) {
                        switch (res) {
                            case "authenticated":
                                gui.writeLogs(socket.getInetAddress().getHostAddress() + " authenticated Successfully\n");
                                s.setIsonline(true);
                                onlineSensorList.put(s.getUsername(), s);
                                out.println("login_success");
                                gui.updateSensorCount(getNoOfOnlineSensors());
                                //  updateCounts();
                                break OUTER;
                            case "user_found_password_mismatch":
                                gui.writeLogs(socket.getInetAddress().getHostAddress() + " username found but password mismatch!\n");
                                out.println("user_found_password_mismatch");
                                gui.updateSensorCount(getNoOfOnlineSensors());
                                return;
                            default:
                                gui.writeLogs(socket.getInetAddress().getHostAddress() + " user not found \n");
                                out.println("user_not_found");
                                OUTER_1:
                                while (true) {
                                    if ("register_user".equals(in.readLine())) {
                                        gui.writeLogs("Registration request sent to user " + socket.getInetAddress().getHostAddress() + "\n");
                                        this.authString = in.readLine();
                                        if (this.authString != null || "".equals(this.authString)) {
                                            String authData[] = this.authString.split(":");
                                            String location = authData[2];
                                            String result = isLocationAvailable(location);
                                            if (null != result) {
                                                switch (result) {
                                                    case "location_available":
                                                        gui.writeLogs("Registernig user " + socket.getInetAddress().getHostAddress() + "\n");
                                                        regUser(this.authString);
                                                        break OUTER_1;
                                                    case "location_already_taken":
                                                        gui.writeLogs("Registernig user  " + socket.getInetAddress().getHostAddress() + "failed! Location already taken!\n");
                                                        out.println("location_already_taken");
                                                        break OUTER_1;
                                                    default:
                                                        gui.writeLogs("Error Occured while registering the user " + socket.getInetAddress().getHostAddress() + "\n");
                                                        out.println("error");
                                                        break OUTER_1;
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }
                }
                //When the login is finished, the sensor will repeatedly send weater data to the server which is stored in  concurrent maps
                out.println("connection_success");
                gui.updateSensorCount(getNoOfOnlineSensors());
                s.setLocation(findLocation(s.getUsername()));
                out.println(s.getLocation());
                gui.writeLogs(socket.getInetAddress().getHostAddress() + " connected to the weather service successfully!\n");
                System.out.println(in.readLine());
                System.out.println(in.readLine());
                while (true) {
                    wd = new WeatherData();
                    out.println("send_weather");
                    w.setTemperature(in.readLine());
                    w.setPressure(in.readLine());
                    w.setRainfall(in.readLine());
                    w.setHumidity(in.readLine());

                    s.setWeather(w);

                    wd.setTemperature(w.getTemperature());
                    wd.setLocation(s.getLocation());
                    wd.setHumidity(w.getHumidity());

                    wd.setPressure(w.getPressure());
                    wd.setRainfall(w.getRainfall());

                    //checking whether the weater is critical or not, alert is send if the weater is criticalÏ
                    if ((wd != null || !"null".equals(wd.toString())) && ((Double.parseDouble(wd.getRainfall()) > 20)) || (Double.parseDouble(wd.getTemperature()) > 30 || Double.parseDouble(wd.getTemperature()) <= 20)) {

                        wd.setIsCritical(true);

                    } else {

                        wd.setIsCritical(false);

                    }
                    //notifying all monitoring stations when there's a wheter update
                    weatherDataList.put(wd.getLocation(), wd);
                    gui.writeLogs("Weather Update From " + socket.getInetAddress().getHostAddress() + "\n" + g.toJson(wd) + "\n");
                    notifyAllMonitoringStations();

                    //  add
                    
                }

                //}
            } catch (IOException e) {
                System.out.println(e);
                try {
                    updateCounts();
                    gui.updateSensorCount(getNoOfOnlineSensors());
                    System.err.println(e.getLocalizedMessage());
                } catch (RemoteException ex) {
                    gui.writeLogs(ex.toString());
                }

            } finally {

                gui.writeLogs(s.getUsername() + " - " + s.getLocation() + " disconnected from the server!\n");
                //   weatherDataList.remove(wd.getLocation());
                //  System.out.println("Removed weather data  from " + wd.getLocation());

//                weatherAlertList.remove(wd.getLocation());
//                System.out.println("Removed weather alert  from " + wd.getLocation());
                onlineSensorList.remove(s.getUsername());
                // gui.updateSensorCount(getNoOfOnlineSensors());
                out.println("reconnect");
//                try {
//                    updateCounts();
//                } catch (RemoteException ex) {
//                    gui.writeLogs(ex.getMessage());
//                }
                //      gui.updateSensorCount(getNoOfOnlineSensors());

            }
        }

        //checcking whether the location is available to register a sensor. No 2 sensors
        // can be registred to and deployed in same area.
        private String isLocationAvailable(String location) {
            try {
                Scanner input = new Scanner(new FileReader(System.getProperty("user.dir") + "/src/com/rama41222/storm/server/sensors.txt"));

                while (input.hasNext()) {
                    String line = input.nextLine();
                    String authUser[] = line.split(":");

                    if (location.equals(authUser[2])) {

                        return "location_already_taken";
                    }

                }
                return "location_available";
            } catch (Exception e) {
                gui.writeLogs("Database Read Error : " + e.getLocalizedMessage());
                return "error";
            }

        }

        //find the location of a particular user form the stored sensors.txt text file
        private String findLocation(String username) {
            try {
                Scanner input = new Scanner(new FileReader(System.getProperty("user.dir") + "/src/com/rama41222/storm/server/sensors.txt"));

                while (input.hasNext()) {
                    String line = input.nextLine();
                    String authUser[] = line.split(":");
                    System.out.println(line);
                    if (username.equals(authUser[0])) {
                        System.out.println(authUser[2]);
                        return authUser[2];
                    }

                }
                return null;
            } catch (Exception e) {
                return null;
            }

        }

        //WRITINg a new user to a file
        private synchronized boolean regUser(String user) {
            try {

                try (FileWriter fw = new FileWriter(System.getProperty("user.dir") + "/src/com/rama41222/storm/server/sensors.txt", true) //the true will append the new data
                        ) {
                    fw.write("\n" + user);//appends the string to the file
                } //appends the string to the file
            } catch (IOException ioe) {

                System.err.println("IOException: " + ioe.getMessage());
            }
            return false;
        }

        //Reading a file and searchs for a particular user, if the user exists returns authenticated
        //else if the user found and passowrd incorrect, returns user_found_password_mismatch
        //else returns user not found
        private synchronized String validateUser(String username, String password) {
            try {
                Scanner input = new Scanner(new FileReader(System.getProperty("user.dir") + "/src/com/rama41222/storm/server/sensors.txt"));

                while (input.hasNext()) {
                    String line = input.nextLine();
                    String authUser[] = line.split(":");
                    if (username.equals(authUser[0])) {

                        if (password.equals(authUser[1])) {
                            return "authenticated";
                        } else {
                            return "user_found_password_mismatch";
                        }
                    }

                }
                return "user_not_found";
            } catch (Exception e) {
                return "user_not_found";
            }

        }

        //method used to reauthenticated. Currently not in use since the performance decrement of the server
        //As a security precaution, it's better if we can validate each and every request 
        private synchronized String reAuthenticate(String username, String password) {
            Set mapkeys = onlineSensorList.keySet();

            for (Object key : mapkeys) {
                if (key.toString().equals(username)) {
                    Iterator iterator = mapkeys.iterator();
                    while (iterator.hasNext()) {
                        String keys = (String) iterator.next();
                        Sensor sensor = onlineSensorList.get(keys);
                        if (sensor.getPassword().equals(password)) {
                            return "authenticated";
                        } else {
                            return "user_found_password_mismatch";
                        }

                    }

                }

            }
            return "user_not_found";
        }

    }

}
