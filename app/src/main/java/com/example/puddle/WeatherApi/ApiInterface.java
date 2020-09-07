package com.example.puddle.WeatherApi;

import com.example.puddle.WeatherModel.CityName;
import com.example.puddle.WeatherModel.WeatherResult;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("onecall")
    Call<WeatherResult> getWeather(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("exclude") String exclude,
            @Query("appid") String appid
    );

    @GET("weather")
    Call<CityName> getCityName(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("appid") String appid
    );

    @GET("weather")
    Call<WeatherResult> getCityCoord(
            @Query("q") String name,
            @Query("appid") String appid
    );
}