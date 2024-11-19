package com.example.weatherapplication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun WeatherIcon(weatherCondition: String, modifier: Modifier = Modifier) {
    val icon = when (weatherCondition) {
        "clear" -> R.drawable.clear
        "clouds" -> R.drawable.cloudy
        "rain" -> R.drawable.rain
        "snow" -> R.drawable.snowy
        "thunderstorm" -> R.drawable.thunderstorm
        "drizzle" -> R.drawable.rain
        "mist" -> R.drawable.fog
        else -> R.drawable.sunny
    }

    Image(
        painter = painterResource(id = icon),
        contentDescription = "Weather Icon",
        modifier = modifier.size(36.dp)
    )
}
