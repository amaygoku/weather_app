package com.example.firstapp.repository

import com.example.firstapp.BuildConfig
import com.example.firstapp.server.ApiServices

class CityRepository(val api: ApiServices) {
    fun getCities(q:String, limit:Int) =
        api.getCityList(q, limit, BuildConfig.API_KEY)
}