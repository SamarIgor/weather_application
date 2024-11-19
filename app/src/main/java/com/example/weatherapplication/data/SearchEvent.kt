package com.example.weatherapplication.data

sealed class SearchEvent {
    data class Search(val query: String) : SearchEvent()
    object ClearSearch : SearchEvent()
}
