package com.example.weatherapplication.repository

import android.util.Log
import com.example.weatherapplication.api.RetrofitInstance
import com.example.weatherapplication.data.Forecast
import com.example.weatherapplication.data.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository {
    suspend fun getCurrentWeather(location: String): Weather {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.api.getCurrentWeather(location, "ea358312c6cd052d15579728afe3a41e")
        }
    }


    suspend fun getForecast(location: String): Forecast {
        return withContext(Dispatchers.IO) {
            RetrofitInstance.api.getForecast(location, "ea358312c6cd052d15579728afe3a41e")
        }
    }

    suspend fun getCurrentWeather(lat: Double, lon: Double): Weather {
        return withContext(Dispatchers.IO) {
            val weather = RetrofitInstance.api.getCurrentWeather(lat, lon, "ea358312c6cd052d15579728afe3a41e")
            Log.d("WeatherRepository", "Fetched weather data: \$weather")
            weather
        }
    }

    suspend fun getForecast(lat: Double, lon: Double): Forecast {
        return withContext(Dispatchers.IO) {
            val forecast = RetrofitInstance.api.getForecast(lat, lon, "ea358312c6cd052d15579728afe3a41e")
            Log.d("WeatherRepository", "Fetched forecast data: \$forecast")
            forecast
        }
    }
}