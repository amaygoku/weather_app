package com.example.firstapp.server

import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("appid") ApiKey:String,
    ): Response<CurrentResponseApi>

    @GET("data/2.5/forecast")
    suspend fun getForecastWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("appid") ApiKey:String,
    ): Response<ForecastResponseApi>

    @GET("geo/1.0/direct")
    suspend fun getCityList(
        @Query("q") q:String,
        @Query("limit") limit:Int,
        @Query("appid") ApiKey:String
    ): Response<CityResponseApi>
}