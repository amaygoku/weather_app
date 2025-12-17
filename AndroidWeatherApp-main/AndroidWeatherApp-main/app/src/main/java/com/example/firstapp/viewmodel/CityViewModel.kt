package com.example.firstapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.firstapp.repository.CityRepository
import com.example.firstapp.server.ApiClient
import com.example.firstapp.server.ApiServices

class CityViewModel(val repository:CityRepository) :ViewModel() {
    constructor():this(CityRepository(ApiClient().getClient().create(ApiServices::class.java)))

    fun loadCities(q:String, limit:Int) =
        repository.getCities(q, limit)
}