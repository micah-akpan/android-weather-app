package com.micah.liveweather.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.WorkInfo;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.micah.liveweather.MathHelper;
import com.micah.liveweather.R;
import com.micah.liveweather.Weather;
import com.micah.liveweather.WeatherHelper;
import com.micah.liveweather.services.WeatherQueryAsyncTask;
import com.micah.liveweather.utils.TaskDelegate;

import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.List;

import timber.log.Timber;

public class HomeFragment extends Fragment implements TaskDelegate {
    public static final double LAGOS_LATITUDE = 6.5801382;
    public static final double LAGOS_LONGITUDE = 3.3415503;
    private boolean mCanShowHourlyNotification = false;
    private boolean mUserHasSearched;
    private String lastSearchedPlace;
    private Resources mResources;

    private static class Coord {
        static double longitude;
        static double latitude;
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    public static final String WEATHER_NOTIFICATION_CHANNEL = "WEATHER_NOTIFICATION_CHANNEL";
    public static final int WEATHER_NOTIFICATION_ID = 1;
    public static final String RECENTLY_SEARCHED_LOCATION = "RECENTLY_SEARCHED_LOCATION";
    private HomeViewModel homeViewModel;
    private FragmentActivity mFragmentActivity;
    public static final String OPENWEATHERMAP_BASE_IMAGE_URL = "http://openweathermap.org/img/wn/";
    TextView mTvMainTemp;

    double mWeatherTemp = 0;
    private TextView mTvUnitTemp;
    private FusedLocationProviderClient mFusedLocationClient;
    private String mLocationSearchQuery;
    private AsyncTask<URL, Integer, String> mFetchWeatherAsyncTask;

    private ProgressBar mProgressBar;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SearchView mSearchView;
    private SharedPreferences mPref;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mFragmentActivity = getActivity();

        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        homeViewModel.workInfos.observe(getViewLifecycleOwner(), this.workInfoObserver());

        Button celsiusDrawerBtn = mFragmentActivity.findViewById(R.id.tvDrawerCelsius);
        Button fahrDrawerBtn = mFragmentActivity.findViewById(R.id.tvDrawerFahr);
        final DrawerLayout drawerLayout = mFragmentActivity.findViewById(R.id.drawer_layout);

        celsiusDrawerBtn.setOnClickListener(listener -> {
            convertTempToCelsius();
            drawerLayout.closeDrawers();
        });

        fahrDrawerBtn.setOnClickListener(listener -> {
            convertTempToFahr();
            drawerLayout.closeDrawers();
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mFragmentActivity);
        setHasOptionsMenu(true);

        PreferenceManager.setDefaultValues(mFragmentActivity, R.xml.general_preferences, false);

        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mTvMainTemp = root.findViewById(R.id.tvMainTemp);
        mTvUnitTemp = root.findViewById(R.id.tvUnitTemp);
        mSwipeRefreshLayout = root.findViewById(R.id.swipeRefresh);

        mResources = getResources();

        return root;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        mSwipeRefreshLayout.setOnRefreshListener(() ->
                {
                    String recentlySearchedLocation = mPref.getString(
                            RECENTLY_SEARCHED_LOCATION, "");

                    assert recentlySearchedLocation != null;
                    if (!recentlySearchedLocation.trim().equals("")) {
                        displayWeatherInfo(recentlySearchedLocation);
                    } else {
                        displayWeatherInfo(Coord.latitude, Coord.longitude);
                    }
                }
        );
    }

    @Override
    public Weather onWeatherFetchSuccess() {
        return null;
    }

    @Override
    public void onWeatherPreFetchReq(int initialProgress) {
        View view = getView();
        assert view != null;
        mProgressBar = view.findViewById(R.id.wUpdateProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(1);
    }

    @Override
    public void onWeatherPostFetchReq(String result) {
        if (result != null) {
            View mView = getView();
            assert mView != null;
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
            tvFeelsLikeTemp.setText(mResources.getString(R.string.weather_feels_like, feelsLikeTemp));
            tvUnitTemp.setText(String.valueOf(Weather.currentUnit));

            todayWeather.setWeatherImage(mFragmentActivity.getApplicationContext(), imageUrl, ivWeatherImage);
            tvDateTemp.setText(Weather.getWeatherTime());

            mWeatherTemp = Double.parseDouble(String.valueOf(mainTemp));
            mProgressBar.setVisibility(View.GONE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            if (mFragmentActivity != null) {
                Toast.makeText(mFragmentActivity, "There are no weather information for " + mLocationSearchQuery, Toast.LENGTH_SHORT)
                        .show();
            }
        }

        onRefreshComplete();
    }

    private void onRefreshComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onWeatherFetchReqProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public Observer<List<WorkInfo>> workInfoObserver() {
       return listLiveData -> {
               if (listLiveData != null) {
                   Timber.d(String.valueOf(listLiveData.size()));
               }
       };
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
            getUserLocationAndFetchWeatherData();
        }
        updateNavHeaderInfo();
    }

    @Override
    public void onPause() {
        Timber.d(userHasPermission() ? "Yes" : "No");
        if (userHasPermission()) {
           homeViewModel.scheduleWeatherUpdate(Coord.latitude, Coord.longitude);
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        if (this.mFetchWeatherAsyncTask != null) {
            this.mFetchWeatherAsyncTask.cancel(true);
        }
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        mSearchView = (SearchView) searchItem.getActionView();

        mSearchView.setQueryHint(mResources.getString(R.string.search_hint));
        addWeatherSearchListener();
    }

    void addWeatherSearchListener() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mLocationSearchQuery = query;
                mUserHasSearched = true;
                // Last searched location will be used to initiate weather fetch on swipe refresh
                SharedPreferences.Editor sharedPrefEditor = mPref.edit();
                sharedPrefEditor.putString(RECENTLY_SEARCHED_LOCATION, query.trim()).apply();
                displayWeatherInfo(query);

                // TODO: Replace this with a listener
                // Update details of the nav header when search completes
                updateNavHeaderInfo();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getUserLocationAndFetchWeatherData();
        } else {
            mFragmentActivity.finish();
        }
    }

    public void updateNavHeaderInfo() {
        NavigationView navigationView = mFragmentActivity.findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView tvNavHeaderUserName = headerView.findViewById(R.id.tvUsername);
        TextView tvNavHeaderUserlocation = headerView.findViewById(R.id.tvUserLocation);

        mPref = PreferenceManager.getDefaultSharedPreferences(mFragmentActivity);
        String userName = mPref.getString(mResources.getString(R.string.pref_display_name_key), "");
        String recentlySearchedLocation = mPref.getString(RECENTLY_SEARCHED_LOCATION, "");

        tvNavHeaderUserName.setText(userName);
        tvNavHeaderUserlocation.setText(recentlySearchedLocation);
    }

    @SuppressLint("MissingPermission")
    public void getUserLocationAndFetchWeatherData() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(mFragmentActivity, location -> {
                    if (location != null) {
                        Coord.latitude = location.getLatitude();
                        Coord.longitude = location.getLongitude();
                    } else {
                        Coord.latitude = homeViewModel.LAGOS_LATITUDE;
                        Coord.longitude = homeViewModel.LAGOS_LONGITUDE;
                    }
                    displayWeatherInfo(Coord.latitude, Coord.longitude);
                });
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

    private void displayWeatherInfo(String location) {
        lastSearchedPlace = location;
        try {
            URL url = WeatherHelper.buildURL(location);
            mFetchWeatherAsyncTask = new WeatherQueryAsyncTask(this).execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayWeatherInfo(double lat, double lon) {
        try {
            URL url = WeatherHelper.buildURL(lat, lon);
            mFetchWeatherAsyncTask = new WeatherQueryAsyncTask(this).execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}