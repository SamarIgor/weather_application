package com.example.weatherapplication.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val api: OpenWeatherMapApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherMapApi::class.java)
    }
}
