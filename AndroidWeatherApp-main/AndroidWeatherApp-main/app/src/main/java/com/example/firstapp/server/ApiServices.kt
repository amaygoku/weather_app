package com.example.firstapp.server

import com.example.firstapp.model.AirPollutionResponseApi
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    /**
     * API lấy thông tin thời tiết hiện tại
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param unit Đơn vị nhiệt độ (metric/imperial)
     * @param ApiKey API Key của OpenWeatherMap
     * @return Response chứa dữ liệu thời tiết hiện tại
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("appid") ApiKey:String,
    ): Response<CurrentResponseApi>

    /**
     * API lấy thông tin dự báo thời tiết 5 ngày
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param unit Đơn vị nhiệt độ (metric/imperial)
     * @param ApiKey API Key của OpenWeatherMap
     * @return Response chứa dữ liệu dự báo thời tiết
     */
    @GET("data/2.5/forecast")
    suspend fun getForecastWeather(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("units") unit:String,
        @Query("appid") ApiKey:String,
    ): Response<ForecastResponseApi>

    /**
     * API tìm kiếm danh sách thành phố theo tên
     * @param q Tên thành phố cần tìm
     * @param limit Số lượng kết quả tối đa
     * @param ApiKey API Key của OpenWeatherMap
     * @return Response chứa danh sách thành phố
     */
    @GET("geo/1.0/direct")
    suspend fun getCityList(
        @Query("q") q:String,
        @Query("limit") limit:Int,
        @Query("appid") ApiKey:String
    ): Response<CityResponseApi>

    /**
     * API lấy thông tin chất lượng không khí (AQI)
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param ApiKey API Key của OpenWeatherMap
     * @return Response chứa dữ liệu chất lượng không khí
     */
    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") lat:Double,
        @Query("lon") lon:Double,
        @Query("appid") ApiKey:String
    ): Response<AirPollutionResponseApi>
}
