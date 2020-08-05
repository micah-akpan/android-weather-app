package com.micah.liveweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

import java.net.URL;

public class MainActivity extends AppCompatActivity {
    public static final String OPENWEATHERMAP_BASE_IMAGE_URL = "http://openweathermap.org/img/wn/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        displayContent();
    }


    private void displayContent() {
        try {
            URL url = WeatherHelper.buildURL("Lagos");
            new WeatherQueryTask().execute(url);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
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
                result = WeatherHelper.getWeatherData(url);
            } catch (Exception e) {
               e.printStackTrace();
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
            TextView tvDateTemp = findViewById(R.id.tvDateTime);
            ImageView ivWeatherImage = findViewById(R.id.ivWeatherImage);

            Weather todayWeather = WeatherHelper.parseWeatherData(result);

            int mainTemp = (int) todayWeather.convertToCelsius('K');
            int minTemp = (int) todayWeather.convertToCelsius(todayWeather.minTemp, 'K');
            int maxTemp = (int) todayWeather.convertToCelsius(todayWeather.maxTemp, 'K');
            int feelsLikeTemp = (int) todayWeather.convertToCelsius(todayWeather.feelsLikeTemp, 'K');
            String imageUrl = OPENWEATHERMAP_BASE_IMAGE_URL + todayWeather.icon + "@2x.png";


            tvMainTemp.setText(String.valueOf(mainTemp));
            tvMinTemp.setText(String.valueOf(minTemp));
            tvMaxTemp.setText(String.valueOf(maxTemp));
            tvWeatherDescription.setText(todayWeather.capitalizeDescription());
            tvFeelsLikeTemp.setText("Feels like " + String.valueOf(feelsLikeTemp));
            tvUnitTemp.setText(String.valueOf(todayWeather.getCurrentUnit()));

            todayWeather.setWeatherImage(getApplicationContext(), imageUrl, ivWeatherImage);
            tvDateTemp.setText(Weather.getWeatherTime());

            super.onPostExecute(result);
        }
    }
}