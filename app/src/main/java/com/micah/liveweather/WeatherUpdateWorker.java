package com.micah.liveweather;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.net.URL;

public class WeatherUpdateWorker extends Worker {
    public WeatherUpdateWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String weatherString = updateWeather();
        Log.d("WeatherUpdate", weatherString);
        return Result.success(
                new Data.Builder()
                  .putString("WEATHER_UPDATE_RESULT", weatherString)
                .build()
        );
    }

    public String updateWeather() {
        try {
            URL url = new URL(getInputData().getString("REQUEST_URL"));
            String jsonString = WeatherHelper.getWeatherData(url);
            return jsonString;
        } catch (Exception e) {

        }
        return null;
    }
}
