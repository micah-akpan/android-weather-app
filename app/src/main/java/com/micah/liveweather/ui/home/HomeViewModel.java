package com.micah.liveweather.ui.home;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.micah.liveweather.Constants;
import com.micah.liveweather.WeatherHelper;
import com.micah.liveweather.WeatherUpdateWorker;

import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HomeViewModel extends AndroidViewModel {

    private MutableLiveData<String> mText;
    private final WorkManager workManager;
    public final LiveData<List<WorkInfo>> workInfos;
    public final double LAGOS_LATITUDE = 6.5801382;
    public final double LAGOS_LONGITUDE = 3.3415503;
    public boolean isPWeatherUpdateScheduled = false;

    public HomeViewModel(Application application) {
        super(application);
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
        workManager = WorkManager.getInstance(application);
        workInfos = workManager.getWorkInfosByTagLiveData(Constants.WEATHER_UPDATE_WORKER_TAG);
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
//                .setConstraints(constraints)
                .build();
    }


    public void scheduleWeatherUpdate(double latitude, double longitude) {
        URL url = WeatherHelper.buildURL(latitude, longitude);
        Data.Builder data = new Data.Builder();
        data.putString(Constants.REQUEST_URL, url.toString());

        WorkRequest weatherUpdateRequest = createWeatherUpdateWorker(data);
        workManager.enqueue(weatherUpdateRequest);
    }
}