package com.example.weatherapplication.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapplication.DefaultLocationHolder
import com.example.weatherapplication.data.Forecast
import com.example.weatherapplication.data.SearchEvent
import com.example.weatherapplication.data.Weather
import com.example.weatherapplication.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {


    private val _currentLocationName = MutableStateFlow<String>("")
    val currentLocationName: StateFlow<String> = _currentLocationName.asStateFlow()

    private val _defaultLocation = MutableStateFlow<String>("Ljubljana")

    private val repository = WeatherRepository()
    private val _weatherData = MutableLiveData<Weather>()
    val weatherData: LiveData<Weather> = _weatherData

    private val _forecastData = MutableLiveData<Forecast>()
    val forecastData: LiveData<Forecast> = _forecastData

    fun fetchWeatherData(location: String) {
        Log.d("WeatherViewModel", "fetchWeatherData called with location: $location")
        viewModelScope.launch {
            try {
                val weather = repository.getCurrentWeather(location)
                _weatherData.value = weather
                _currentLocationName.value = weather.name
                val forecast = repository.getForecast(location)
                _forecastData.value = forecast
            } catch (e: Exception) {
                _currentLocationName.value = "Location not found"
            }
        }
    }


    init {
        Log.d("WeatherViewModel", "Initializing WeatherViewModel")
        _defaultLocation.value = DefaultLocationHolder.defaultLocation
        Log.d("WeatherViewModel", "Default location: ${DefaultLocationHolder.defaultLocation}")
        viewModelScope.launch {
            _defaultLocation.collectLatest { location ->
                Log.d("WeatherViewModel", "Default location changed: $location")
                fetchWeatherData(location)
            }
        }
    }

    fun onSearchEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.Search -> {
                fetchWeatherData(event.query)
            }
            is SearchEvent.ClearSearch -> {
                fetchWeatherData(_currentLocationName.value)
            }
        }
    }

    fun fetchWeatherData(lat: Double, lon: Double) {
        Log.d("WeatherViewModel", "fetchWeatherData called with location: $lat and $lon")
        viewModelScope.launch {
            val weather = repository.getCurrentWeather(lat, lon)
            _weatherData.value = weather
            Log.d("WeatherViewModel", "Fetched weather data: $weather")
            _currentLocationName.value = "My Location"
            val forecast = repository.getForecast(lat, lon)
            _forecastData.value = forecast
            Log.d("WeatherViewModel", "Fetched forecast data: $forecast")
        }
    }

    fun updateDefaultLocation(location: String) {
        Log.d("WeatherViewModel", "Updating default location: $location")
        _defaultLocation.value = location
        DefaultLocationHolder.defaultLocation = location
    }
}