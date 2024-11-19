package com.example.weatherapplication.data

data class Weather(
    val main: Main,
    val weather: List<WeatherItem>,
    val wind: Wind,
    val sys: Sys,
    val name: String
) {
    data class Main(
        val temp: Double,
        val humidity: Int,
        val pressure: Double,
        val feels_like: Double
    )

    data class WeatherItem(
        val description: String,
        val icon: String
    )

    data class Wind(
        val speed: Double,
        val deg: Double
    )

    data class Sys(
        val sunrise: Long,
        val sunset: Long
    )
}
