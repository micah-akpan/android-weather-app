package com.micah.liveweather;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.micah.liveweather.utils.TaskDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WeatherRepository {
    public static final String BASE_API_URL = "http://api.openweathermap.org/data/2.5/weather";
    public static final String API_QUERY_PARAMETER_KEY = "appid";
    public static final String API_KEY = "f4b7c212a14a8efd5a0f08e9cf50a192";
    public static final String API_LOCATION_TEXT_KEY = "q";
    public static URL WEATHER_REQUEST_URL;
    private static WeatherQueryAsyncTask mWeatherQueryAsyncTask;

    public static URL buildURL(String location) {
        URL url = null;
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(API_LOCATION_TEXT_KEY, location)
                .appendQueryParameter(API_QUERY_PARAMETER_KEY, API_KEY)
                .build();
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static URL buildURL(double latitude, double longitude) {
        URL url = null;
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter("lat", String.valueOf(latitude))
                .appendQueryParameter("lon", String.valueOf(longitude))
                .appendQueryParameter(API_QUERY_PARAMETER_KEY, API_KEY)
                .build();

        try {
            url = new URL(uri.toString());
            setWeatherRequestUrl(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getWeatherData(@NonNull URL url) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                InputStream stream = connection.getInputStream();

                Scanner scanner = new Scanner(stream);
                scanner.useDelimiter("\\A");

                boolean hasData = scanner.hasNext();
                return hasData ? scanner.next() : null;
            } else {
                return null;
            }
        } catch (IOException e) {
           e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }

        return null;
    }

    public static void setWeatherRequestUrl(URL url) {
        WEATHER_REQUEST_URL = url;
    }

    public static WeatherQueryAsyncTask getWeatherQueryAsyncTask() {
        if (mWeatherQueryAsyncTask == null) {
            mWeatherQueryAsyncTask = new WeatherQueryAsyncTask();
        }
        return mWeatherQueryAsyncTask;
    }

    public void fetchWeatherData(URL url) {
        getWeatherQueryAsyncTask().execute(url);
    }

    public LiveData<String> getWeatherData() {
        mWeatherQueryAsyncTask = getWeatherQueryAsyncTask();
        return mWeatherQueryAsyncTask.mWeatherData;
    }

    public MutableLiveData<Integer> getWeatherFetchProgress() {
        return getWeatherQueryAsyncTask().getWeatherFetchProgress();
    }


    public static class WeatherQueryAsyncTask extends AsyncTask<URL, Integer, String> {
        private final MutableLiveData<Integer> mWeatherFetchProgress;
        private final MutableLiveData<String> mWeatherData;

        public WeatherQueryAsyncTask() {
            super();
            mWeatherData = new MutableLiveData<>();
            mWeatherFetchProgress = new MutableLiveData<>();
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String result = null;

            try {
                publishProgress(2);
                result = getWeatherData(url);
                publishProgress(3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            mWeatherFetchProgress.setValue(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mWeatherData.setValue(result);
        }

        public MutableLiveData<Integer> getWeatherFetchProgress() {
            return mWeatherFetchProgress;
        }
    }
}
