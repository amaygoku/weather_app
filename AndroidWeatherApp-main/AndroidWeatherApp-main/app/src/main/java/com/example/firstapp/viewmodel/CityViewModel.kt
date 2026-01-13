package com.example.firstapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.firstapp.model.CityResponseApi
import com.example.firstapp.repository.CityRepository
import com.example.firstapp.server.ApiClient
import com.example.firstapp.server.ApiServices
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CityViewModel(private val repository: CityRepository) : ViewModel() {

    // Constructor for creating an instance without a factory
    constructor() : this(CityRepository(ApiClient().getClient().create(ApiServices::class.java)))

    private val _cities = MutableLiveData<CityResponseApi>()
    val cities: LiveData<CityResponseApi> = _cities

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Cập nhật API Key mới
    private val apiKey = "dbf63f777a1a59d0c0cf2f858e2b5e4c"
    private var searchJob: Job? = null

    /**
     * Tìm kiếm danh sách thành phố theo tên
     * Sử dụng debounce (delay 500ms) để tránh gọi API quá nhiều
     * @param q Tên thành phố cần tìm
     * @param limit Số lượng kết quả tối đa
     */
    fun loadCities(q: String, limit: Int) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                delay(500)
                val response = repository.getCities(q, limit, apiKey)
                if (response.isSuccessful && response.body() != null) {
                    _cities.postValue(response.body())
                } else {
                    _error.postValue("Error: ${response.message()}")
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    _error.postValue("Exception: ${e.message}")
                }
            }
        }
    }
}
