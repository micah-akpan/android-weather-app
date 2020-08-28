package com.micah.liveweather.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.micah.liveweather.MathHelper;
import com.micah.liveweather.R;
import com.micah.liveweather.Weather;
import com.micah.liveweather.WeatherHelper;
import com.micah.liveweather.WeatherUpdateWorker;

import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private HomeViewModel homeViewModel;
    private FragmentActivity mFragmentActivity;
    public static final String OPENWEATHERMAP_BASE_IMAGE_URL = "http://openweathermap.org/img/wn/";
    TextView mTvMainTemp;

    double mWeatherTemp = 0;
    private TextView mTvUnitTemp;
    private FusedLocationProviderClient mFusedLocationClient;
    private String mLocationSearchQuery;

    private static class Coord {
        static double longitude;
        static double latitude;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mFragmentActivity = getActivity();
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

        mTvMainTemp = root.findViewById(R.id.tvMainTemp);
        mTvUnitTemp = root.findViewById(R.id.tvUnitTemp);

        Button celsiusDrawerBtn = mFragmentActivity.findViewById(R.id.tvDrawerCelsius);
        Button fahrDrawerBtn = mFragmentActivity.findViewById(R.id.tvDrawerFahr);
        final DrawerLayout drawerLayout = mFragmentActivity.findViewById(R.id.drawer_layout);
        celsiusDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertTempToCelsius();
                drawerLayout.closeDrawers();
            }
        });

        fahrDrawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                convertTempToFahr();
                drawerLayout.closeDrawers();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mFragmentActivity);
        setHasOptionsMenu(true);

        PreferenceManager.setDefaultValues(mFragmentActivity, R.xml.general_preferences, false);
        return root;
    }

    public void startScheduledWork() {
        URL url = WeatherHelper.buildURL(Coord.latitude, Coord.longitude);
        WorkRequest weatherUpdateRequest = new PeriodicWorkRequest
                 .Builder(WeatherUpdateWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(
                        new Data.Builder()
                        .putString("REQUEST_URL", url.toString())
                        .build()
                )
                .build();
        WorkManager.getInstance(mFragmentActivity).enqueue(weatherUpdateRequest);

        // get result
        WorkManager.getInstance(mFragmentActivity)
                .getWorkInfoByIdLiveData(weatherUpdateRequest.getId())
                .observe(getViewLifecycleOwner(), info -> {
                    Log.d("HomeFragment", info.getId().toString());
                   if (info != null && info.getState().isFinished()) {
                       String myResult = info.getOutputData().getString("WEATHER_UPDATE_RESULT");
                       Log.d("HomeFragment Result", myResult);
                   }
                });
    }

    public boolean userHasPermission() {
        return ActivityCompat.checkSelfPermission(mFragmentActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!userHasPermission()) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
        this.updateNavHeaderInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        startScheduledWork();
    }

    public void updateNavHeaderInfo() {
        NavigationView navigationView = mFragmentActivity.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView tvNavHeaderUserName = headerView.findViewById(R.id.tvUsername);
        TextView tvNavHeaderUserlocation = headerView.findViewById(R.id.tvUserLocation);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mFragmentActivity);
        String userName = pref.getString(getResources().getString(R.string.pref_display_name_key), "");

        tvNavHeaderUserName.setText(userName);
        tvNavHeaderUserlocation.setText("Amsterdam");
    }

    @SuppressLint("MissingPermission")
    public void getUserLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(mFragmentActivity, location -> {
                    if (location != null) {
                        Coord.latitude = location.getLatitude();
                        Coord.longitude = location.getLongitude();

                        displayWeatherInfo(Coord.latitude, Coord.longitude);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            getUserLocation();
        } else {
            mFragmentActivity.finish();
        }
    }

    private void convertTempToCelsius() {
        if (Weather.currentUnit == 'F') {
            double newTemp = MathHelper.round(Weather.convertTemp(mWeatherTemp, 'C'));
            mWeatherTemp = newTemp;
            mTvMainTemp.setText(String.valueOf(Math.round(mWeatherTemp)));
            Weather.setCurrentUnit('C');
            mTvUnitTemp.setText(String.valueOf(Weather.currentUnit));
        }
    }

    private void convertTempToFahr() {
        if (Weather.currentUnit == 'C') {
            double newTemp = MathHelper.round(Weather.convertTemp(mWeatherTemp, 'F'), 0);
            mWeatherTemp = newTemp;
            mTvMainTemp.setText(String.valueOf(Math.round(mWeatherTemp)));
            Weather.setCurrentUnit('F');
            mTvUnitTemp.setText(String.valueOf(Weather.currentUnit));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

//        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) searchItem.getActionView();
//
//        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchView.setQueryHint(getResources().getString(R.string.search_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mLocationSearchQuery = query;
                displayWeatherInfo(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void displayWeatherInfo(String location) {
        try {
            URL url = WeatherHelper.buildURL(location);
            new HomeFragment.WeatherQueryTask().execute(url);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
    }

    private void displayWeatherInfo(double lat, double lon) {
        try {
            URL url = WeatherHelper.buildURL(lat, lon);
            new HomeFragment.WeatherQueryTask().execute(url);
        } catch (Exception e) {
            Log.e("Error: ", e.toString());
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) { }

    public class WeatherQueryTask extends AsyncTask<URL, Integer, String> {

        private View mView = getView();
        private ProgressBar mProgressBar;

        @Override
        protected void onPreExecute() {
            mProgressBar = (ProgressBar) mView.findViewById(R.id.wUpdateProgressBar);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(1);
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
           int progressValue = progress[0];
           mProgressBar.setProgress(progressValue);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                mView = getView();
                TextView tvMainTemp = mView.findViewById(R.id.tvMainTemp);
                TextView tvMinTemp = mView.findViewById(R.id.tvNightTemp);
                TextView tvMaxTemp = mView.findViewById(R.id.tvDayTemp);
                TextView tvWeatherDescription = mView.findViewById(R.id.tvWeatherText);
                TextView tvFeelsLikeTemp = mView.findViewById(R.id.tvPerceivedTemp);
                TextView tvUnitTemp = mView.findViewById(R.id.tvUnitTemp);
                TextView tvDateTemp = mView.findViewById(R.id.tvDateTime);
                ImageView ivWeatherImage = mView.findViewById(R.id.ivWeatherImage);

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
                tvUnitTemp.setText(String.valueOf(Weather.currentUnit));

                todayWeather.setWeatherImage(mFragmentActivity.getApplicationContext(), imageUrl, ivWeatherImage);
                tvDateTemp.setText(Weather.getWeatherTime());

                mWeatherTemp = Double.parseDouble(String.valueOf(mainTemp));
                mProgressBar.setVisibility(View.GONE);

                super.onPostExecute(result);
            } else {
                mProgressBar.setVisibility(View.GONE);
                // TODO: Show a toast here or something
                if (mFragmentActivity != null) {
                    Toast.makeText(mFragmentActivity, "There are no weather information for " + mLocationSearchQuery + " at this time", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}