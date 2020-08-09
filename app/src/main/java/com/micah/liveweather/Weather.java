package com.micah.liveweather;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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
        // data comes measured in "K" by default
        this.temp = MathHelper.round(temp);
        this.minTemp = MathHelper.round(minTemp);
        this.maxTemp = MathHelper.round(maxTemp);
        this.feelsLikeTemp = MathHelper.round(feelsLikeTemp);
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


    double convertToCelsius(double temp, char unit) {
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

    String capitalizeDescription() {
        return this.description.substring(0, 1).toUpperCase() +
                this.description.substring(1).toLowerCase();
    }

    void setWeatherImage(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context).load(imageUrl).into(imageView);
    }

    static String getWeatherTime() {
        final long now = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat sf = new SimpleDateFormat("MMM d, h:mm a");
        String newDateForm = sf.format(new Date(now));
        return newDateForm;
    }
}
