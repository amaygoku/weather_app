package com.example.firstapp.repository

import com.example.firstapp.server.ApiServices

class WeatherRepository(private val api: ApiServices) {
    /**
     * Gọi API lấy dữ liệu thời tiết hiện tại
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param unit Đơn vị nhiệt độ
     * @param apiKey API Key của OpenWeatherMap
     * @return Response chứa dữ liệu thời tiết hiện tại
     */
    suspend fun getCurrentWeather(lat: Double, lon: Double, unit: String, apiKey: String) =
        api.getCurrentWeather(lat, lon, unit, apiKey)

    /**
     * Gọi API lấy dữ liệu dự báo thời tiết 5 ngày
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param unit Đơn vị nhiệt độ
     * @param apiKey API Key của OpenWeatherMap
     * @return Response chứa dữ liệu dự báo thời tiết
     */
    suspend fun getForecastWeather(lat: Double, lon: Double, unit: String, apiKey: String) =
        api.getForecastWeather(lat, lon, unit, apiKey)

    /**
     * Gọi API tìm kiếm danh sách thành phố theo tên
     * @param q Tên thành phố cần tìm
     * @param limit Số lượng kết quả tối đa
     * @param apiKey API Key của OpenWeatherMap
     * @return Response chứa danh sách thành phố
     */
    suspend fun getCityList(q: String, limit: Int, apiKey: String) =
        api.getCityList(q, limit, apiKey)

    /**
     * Gọi API lấy dữ liệu chất lượng không khí (AQI)
     * @param lat Vĩ độ
     * @param lon Kinh độ
     * @param apiKey API Key của OpenWeatherMap
     * @return Response chứa dữ liệu chất lượng không khí
     */
    suspend fun getAirPollution(lat: Double, lon: Double, apiKey: String) =
        api.getAirPollution(lat, lon, apiKey)
}
