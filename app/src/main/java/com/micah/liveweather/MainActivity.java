package com.micah.liveweather;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_API_URL = "http://api.openweathermap.org/data/2.5/weather";
    private static final String API_QUERY_PARAMETER_KEY = "appid";
    private static final String API_KEY = "f4b7c212a14a8efd5a0f08e9cf50a192";
    private static final String API_LOCATION_TEXT = "q";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

//        URL url = buildURL(234826);

        displayContent();
    }


    private void displayContent() {
        try {
            URL url = buildURL("Lagos");
            new WeatherQueryTask().execute(url);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
    }

    protected URL buildURL(String location) {
//        String DAY_WEATHER_API_URL = BASE_API_URL + location;
        URL url = null;
        Uri uri = Uri.parse(BASE_API_URL).buildUpon()
                .appendQueryParameter(API_QUERY_PARAMETER_KEY, API_KEY)
                .appendQueryParameter(API_LOCATION_TEXT, String.valueOf(location))
                .build();

        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    protected String getWeatherData(URL url) {
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.setReadTimeout(5000);
//            connection.setConnectTimeout(5000);

            int responseCode = connection.getResponseCode();
            InputStream stream = connection.getInputStream();

            Scanner scanner = new Scanner(stream);
            scanner.useDelimiter("\\A");

            boolean hasData = scanner.hasNext();
            return hasData ? scanner.next() : null;
        } catch (IOException e) {
            Log.e("WEATHER_APP", e.toString());
        } finally {
            connection.disconnect();
        }

        return null;
    }

    public Weather parseWeatherData(String json) {
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
            double weatherTime = Double.valueOf(jsonWeather.getString(WEATHER_TIME));

            weather = new Weather(description, temp, minTemp, maxTemp, feelsLikeTemp, weatherTime, icon);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class WeatherQueryTask extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String result = "";

            try {
                result = getWeatherData(url);
            } catch (Exception e) {
                Log.e("ERROR: ", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            TextView tvMainTemp = findViewById(R.id.tvMainTemp);
            TextView tvMinTemp = findViewById(R.id.tvNightTemp);
            TextView tvMaxTemp = findViewById(R.id.tvDayTemp);
            TextView tvWeatherDescription = findViewById(R.id.tvWeatherText);
            TextView tvFeelsLikeTemp = findViewById(R.id.tvPerceivedTemp);
            TextView tvUnitTemp = findViewById(R.id.tvUnitTemp);

            Weather todayWeather = parseWeatherData(result);
            tvMainTemp.setText(String.valueOf(todayWeather.temp));
            tvMinTemp.setText(String.valueOf(todayWeather.minTemp));
            tvMaxTemp.setText(String.valueOf(todayWeather.maxTemp));
            tvWeatherDescription.setText(todayWeather.description);
            tvFeelsLikeTemp.setText(String.valueOf(todayWeather.feelsLikeTemp));
            tvUnitTemp.setText(String.valueOf(tvUnitTemp));

            super.onPostExecute(result);
        }
    }
}