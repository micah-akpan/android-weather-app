package com.micah.liveweather;

import java.time.LocalDateTime;

public class Weather {
    public final String description;
    public final double temp;
    public final double minTemp;
    public final double maxTemp;
    public final double feelsLikeTemp;
//    public final LocalDateTime currentTime;
    public final double currentTime;
    public final String icon;
    private char currentUnit = 'C';


    public Weather(String description, double temp, double minTemp, double maxTemp,
                   double feelsLikeTemp, double currentTime, String icon) {
        this.description = description;
        this.icon = icon;
        // data comes in "K" by default
        this.temp = convertToCelsius('K');
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.feelsLikeTemp = feelsLikeTemp;
        this.currentTime = currentTime;
    }

    public void setCurrentUnit(char unit) {
        currentUnit = unit;
    }

    public char getCurrentUnit() {
        return currentUnit;
    }

    double convertToCelsius(char unit) {
        double celsiusTemp = 0;
        switch(unit) {
            case 'K':
                celsiusTemp = 273.15 - temp;
                break;
            case 'F':
                celsiusTemp = (temp - 32) / (5 / 9);
                break;

            default:
                celsiusTemp = temp;
                break;
        }

        return celsiusTemp;
    }
}
