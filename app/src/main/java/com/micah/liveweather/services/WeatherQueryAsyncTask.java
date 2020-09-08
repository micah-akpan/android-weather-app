package com.micah.liveweather.services;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.micah.liveweather.R;
import com.micah.liveweather.Weather;
import com.micah.liveweather.WeatherHelper;
import com.micah.liveweather.threading.TaskDelegate;

import java.net.URL;

public class WeatherQueryAsyncTask extends AsyncTask<URL, Integer, String> {
    private TaskDelegate mDelegate;

    public WeatherQueryAsyncTask(TaskDelegate delegate) {
        mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        this.mDelegate.onWeatherPreFetchReq(1);
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
        mDelegate.onWeatherFetchReqProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
//        if (result != null) {
//            mView = getView();
//            TextView tvMainTemp = mView.findViewById(R.id.tvMainTemp);
//            TextView tvMinTemp = mView.findViewById(R.id.tvNightTemp);
//            TextView tvMaxTemp = mView.findViewById(R.id.tvDayTemp);
//            TextView tvWeatherDescription = mView.findViewById(R.id.tvWeatherText);
//            TextView tvFeelsLikeTemp = mView.findViewById(R.id.tvPerceivedTemp);
//            TextView tvUnitTemp = mView.findViewById(R.id.tvUnitTemp);
//            TextView tvDateTemp = mView.findViewById(R.id.tvDateTime);
//            ImageView ivWeatherImage = mView.findViewById(R.id.ivWeatherImage);
//
//            Weather todayWeather = WeatherHelper.parseWeatherData(result);
//
//            int mainTemp = (int) todayWeather.convertToCelsius('K');
//            int minTemp = (int) todayWeather.convertToCelsius(todayWeather.minTemp, 'K');
//            int maxTemp = (int) todayWeather.convertToCelsius(todayWeather.maxTemp, 'K');
//            int feelsLikeTemp = (int) todayWeather.convertToCelsius(todayWeather.feelsLikeTemp, 'K');
//            String imageUrl = OPENWEATHERMAP_BASE_IMAGE_URL + todayWeather.icon + "@2x.png";
//
//            tvMainTemp.setText(String.valueOf(mainTemp));
//            tvMinTemp.setText(String.valueOf(minTemp));
//            tvMaxTemp.setText(String.valueOf(maxTemp));
//            tvWeatherDescription.setText(todayWeather.capitalizeDescription());
//            tvFeelsLikeTemp.setText("Feels like " + String.valueOf(feelsLikeTemp));
//            tvUnitTemp.setText(String.valueOf(Weather.currentUnit));
//
//            todayWeather.setWeatherImage(mFragmentActivity.getApplicationContext(), imageUrl, ivWeatherImage);
//            tvDateTemp.setText(Weather.getWeatherTime());
//
//            mWeatherTemp = Double.parseDouble(String.valueOf(mainTemp));
//            mProgressBar.setVisibility(View.GONE);
//
//            super.onPostExecute(result);
//        } else {
//            mProgressBar.setVisibility(View.GONE);
//            // TODO: Show a toast here or something
//            if (mFragmentActivity != null) {
//                Toast.makeText(mFragmentActivity, "There are no weather information for " + mLocationSearchQuery + " at this time", Toast.LENGTH_SHORT).show();
//            }
//        }

        mDelegate.onWeatherPostFetchReq(result);
    }
}

