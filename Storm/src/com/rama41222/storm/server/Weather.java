//Pojoclass used to convert Weather object to json via gson inorder to send the weater update to client via RMI
package com.rama41222.storm.server;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Weather {

    @SerializedName("temperature")
    @Expose
    private String temperature;
    @SerializedName("rainfall")
    @Expose
    private String rainfall;
    @SerializedName("pressure")
    @Expose
    private String pressure;
    @SerializedName("humidity")
    @Expose
    private String humidity;

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getRainfall() {
        return rainfall;
    }

    public void setRainfall(String rainfall) {
        this.rainfall = rainfall;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

}
