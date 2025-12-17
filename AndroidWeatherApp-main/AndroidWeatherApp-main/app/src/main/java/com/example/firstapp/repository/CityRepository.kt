package com.example.firstapp.repository

import com.example.firstapp.server.ApiServices

class CityRepository(private val api: ApiServices) {
    suspend fun getCities(q: String, limit: Int, apiKey: String) =
        api.getCityList(q, limit, apiKey)
}