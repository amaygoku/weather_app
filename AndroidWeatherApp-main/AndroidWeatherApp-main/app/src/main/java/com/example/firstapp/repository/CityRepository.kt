package com.example.firstapp.repository

import com.example.firstapp.server.ApiServices

class CityRepository(private val api: ApiServices) {
    /**
     * Gọi API tìm kiếm danh sách thành phố theo tên
     * @param q Tên thành phố cần tìm
     * @param limit Số lượng kết quả tối đa
     * @param apiKey API Key của OpenWeatherMap
     * @return Response chứa danh sách thành phố
     */
    suspend fun getCities(q: String, limit: Int, apiKey: String) =
        api.getCityList(q, limit, apiKey)
}