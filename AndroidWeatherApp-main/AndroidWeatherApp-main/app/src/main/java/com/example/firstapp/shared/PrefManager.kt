package com.example.firstapp.shared

import android.content.Context
import android.content.SharedPreferences
import com.example.firstapp.model.CityResponseApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PrefManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences("WeatherPref", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveLocation(lat: Double, lon: Double, cityName: String) {
        pref.edit().apply {
            putFloat("lat", lat.toFloat())
            putFloat("lon", lon.toFloat())
            putString("city", cityName)
            apply()
        }
        addCityToHistory(cityName, lat, lon)
    }

    // Quản lý danh sách Yêu thích
    fun getFavorites(): MutableList<CityResponseApi.CityResponseItem> {
        val json = pref.getString("favorites", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<CityResponseApi.CityResponseItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun toggleFavorite(city: CityResponseApi.CityResponseItem) {
        val favorites = getFavorites()
        val exists = favorites.find { it.name == city.name && it.lat == city.lat }
        if (exists != null) {
            favorites.remove(exists)
        } else {
            favorites.add(city)
        }
        pref.edit().putString("favorites", gson.toJson(favorites)).apply()
    }

    fun isFavorite(cityName: String?): Boolean {
        return getFavorites().any { it.name == cityName }
    }

    // Lịch sử tìm kiếm
    private fun addCityToHistory(name: String, lat: Double, lon: Double) {
        val history = getHistory().toMutableList()
        history.removeAll { it.name == name }
        history.add(0, CityResponseApi.CityResponseItem(null, lat, null, lon, name, null))
        val limitedHistory = if (history.size > 5) history.take(5) else history
        pref.edit().putString("search_history", gson.toJson(limitedHistory)).apply()
    }

    fun getHistory(): List<CityResponseApi.CityResponseItem> {
        val json = pref.getString("search_history", null) ?: return emptyList()
        val type = object : TypeToken<List<CityResponseApi.CityResponseItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun loadData() {
        SharedData.sharedLatitude = pref.getFloat("lat", 21.0285f).toDouble()
        SharedData.sharedLongitude = pref.getFloat("lon", 105.8542f).toDouble()
        SharedData.sharedCity = pref.getString("city", "Hanoi") ?: "Hanoi"
        SharedData.sharedUnit = pref.getString("unit", "metric") ?: "metric"
    }

    fun saveUnit(unit: String) {
        pref.edit().putString("unit", unit).apply()
    }
}
