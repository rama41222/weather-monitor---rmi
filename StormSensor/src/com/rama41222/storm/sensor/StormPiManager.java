/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rama41222.storm.sensor;

import com.rama41222.storm.sensor.barometer.BarometerSensor;
import com.rama41222.storm.sensor.humidity.HumiditySensor;
import com.rama41222.storm.sensor.rainguage.RainGuage;
import com.rama41222.storm.sensor.temperature.ThermometerSensor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Rama41222
 */
public class StormPiManager implements StormBoard {

    private final WritableGUI gui;
    private final ThermometerSensor ts = new ThermometerSensor(this);
    private final BarometerSensor ps = new BarometerSensor(this);
    private final RainGuage rg = new RainGuage(this);
    private final HumiditySensor hs = new HumiditySensor(this);

    private double temp = 0.0;
    private double humidity = 0.0;
    private double rainfall = 0.0;
    private double pressure = 0.0;

    private Socket sock = null;
    private BufferedReader in;
    private PrintWriter out;
    private String serverAddress;
    private String clientPassword;
    private String clientUserName;
    private int serverPort;

    private String regUsername;
    private String regPassword;
    private String regLocation;

    private String loc = "";

    private String getClientUserName() {
        return clientUserName;
    }

    public StormPiManager(WritableGUI gui) {
        this.gui = gui;
    }

    private String getClientPassword() {
        return clientPassword;
    }

    public double getTemp() {
        return temp;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getRainfall() {
        return rainfall;
    }

    public double getPressure() {
        return pressure;
    }

    //Setting registered user information
    public void registerSensor(String username, char[] password, String location) {
        this.regUsername = username;
        this.regPassword = new String(password);
        this.regLocation = location;
        this.clientUserName = username;
        this.clientPassword = new String(password);
        out.println("register_user");
        out.println(this.regUsername + ":" + this.regPassword + ":" + this.regLocation);

    }

    //Setting the sensor parameters needed to connect to the server(for authentication and to connect)
    public void setSensorParams(String serverAddress, int serverPort, char[] clientPassword, String clientUserName) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clientPassword = new String(clientPassword);
        this.clientUserName = clientUserName;
    }

    Thread tempTask = new Thread() {
        @Override
        public void run() {
            ts.run();
        }
    };

    Thread pressureTask = new Thread() {
        @Override
        public void run() {
            ps.run();
        }
    };

    Thread rainGuageTask = new Thread() {
        @Override
        public void run() {
            rg.run();
        }
    };

    Thread humidityTask = new Thread() {
        @Override
        public void run() {
            hs.run();
        }
    };

    //this method handles the main logic / protocol on how to respond for the server request.
    //This is the standard way of making protocols when dealing with low level implemetation fof sockets
    void runSensorProtocol() throws InterruptedException {
        try {

            //Opens a socket 
            sock = new Socket(this.serverAddress, this.serverPort);
            //getting the input stream from the opend socket
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            //setting the output stream from the opend socket
            out = new PrintWriter(sock.getOutputStream(), true);
            String line = "";
            //while there's a input from the server this loop will run
            while ((line = in.readLine()) != null) {
                //validation
                if (line.startsWith("login")) {

                    out.println(this.getClientUserName());
                    out.println(this.getClientPassword());

                } else if (line.startsWith("login_success")) {

                    // out.println("Hiiiiii ");
                } else if (line.startsWith("user_found_password_mismatch")) {
                    //password mismatch alert
                    JOptionPane.showMessageDialog(new JFrame(), "Incorrect Password, Please Reconnect");
                    //Spawning the register user interface when the user is not available
                } else if (line.startsWith("user_not_found")) {

                    Register r = new Register(this);
                    r.setVisible(true);
                    //when the connection is success
                } else if (line.startsWith("connection_success")) {
                    String location = in.readLine();
                    this.loc = location;
                    gui.updateLocation(location + " Sensor is Online");
                    JOptionPane.showMessageDialog(new JFrame(), "Connected to the Server Successfully!");
                    //if the server is offline or no input
                } else if (line.startsWith("reconnect")) {
                    if (!"".equals(this.loc) || !this.loc.isEmpty()) {
                        gui.updateLocation(this.loc + " Sensor is Offiline");
                    }
                    JOptionPane.showMessageDialog(new JFrame(), "Connection Lost, Please Reconnect");
                } else if (line.startsWith("send_weather")) {
                    //sending the updates from the sensor in the intervasl of 1 hour
                    out.println(this.getTemp());
                    out.println(this.getPressure());
                    out.println(this.getRainfall());
                    out.println(this.getHumidity());
                    //Thread.sleep(5000);
                    Thread.sleep(3600000);
                    //reauthentication
                } else if (line.startsWith("re_authenticate")) {
                    out.println(this.getClientUserName());
                    out.println(this.getClientPassword());
                    //IF 2 SENSORS registers for a same location
                } else if (line.startsWith("location_already_taken")) {
                    gui.updateLocation(this.loc + " Sensor is Offiline");
                    JOptionPane.showMessageDialog(new JFrame(), "Location already taken by a sensor, Please try again with more accurate location!");
                } else if (line.startsWith("error")) {
                    gui.updateLocation(this.loc + " Sensor sent 500");
                    JOptionPane.showMessageDialog(new JFrame(), "Internal Server Error: 500");
                }

            }

            JOptionPane.showMessageDialog(new JFrame(), "Connection to the Server Lost or Server is offline, Please Reconnect");
            //exception handling
        } catch (UnknownHostException e) {
            gui.updateLocation(this.loc + " Sensor is Offiline due to : " + e.getMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Sensor is turned of or Connection was reset, Please reconnect");
        } catch (IOException e) {
            gui.updateLocation(this.loc + " Sensor is Offiline due to : " + e.getMessage());
            System.err.println(e.getLocalizedMessage());
            JOptionPane.showMessageDialog(new JFrame(), "Sensor is turned off or Connection was reset, Please reconnect");
        }

    }

    //Start all threads (temperature , pressure, humidity and rainfall)
    public void startSensors() {
        tempTask.start();
        pressureTask.start();
        rainGuageTask.start();
        humidityTask.start();
    }

    //Stop all threads
    public void stopSensors() {
        ts.setStop(true);
        ps.setStop(true);
        rg.setStop(true);
        hs.setStop(true);

    }

    // get the temperature from temp thread.
    @Override
    public void sendTemperature(double temp) {
        this.temp = temp;
        gui.updateTemperature(temp);
        System.out.println(temp);
    }

    /// getting the humidity from humidity threaf
    @Override
    public void sendHumidity(double humidity) {
        this.humidity = humidity;
        gui.updateHumidity(humidity);
    }

    //get the pressure from pressure thread task
    @Override
    public void sendBarometer(double pressure) {
        this.pressure = pressure;
        gui.updateBarometer(pressure);
        System.out.println(pressure);
    }

    //get the rainfall from rainfall thread task
    @Override
    public void sendRainfall(double rainfall) {
        this.rainfall = rainfall;
        gui.updateRainfall(rainfall);
    }

    //TODO:
    @Override
    public void authenticate(double rainfall) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //Updating the gui after registering a user, so that the sensor can be automatically logged in
    void setUser(String usrrname, char[] password) {
        gui.updateUser(usrrname, new String(password));
    }

}
