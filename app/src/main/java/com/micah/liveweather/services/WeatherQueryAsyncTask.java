package com.micah.liveweather.services;

import android.os.AsyncTask;

import com.micah.liveweather.WeatherHelper;
import com.micah.liveweather.utils.TaskDelegate;

import java.lang.ref.WeakReference;
import java.net.URL;

public class WeatherQueryAsyncTask extends AsyncTask<URL, Integer, String> {
    private WeakReference<TaskDelegate> mDelegate;

    public WeatherQueryAsyncTask(TaskDelegate delegate) {
        super();
        mDelegate = new WeakReference(delegate);
    }

    @Override
    protected void onPreExecute() {
        mDelegate.get().onWeatherPreFetchReq(1);
    }

    @Override
    protected String doInBackground(URL... urls) {
        URL url = urls[0];
        String result = null;

        try {
            publishProgress(2);
            result = WeatherHelper.getWeatherData(url);
            publishProgress(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mDelegate.get().onWeatherFetchReqProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mDelegate.get().onWeatherPostFetchReq(result);
    }
}

