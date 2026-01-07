package com.example.firstapp.repository

import com.example.firstapp.server.ApiServices

class WeatherRepository(private val api: ApiServices) {
    suspend fun getCurrentWeather(lat: Double, lon: Double, unit: String, apiKey: String) =
        api.getCurrentWeather(lat, lon, unit, apiKey)

    suspend fun getForecastWeather(lat: Double, lon: Double, unit: String, apiKey: String) =
        api.getForecastWeather(lat, lon, unit, apiKey)

    suspend fun getCityList(q: String, limit: Int, apiKey: String) =
        api.getCityList(q, limit, apiKey)

    suspend fun getAirPollution(lat: Double, lon: Double, apiKey: String) =
        api.getAirPollution(lat, lon, apiKey)
}
