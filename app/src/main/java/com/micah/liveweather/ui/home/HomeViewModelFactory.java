package com.micah.liveweather.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.micah.liveweather.WeatherRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final WeatherRepository mWeatherRepository;

    public HomeViewModelFactory(final WeatherRepository weatherRepository) {
        mWeatherRepository = weatherRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
       if (modelClass.isAssignableFrom(HomeViewModel.class)) {
           return (T) new HomeViewModel(mWeatherRepository);
       }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
