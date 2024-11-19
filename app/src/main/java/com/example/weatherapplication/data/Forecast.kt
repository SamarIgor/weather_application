package com.example.weatherapplication.data

data class Forecast(
    val list: List<HourlyForecast>
) {
    data class HourlyForecast(
        val dt_txt: String,
        val main: Main,
        val weather: List<WeatherItem>
    ) {
        data class Main(
            val temp: Double
        )

        data class WeatherItem(
            val description: String,
            val icon: String
        )
    }


}
