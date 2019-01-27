package com.zbm.dainty.bean;

import java.io.Serializable;

public class WeatherInfoBean implements Serializable {
    private String city;
    private String temperature;
    private String climate;

    public WeatherInfoBean(String city, String temperature, String climate) {
        this.city = city;
        this.temperature = temperature;
        this.climate = climate;
    }

    public String getCity() {
        return city;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getClimate() {
        return climate;
    }
}
