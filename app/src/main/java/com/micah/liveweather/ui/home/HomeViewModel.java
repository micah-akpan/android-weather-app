package com.micah.liveweather.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.micah.liveweather.Constants;
import com.micah.liveweather.Weather;
import com.micah.liveweather.WeatherRepository;
import com.micah.liveweather.WeatherUpdateWorker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeViewModel extends AndroidViewModel {
    private final WorkManager workManager;
    public final LiveData<List<WorkInfo>> workInfos;
    public final double LAGOS_NG_LATITUDE = 6.5801382;
    public final double LAGOS_NG_LONGITUDE = 3.3415503;
    public boolean isPWeatherUpdateScheduled = false;
    private final LiveData<String> mWeatherData;

    private final MutableLiveData<Integer> mWeatherFetchProgress;

    WeatherRepository mWeatherRepository;

    public HomeViewModel(Application application) {
        super(application);
        workManager = WorkManager.getInstance();
        workInfos = workManager.getWorkInfosByTagLiveData(Constants.WEATHER_UPDATE_WORKER_TAG);
        mWeatherRepository = new WeatherRepository();
        mWeatherData = mWeatherRepository.getWeatherData();
        mWeatherFetchProgress = mWeatherRepository.getWeatherFetchProgress();
    }

    public WorkRequest createWeatherUpdateWorker(Data.Builder inputData) {
        this.isPWeatherUpdateScheduled = true;
        Constraints constraints = new Constraints.Builder()
                .setRequiresDeviceIdle(true)
                .setRequiresBatteryNotLow(true)
                .build();

        return new PeriodicWorkRequest
                .Builder(WeatherUpdateWorker.class, 15, TimeUnit.MINUTES)
                .addTag(Constants.WEATHER_UPDATE_WORKER_TAG)
                .setInputData(inputData.build())
                .setConstraints(constraints)
                .build();
    }

    public void scheduleWeatherUpdate(double latitude, double longitude) {
        URL url = WeatherRepository.buildURL(latitude, longitude);
        Data.Builder data = new Data.Builder();
        data.putString(Constants.REQUEST_URL, url.toString());

        WorkRequest weatherUpdateRequest = createWeatherUpdateWorker(data);
        workManager.enqueue(weatherUpdateRequest);
    }

    void fetchWeather(URL weatherUrl) {
        if (weatherUrl != null) {
            mWeatherRepository.fetchWeatherData(weatherUrl);
        }
    }

    public Weather parseWeatherData(String weatherJSON) {
        Weather weather = null;

        final String WEATHER_TITLE = "weather";
        final String WEATHER_DESCRIPTION = "description";
        final String WEATHER_ICON = "icon";
        final String TEMP = "temp";
        final String MIN_TEMP = "temp_min";
        final String MAX_TEMP = "temp_max";
        final String FEELS_TEMP = "feels_like";
        final String WEATHER_TIME = "dt";

        try {
            JSONObject jsonWeather = new JSONObject(weatherJSON);
            JSONArray jsonWeatherObjArray = jsonWeather.getJSONArray(WEATHER_TITLE);
            JSONObject jsonWeatherObj = jsonWeatherObjArray.getJSONObject(0);
            String description = jsonWeatherObj.getString(WEATHER_DESCRIPTION);
            String icon = jsonWeatherObj.getString(WEATHER_ICON);
            JSONObject jsonWeatherMain = jsonWeather.getJSONObject("main");

            double temp = Double.parseDouble(jsonWeatherMain.getString(TEMP));
            double feelsLikeTemp = Double.parseDouble(jsonWeatherMain.getString(FEELS_TEMP));
            double maxTemp = Double.parseDouble(jsonWeatherMain.getString(MAX_TEMP));
            double minTemp = Double.parseDouble(jsonWeatherMain.getString(MIN_TEMP));
            double weatherTime = Double.parseDouble(jsonWeather.getString(WEATHER_TIME));

            weather = new Weather(description, temp, minTemp, maxTemp, feelsLikeTemp, weatherTime, icon);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;
    }

    public LiveData<String> getWeatherData() {
        return mWeatherData;
    }

    public MutableLiveData<Integer> getWeatherFetchProgress() {
        return mWeatherFetchProgress;
    }
}