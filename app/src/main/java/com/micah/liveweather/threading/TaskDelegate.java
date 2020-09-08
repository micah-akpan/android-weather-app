package com.micah.liveweather.threading;

import com.micah.liveweather.Weather;

public interface TaskDelegate {
    Weather onWeatherFetchSuccess();
    void onWeatherPreFetchReq(int initialProgress);
    void onWeatherPostFetchReq(String result);
    void onWeatherFetchReqProgress(int progress);
}
