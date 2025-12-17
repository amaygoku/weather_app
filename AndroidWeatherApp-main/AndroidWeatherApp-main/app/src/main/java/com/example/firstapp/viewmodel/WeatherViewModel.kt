package com.example.firstapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import com.example.firstapp.repository.WeatherRepository
import com.example.firstapp.server.ApiClient
import com.example.firstapp.server.ApiServices
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    // Constructor for creating an instance without a factory
    constructor() : this(WeatherRepository(ApiClient().getClient().create(ApiServices::class.java)))

    private val _currentWeather = MutableLiveData<CurrentResponseApi>()
    val currentWeather: LiveData<CurrentResponseApi> = _currentWeather

    private val _forecastWeather = MutableLiveData<ForecastResponseApi>()
    val forecastWeather: LiveData<ForecastResponseApi> = _forecastWeather

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val apiKey = "bde3155f96b4e71b2d4f5546b52701d8" // Move to a secure place

    fun loadCurrentWeather(lat: Double, lon: Double, unit: String) {
        viewModelScope.launch {
            try {
                val response = repository.getCurrentWeather(lat, lon, unit, apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _currentWeather.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Exception: ${e.message}")
            }
        }
    }

    fun loadForecastWeather(lat: Double, lon: Double, unit: String) {
        viewModelScope.launch {
            try {
                val response = repository.getForecastWeather(lat, lon, unit, apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _forecastWeather.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Exception: ${e.message}")
            }
        }
    }
}
