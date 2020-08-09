package com.micah.liveweather.ui.home;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.micah.liveweather.MainActivity;
import com.micah.liveweather.R;
import com.micah.liveweather.Weather;
import com.micah.liveweather.WeatherHelper;

import java.net.URL;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private Context mContext;
    public static final String OPENWEATHERMAP_BASE_IMAGE_URL = "http://openweathermap.org/img/wn/";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
//        View root = inflater.inflate(R.layout.activity_main2, container, false);
//        final TextView textView = root.findViewById(R.id.text_home);
//        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        displayContent();
        return root;
    }

    private void displayContent() {
        try {
            URL url = WeatherHelper.buildURL("Lagos");
            new HomeFragment.WeatherQueryTask().execute(url);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
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
            mContext = getContext();
            View view = getView();
            TextView tvMainTemp = view.findViewById(R.id.tvMainTemp);
            TextView tvMinTemp = view.findViewById(R.id.tvNightTemp);
            TextView tvMaxTemp = view.findViewById(R.id.tvDayTemp);
            TextView tvWeatherDescription = view.findViewById(R.id.tvWeatherText);
            TextView tvFeelsLikeTemp = view.findViewById(R.id.tvPerceivedTemp);
            TextView tvUnitTemp = view.findViewById(R.id.tvUnitTemp);
            TextView tvDateTemp = view.findViewById(R.id.tvDateTime);
            ImageView ivWeatherImage = view.findViewById(R.id.ivWeatherImage);

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

            todayWeather.setWeatherImage(mContext.getApplicationContext(), imageUrl, ivWeatherImage);
            tvDateTemp.setText(Weather.getWeatherTime());

            super.onPostExecute(result);
        }
    }
}