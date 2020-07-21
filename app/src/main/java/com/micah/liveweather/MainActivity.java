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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String BASE_API_URL = "https://dataservice.accuweather.com/forecasts/v1/daily/1day/";
    private static final String API_QUERY_PARAMETER_KEY = "apikey";
    private static final String API_KEY = "hLA5vUyF9ufsXX9IYVrVZbkgQGG9f7AX";
    private static final String API_LOCATION_TEXT = "location";

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
            URL url = buildURL(234826);
            new WeatherQueryTask().execute(url);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
    }

    protected URL buildURL(int location) {
        String DAY_WEATHER_API_URL = BASE_API_URL + location;
        URL url = null;
        Uri uri = Uri.parse(DAY_WEATHER_API_URL).buildUpon()
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

            String next = scanner.next();

            boolean hasData = scanner.hasNext();
        } catch (IOException e) {
            Log.e("WEATHER_APP", e.toString());
        }

        return null;
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
        protected String doInBackground(URL... urls) {
            URL url = urls[0];
            String result = "";

            try {
                result = getWeatherData(url);
            } catch (Exception e) {
                Log.e("ERROR: ", e.getMessage());
            }
            return null;
        }
    }
}