package com.example.firstapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UnitViewModel : ViewModel() {
    private val _temperatureUnit = MutableLiveData<String>("Celsius")
    val temperatureUnit: LiveData<String> get() = _temperatureUnit

    fun setTemperatureUnit(unit: String) {
        _temperatureUnit.value = unit
    }
}