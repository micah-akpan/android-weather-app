package com.micah.liveweather;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.net.URL;

import timber.log.Timber;

public class WeatherUpdateWorker extends Worker {

    private final Context mApplicationContext;

    public WeatherUpdateWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        mApplicationContext = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            URL url = new URL(getInputData().getString(Constants.REQUEST_URL));
            String weatherData = WeatherHelper.getWeatherData(url);
            Timber.d(weatherData);
            WorkerUtils.displayNotification(mApplicationContext.getString(R.string.weather_update_title), mApplicationContext);
            return Result.success(new Data.Builder().putString(Constants.WEATHER_UPDATE_RESULT, weatherData).build());
        } catch (Exception error) {
            Timber.e(error);
            return Result.failure();
        }
    }
}

