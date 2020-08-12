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
    public final double currentTime;
    public final String icon;

    // TODO: Refactor this to be an enum
    public static char currentUnit = 'C';


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

    public static void setCurrentUnit(char unit) {
        currentUnit = unit;
    }

    public double convertToCelsius(char unit) {
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


    public double convertToCelsius(double temp, char unit) {
        double celsiusTemp = 0;
        switch(unit) {
            case 'K':
                celsiusTemp = temp - 273.15;
                break;
            case 'F':
                celsiusTemp = (temp - 32) * (5 / 9);
                break;

            default:
                celsiusTemp = temp;
                break;
        }

        setCurrentUnit('C');

        return celsiusTemp;
    }

    public static double convertTemp(final double temp, final char to) {
        if (to == 'F') {
            setCurrentUnit('F');
            return (temp + 32 * 9) / 5;
        } else {
            setCurrentUnit('C');
            return (temp - 32) * (5 / 9);
        }
    }

    public String capitalizeDescription() {
        return this.description.substring(0, 1).toUpperCase() +
                this.description.substring(1).toLowerCase();
    }

    public void setWeatherImage(Context context, String imageUrl, ImageView imageView) {
        Glide.with(context).load(imageUrl).into(imageView);
    }

    public static String getWeatherTime() {
        final long now = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat sf = new SimpleDateFormat("MMM d, h:mm a");
        String newDateForm = sf.format(new Date(now));
        return newDateForm;
    }
}
