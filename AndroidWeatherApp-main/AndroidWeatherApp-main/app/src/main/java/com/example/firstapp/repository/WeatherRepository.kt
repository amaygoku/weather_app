package com.example.firstapp.repository

import com.example.firstapp.server.ApiServices
import com.example.firstapp.BuildConfig


class WeatherRepository(val api:ApiServices) {
    fun getCurrentWeather(lat:Double, lon:Double, unit:String) =
        api.getCurrentWeather(lat, lon, unit, BuildConfig.API_KEY)
    fun getForecastWeather(lat:Double, lon:Double, unit:String) =
        api.getForecastWeather(lat, lon, unit, BuildConfig.API_KEY)
}