package com.example.firstapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstapp.model.AirPollutionResponseApi
import com.example.firstapp.model.CurrentResponseApi
import com.example.firstapp.model.ForecastResponseApi
import com.example.firstapp.repository.WeatherRepository
import com.example.firstapp.server.ApiClient
import com.example.firstapp.server.ApiServices
import kotlinx.coroutines.launch

class WeatherViewModel(private val repository: WeatherRepository) : ViewModel() {

    constructor() : this(WeatherRepository(ApiClient().getClient().create(ApiServices::class.java)))

    private val _currentWeather = MutableLiveData<CurrentResponseApi>()
    val currentWeather: LiveData<CurrentResponseApi> = _currentWeather

    private val _forecastWeather = MutableLiveData<ForecastResponseApi>()
    val forecastWeather: LiveData<ForecastResponseApi> = _forecastWeather

    private val _airPollution = MutableLiveData<AirPollutionResponseApi>()
    val airPollution: LiveData<AirPollutionResponseApi> = _airPollution

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val apiKey = "dbf63f777a1a59d0c0cf2f858e2b5e4c"

    /**
     * Tải dữ liệu thời tiết hiện tại từ API
     * @param lat Vĩ độ của thành phố
     * @param lon Kinh độ của thành phố
     * @param unit Đơn vị nhiệt độ ("metric" cho °C, "imperial" cho °F)
     */
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

    /**
     * Tải dữ liệu dự báo thời tiết 5 ngày từ API
     * @param lat Vĩ độ của thành phố
     * @param lon Kinh độ của thành phố
     * @param unit Đơn vị nhiệt độ ("metric" cho °C, "imperial" cho °F)
     */
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

    /**
     * Tải dữ liệu chất lượng không khí (AQI) từ API
     * @param lat Vĩ độ của thành phố
     * @param lon Kinh độ của thành phố
     */
    fun loadAirPollution(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getAirPollution(lat, lon, apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _airPollution.postValue(response.body())
                }
            } catch (e: Exception) {
                // Quietly handle air pollution error
            }
        }
    }
}
