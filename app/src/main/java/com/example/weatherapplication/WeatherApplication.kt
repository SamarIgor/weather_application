package com.example.weatherapplication

import android.app.Application

class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DefaultLocationHolder.initialize(this)
    }
}

