package com.example.weatherapplication.api

import android.util.Log
import com.example.weatherapplication.data.Forecast
import com.example.weatherapplication.data.Weather
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapApi {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") location: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Weather

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") location: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Forecast

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Weather {
        Log.d("OpenWeatherMapApi", "API request URL: ${RetrofitInstance.BASE_URL}weather?lat=$lat&lon=$lon&appid=$apiKey&units=$units")
        return RetrofitInstance.api.getCurrentWeather(lat, lon, apiKey, units)
    }

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Forecast {
        Log.d("OpenWeatherMapApi", "API request URL: ${RetrofitInstance.BASE_URL}forecast?lat=$lat&lon=$lon&appid=$apiKey&units=$units")
        return RetrofitInstance.api.getForecast(lat, lon, apiKey, units)
    }

}
