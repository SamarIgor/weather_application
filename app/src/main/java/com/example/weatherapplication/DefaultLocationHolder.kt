package com.example.weatherapplication

import android.content.Context
import android.content.SharedPreferences

object DefaultLocationHolder {
    private const val PREF_NAME = "weather_preferences"
    private const val KEY_DEFAULT_LOCATION = "default_location"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var defaultLocation: String
        get() = sharedPreferences.getString(KEY_DEFAULT_LOCATION, "Ljubljana") ?: "Ljubljana"
        set(value) {
            sharedPreferences.edit().putString(KEY_DEFAULT_LOCATION, value).apply()
        }
}
