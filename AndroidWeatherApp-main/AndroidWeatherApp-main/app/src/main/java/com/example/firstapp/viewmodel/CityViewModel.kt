package com.example.firstapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.repository.CityRepository
import com.example.firstapp.server.ApiClient
import com.example.firstapp.server.ApiServices
import kotlinx.coroutines.launch

class CityViewModel(private val repository: CityRepository) : ViewModel() {

    // Constructor for creating an instance without a factory
    constructor() : this(CityRepository(ApiClient().getClient().create(ApiServices::class.java)))

    private val _cities = MutableLiveData<CityResponseApi>()
    val cities: LiveData<CityResponseApi> = _cities

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val apiKey = "bde3155f96b4e71b2d4f5546b52701d8" // Move to a secure place

    fun loadCities(q: String, limit: Int) {
        viewModelScope.launch {
            try {
                val response = repository.getCities(q, limit, apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _cities.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Exception: ${e.message}")
            }
        }
    }
}
