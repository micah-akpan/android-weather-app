package com.micah.liveweather;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        this.temp = temp;
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
                celsiusTemp = temp - 273.15;
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


    double convertToCelsius(double temp, char unit, boolean round) {
        double celsiusTemp = 0;
        switch(unit) {
            case 'K':
                celsiusTemp = temp - 273.15;
                break;
            case 'F':
                celsiusTemp = (temp - 32) / (5 / 9);
                break;

            default:
                celsiusTemp = temp;
                break;
        }

        if (round) celsiusTemp = Math.round(celsiusTemp);
        return celsiusTemp;
    }

    static String getWeatherTime(double dateTime) {
        Date d = new Date((long) dateTime);
        SimpleDateFormat sf = new SimpleDateFormat("MMM DD, h:mm a");
        String newDateForm = sf.format(d);
        return newDateForm;
    }
}
