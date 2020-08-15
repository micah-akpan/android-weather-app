package com.micah.liveweather;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class WeatherHelper {
    public static final String BASE_API_URL = "http://api.openweathermap.org/data/2.5/weather";
    public static final String API_QUERY_PARAMETER_KEY = "appid";
    public static final String API_KEY = "f4b7c212a14a8efd5a0f08e9cf50a192";
    public static final String API_LOCATION_TEXT = "q";
    public static final String OPEN_WEATHER_MAP_BASE_IMAGE_URL = "http://openweathermap.org/img/wn/";

    public static URL buildURL(String location) {
        URL url = null;
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(API_LOCATION_TEXT, location)
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
                return "";
            }
        } catch (IOException e) {
           e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
        }

        return "";
    }

    public static Weather parseWeatherData(String json) {
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
            JSONObject jsonWeather = new JSONObject(json);
            JSONArray jsonWeatherObjArray = jsonWeather.getJSONArray(WEATHER_TITLE);
            JSONObject jsonWeatherObj = jsonWeatherObjArray.getJSONObject(0);
            String description = jsonWeatherObj.getString(WEATHER_DESCRIPTION);
            String icon = jsonWeatherObj.getString(WEATHER_ICON);
            JSONObject jsonWeatherMain = jsonWeather.getJSONObject("main");

            double temp = Double.valueOf(jsonWeatherMain.getString(TEMP));
            double feelsLikeTemp = Double.valueOf(jsonWeatherMain.getString(FEELS_TEMP));
            double maxTemp = Double.valueOf(jsonWeatherMain.getString(MAX_TEMP));
            double minTemp = Double.valueOf(jsonWeatherMain.getString(MIN_TEMP));
            double weatherTime = Double.parseDouble(jsonWeather.getString(WEATHER_TIME));

            weather = new Weather(description, temp, minTemp, maxTemp, feelsLikeTemp, weatherTime, icon);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;
    }
}
